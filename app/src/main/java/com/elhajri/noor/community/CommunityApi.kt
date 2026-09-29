package com.elhajri.noor.community

import com.elhajri.noor.auth.Base44Auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

private const val BASE_URL = "https://app.base44.com"
private const val APP_ID = "6a833faeb9e42cca9a6576fa"
private const val ENTITIES_URL = "$BASE_URL/api/apps/$APP_ID/entities"
private const val UPLOAD_URL = "$BASE_URL/api/apps/$APP_ID/integration-endpoints/Core/UploadFile"

data class User(
    val email: String,
    val id: String = ""
)

data class CommunityProfile(
    val id: String = "",
    val handle: String = "",
    val displayName: String = "",
    val avatarUrl: String = "",
    val userEmail: String = ""
)

data class Conversation(
    val id: String = "",
    val name: String = "",
    val type: String = "direct",
    val memberHandles: List<String> = emptyList(),
    val memberNames: List<String> = emptyList(),
    val memberAvatars: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastSender: String = "",
    val lastMessageTime: String = "",
    val createdDate: String = ""
)

data class CommunityMessage(
    val id: String = "",
    val conversationId: String = "",
    val nickname: String = "",
    val text: String = "",
    val avatarUrl: String = "",
    val fileUrl: String = "",
    val fileType: String = "",
    val createdDate: String = "",
    val pending: Boolean = false,
    val failed: Boolean = false
)

object CommunityApi {
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private fun checkResponse(code: Int, bodyStr: String?) {
        if (code == 401 || code == 403) {
            throw Exception("تحتاج تسجيل الدخول للمشاركة")
        }
        if (code !in 200..299) {
            val msg = try {
                if (!bodyStr.isNullOrBlank()) JSONObject(bodyStr).optString("message", null) else null
            } catch (_: Exception) { null }
            throw Exception(msg ?: "حدث خطأ في الاتصال بالخادم ($code)")
        }
    }

    suspend fun getCurrentUser(): Result<User> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$ENTITIES_URL/User/me")
                .get()
                .build()

            val client = Base44Auth.getClient()
            val response = client.newCall(request).execute()
            val bodyStr = response.body?.string()

            if (response.code == 401 || response.code == 403) {
                return@withContext Result.failure(Exception("تحتاج تسجيل الدخول للمشاركة"))
            }
            if (!response.isSuccessful || bodyStr.isNullOrBlank()) {
                return@withContext Result.failure(Exception("غير مسجّل الدخول"))
            }

            val json = JSONObject(bodyStr)
            val email = json.optString("email", "")
            if (email.isBlank()) {
                return@withContext Result.failure(Exception("غير مسجّل الدخول"))
            }
            val id = json.optString("id", json.optString("_id", ""))
            Result.success(User(email = email, id = id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProfileByEmail(email: String): Result<CommunityProfile?> = withContext(Dispatchers.IO) {
        try {
            val q = JSONObject().put("user_email", email).toString()
            val encodedQ = URLEncoder.encode(q, "UTF-8")
            val url = "$ENTITIES_URL/CommunityProfile?q=$encodedQ"

            val request = Request.Builder().url(url).get().build()
            val response = Base44Auth.getClient().newCall(request).execute()
            val bodyStr = response.body?.string()
            checkResponse(response.code, bodyStr)

            val arr = JSONArray(bodyStr ?: "[]")
            if (arr.length() == 0) {
                Result.success(null)
            } else {
                val obj = arr.getJSONObject(0)
                Result.success(parseProfile(obj))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProfileByHandle(handle: String): Result<CommunityProfile?> = withContext(Dispatchers.IO) {
        try {
            val q = JSONObject().put("handle", handle.lowercase().trim()).toString()
            val encodedQ = URLEncoder.encode(q, "UTF-8")
            val url = "$ENTITIES_URL/CommunityProfile?q=$encodedQ"

            val request = Request.Builder().url(url).get().build()
            val response = Base44Auth.getClient().newCall(request).execute()
            val bodyStr = response.body?.string()
            checkResponse(response.code, bodyStr)

            val arr = JSONArray(bodyStr ?: "[]")
            if (arr.length() == 0) {
                Result.success(null)
            } else {
                val obj = arr.getJSONObject(0)
                Result.success(parseProfile(obj))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkHandleAvailable(handle: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val res = getProfileByHandle(handle)
            res.map { profile -> profile == null }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createProfile(
        handle: String,
        displayName: String,
        avatarUrl: String,
        userEmail: String
    ): Result<CommunityProfile> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("handle", handle.lowercase().trim())
                put("display_name", displayName.trim())
                put("avatar_url", avatarUrl)
                put("user_email", userEmail)
            }
            val request = Request.Builder()
                .url("$ENTITIES_URL/CommunityProfile")
                .post(json.toString().toRequestBody(jsonMediaType))
                .build()

            val response = Base44Auth.getClient().newCall(request).execute()
            val bodyStr = response.body?.string()
            checkResponse(response.code, bodyStr)

            val obj = JSONObject(bodyStr ?: "{}")
            Result.success(parseProfile(obj))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getConversations(myHandle: String): Result<List<Conversation>> = withContext(Dispatchers.IO) {
        try {
            val url = "$ENTITIES_URL/Conversation?sort=-created_date&limit=100"
            val request = Request.Builder().url(url).get().build()
            val response = Base44Auth.getClient().newCall(request).execute()
            val bodyStr = response.body?.string()
            checkResponse(response.code, bodyStr)

            val arr = JSONArray(bodyStr ?: "[]")
            val list = mutableListOf<Conversation>()
            for (i in 0 until arr.length()) {
                val conv = parseConversation(arr.getJSONObject(i))
                if (conv.memberHandles.contains(myHandle)) {
                    list.add(conv)
                }
            }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createConversation(
        name: String,
        type: String,
        memberHandles: List<String>,
        memberNames: List<String>,
        memberAvatars: List<String>
    ): Result<Conversation> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("name", name)
                put("type", type)
                put("member_handles", JSONArray(memberHandles))
                put("member_names", JSONArray(memberNames))
                put("member_avatars", JSONArray(memberAvatars))
                put("last_message", "")
                put("last_sender", "")
            }
            val request = Request.Builder()
                .url("$ENTITIES_URL/Conversation")
                .post(json.toString().toRequestBody(jsonMediaType))
                .build()

            val response = Base44Auth.getClient().newCall(request).execute()
            val bodyStr = response.body?.string()
            checkResponse(response.code, bodyStr)

            val obj = JSONObject(bodyStr ?: "{}")
            Result.success(parseConversation(obj))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateConversation(
        id: String,
        lastMessage: String,
        lastSender: String,
        lastMessageTime: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("last_message", lastMessage)
                put("last_sender", lastSender)
                put("last_message_time", lastMessageTime)
            }
            val request = Request.Builder()
                .url("$ENTITIES_URL/Conversation/$id")
                .put(json.toString().toRequestBody(jsonMediaType))
                .build()

            val response = Base44Auth.getClient().newCall(request).execute()
            val bodyStr = response.body?.string()
            checkResponse(response.code, bodyStr)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMessages(conversationId: String): Result<List<CommunityMessage>> = withContext(Dispatchers.IO) {
        try {
            val q = JSONObject().put("conversation_id", conversationId).toString()
            val encodedQ = URLEncoder.encode(q, "UTF-8")
            val url = "$ENTITIES_URL/CommunityMessage?q=$encodedQ&sort=-created_date&limit=50"

            val request = Request.Builder().url(url).get().build()
            val response = Base44Auth.getClient().newCall(request).execute()
            val bodyStr = response.body?.string()
            checkResponse(response.code, bodyStr)

            val arr = JSONArray(bodyStr ?: "[]")
            val list = mutableListOf<CommunityMessage>()
            for (i in 0 until arr.length()) {
                list.add(parseMessage(arr.getJSONObject(i)))
            }
            Result.success(list.reversed())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createMessage(
        conversationId: String,
        nickname: String,
        text: String,
        avatarUrl: String = "",
        fileUrl: String = "",
        fileType: String = ""
    ): Result<CommunityMessage> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("conversation_id", conversationId)
                put("nickname", nickname)
                put("text", text)
                put("avatar_url", avatarUrl)
                if (fileUrl.isNotBlank()) put("file_url", fileUrl)
                if (fileType.isNotBlank()) put("file_type", fileType)
            }
            val request = Request.Builder()
                .url("$ENTITIES_URL/CommunityMessage")
                .post(json.toString().toRequestBody(jsonMediaType))
                .build()

            val response = Base44Auth.getClient().newCall(request).execute()
            val bodyStr = response.body?.string()
            checkResponse(response.code, bodyStr)

            val obj = JSONObject(bodyStr ?: "{}")
            Result.success(parseMessage(obj))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteMessage(messageId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$ENTITIES_URL/CommunityMessage/$messageId")
                .delete()
                .build()

            val response = Base44Auth.getClient().newCall(request).execute()
            val bodyStr = response.body?.string()
            checkResponse(response.code, bodyStr)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadFile(bytes: ByteArray, fileName: String, mimeType: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val mediaType = mimeType.toMediaType()
            val filePart = bytes.toRequestBody(mediaType)
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", fileName, filePart)
                .build()

            val request = Request.Builder()
                .url(UPLOAD_URL)
                .post(requestBody)
                .build()

            val response = Base44Auth.getClient().newCall(request).execute()
            val bodyStr = response.body?.string()
            checkResponse(response.code, bodyStr)

            val json = JSONObject(bodyStr ?: "{}")
            val fileUrl = json.optString("file_url", "")
            if (fileUrl.isBlank()) {
                Result.failure(Exception("فشل تحميل الملف"))
            } else {
                Result.success(fileUrl)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseProfile(obj: JSONObject): CommunityProfile {
        return CommunityProfile(
            id = obj.optString("id", obj.optString("_id", "")),
            handle = obj.optString("handle", ""),
            displayName = obj.optString("display_name", obj.optString("name", "")),
            avatarUrl = obj.optString("avatar_url", ""),
            userEmail = obj.optString("user_email", "")
        )
    }

    private fun parseConversation(obj: JSONObject): Conversation {
        val handlesJson = obj.optJSONArray("member_handles") ?: JSONArray()
        val namesJson = obj.optJSONArray("member_names") ?: JSONArray()
        val avatarsJson = obj.optJSONArray("member_avatars") ?: JSONArray()

        val handles = mutableListOf<String>()
        val names = mutableListOf<String>()
        val avatars = mutableListOf<String>()

        for (i in 0 until handlesJson.length()) handles.add(handlesJson.optString(i))
        for (i in 0 until namesJson.length()) names.add(namesJson.optString(i))
        for (i in 0 until avatarsJson.length()) avatars.add(avatarsJson.optString(i))

        return Conversation(
            id = obj.optString("id", obj.optString("_id", "")),
            name = obj.optString("name", ""),
            type = obj.optString("type", "direct"),
            memberHandles = handles,
            memberNames = names,
            memberAvatars = avatars,
            lastMessage = obj.optString("last_message", ""),
            lastSender = obj.optString("last_sender", ""),
            lastMessageTime = obj.optString("last_message_time", ""),
            createdDate = obj.optString("created_date", "")
        )
    }

    private fun parseMessage(obj: JSONObject): CommunityMessage {
        return CommunityMessage(
            id = obj.optString("id", obj.optString("_id", "")),
            conversationId = obj.optString("conversation_id", ""),
            nickname = obj.optString("nickname", ""),
            text = obj.optString("text", ""),
            avatarUrl = obj.optString("avatar_url", ""),
            fileUrl = obj.optString("file_url", ""),
            fileType = obj.optString("file_type", ""),
            createdDate = obj.optString("created_date", "")
        )
    }
}
