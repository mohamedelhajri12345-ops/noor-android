package com.elhajri.noor.community

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class CommunityProfile(
    val id: String = "",
    val handle: String = "",
    val displayName: String = "",
    val userEmail: String? = null,
    val city: String? = null,
    val bio: String? = null,
    val avatarUrl: String? = null,
    val createdDate: String? = null
)

data class CommunityMsg(
    val id: String = "",
    val conversationId: String = "",
    val nickname: String = "",
    val text: String = "",
    val avatarUrl: String? = null,
    val fileUrl: String? = null,
    val fileType: String? = null,
    val createdDate: String? = null,
    val isMine: Boolean = false
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
    val lastMessageTime: String = ""
)

object CommunityApi {
    private const val BASE_URL = "https://app.base44.com"
    private const val APP_ID = "6a833faeb9e42cca9a6576fa"
    private const val ENTITIES_PATH = "/api/apps/$APP_ID/entities"

    private val cookieStore = HashMap<String, List<Cookie>>()

    private val okHttpClient = OkHttpClient.Builder()
        .cookieJar(object : CookieJar {
            override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                cookieStore[url.host] = cookies
            }
            override fun loadForRequest(url: HttpUrl): List<Cookie> {
                return cookieStore[url.host] ?: emptyList()
            }
        })
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()

    suspend fun listProfiles(): List<CommunityProfile> = withContext(Dispatchers.IO) {
        val url = "$BASE_URL$ENTITIES_PATH/CommunityProfile"
        val request = Request.Builder().url(url).get().build()
        try {
            val response = okHttpClient.newCall(request).execute()
            val bodyStr = response.body?.string() ?: "[]"
            if (!response.isSuccessful) return@withContext emptyList()
            val arr = if (bodyStr.trim().startsWith("[")) JSONArray(bodyStr) else JSONArray()
            val list = mutableListOf<CommunityProfile>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    CommunityProfile(
                        id = obj.optString("id", obj.optString("_id", "")),
                        handle = obj.optString("handle", ""),
                        displayName = obj.optString("display_name", obj.optString("name", "عضو")),
                        userEmail = obj.optString("user_email", null),
                        city = obj.optString("city", null),
                        bio = obj.optString("bio", null),
                        avatarUrl = obj.optString("avatar_url", null),
                        createdDate = obj.optString("created_date", null)
                    )
                )
            }
            list
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun registerProfile(
        fullName: String,
        city: String = "",
        bio: String = "",
        handle: String = "",
        userEmail: String = ""
    ): CommunityProfile = withContext(Dispatchers.IO) {
        val url = "$BASE_URL$ENTITIES_PATH/CommunityProfile"
        val cleanHandle = handle.ifBlank { "user_${System.currentTimeMillis().toString().takeLast(6)}" }
            .lowercase().replace(" ", "_")
        val json = JSONObject().apply {
            put("handle", cleanHandle)
            put("display_name", fullName)
            put("city", city)
            put("bio", bio)
            if (userEmail.isNotBlank()) put("user_email", userEmail)
        }
        val request = Request.Builder()
            .url(url)
            .post(json.toString().toRequestBody(JSON_MEDIA))
            .build()
        try {
            val response = okHttpClient.newCall(request).execute()
            val bodyStr = response.body?.string() ?: "{}"
            val obj = JSONObject(bodyStr)
            CommunityProfile(
                id = obj.optString("id", obj.optString("_id", "")),
                handle = obj.optString("handle", cleanHandle),
                displayName = obj.optString("display_name", fullName),
                userEmail = obj.optString("user_email", userEmail),
                city = obj.optString("city", city),
                bio = obj.optString("bio", bio),
                avatarUrl = obj.optString("avatar_url", null),
                createdDate = obj.optString("created_date", null)
            )
        } catch (e: Exception) {
            e.printStackTrace()
            CommunityProfile(
                id = "temp_${System.currentTimeMillis()}",
                handle = cleanHandle,
                displayName = fullName,
                city = city,
                bio = bio
            )
        }
    }

    suspend fun sendMessage(
        conversationId: String,
        text: String,
        nickname: String = "عضو"
    ): CommunityMsg = withContext(Dispatchers.IO) {
        val url = "$BASE_URL$ENTITIES_PATH/CommunityMessage"
        val json = JSONObject().apply {
            put("conversation_id", conversationId)
            put("nickname", nickname)
            put("text", text)
        }
        val request = Request.Builder()
            .url(url)
            .post(json.toString().toRequestBody(JSON_MEDIA))
            .build()
        try {
            val response = okHttpClient.newCall(request).execute()
            val bodyStr = response.body?.string() ?: "{}"
            val obj = JSONObject(bodyStr)
            CommunityMsg(
                id = obj.optString("id", "msg_${System.currentTimeMillis()}"),
                conversationId = conversationId,
                nickname = nickname,
                text = text,
                createdDate = obj.optString("created_date", null),
                isMine = true
            )
        } catch (e: Exception) {
            e.printStackTrace()
            CommunityMsg(
                id = "msg_${System.currentTimeMillis()}",
                conversationId = conversationId,
                nickname = nickname,
                text = text,
                isMine = true
            )
        }
    }

    suspend fun sendProfileMessage(
        toProfileId: String,
        text: String,
        senderName: String = "عضو"
    ): CommunityMsg = sendMessage(toProfileId, text, senderName)

    suspend fun listMessages(conversationId: String): List<CommunityMsg> = withContext(Dispatchers.IO) {
        val url = "$BASE_URL$ENTITIES_PATH/CommunityMessage"
        val request = Request.Builder().url(url).get().build()
        try {
            val response = okHttpClient.newCall(request).execute()
            val bodyStr = response.body?.string() ?: "[]"
            if (!response.isSuccessful) return@withContext emptyList()
            val arr = if (bodyStr.trim().startsWith("[")) JSONArray(bodyStr) else JSONArray()
            val list = mutableListOf<CommunityMsg>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val cId = obj.optString("conversation_id", obj.optString("to_profile_id", ""))
                if (cId == conversationId || conversationId.isEmpty()) {
                    list.add(
                        CommunityMsg(
                            id = obj.optString("id", obj.optString("_id", "")),
                            conversationId = cId,
                            nickname = obj.optString("nickname", obj.optString("sender_name", "عضو")),
                            text = obj.optString("text", ""),
                            avatarUrl = obj.optString("avatar_url", null),
                            fileUrl = obj.optString("file_url", null),
                            fileType = obj.optString("file_type", null),
                            createdDate = obj.optString("created_date", null)
                        )
                    )
                }
            }
            list
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun listConversations(): List<Conversation> = withContext(Dispatchers.IO) {
        val url = "$BASE_URL$ENTITIES_PATH/Conversation"
        val request = Request.Builder().url(url).get().build()
        try {
            val response = okHttpClient.newCall(request).execute()
            val bodyStr = response.body?.string() ?: "[]"
            if (!response.isSuccessful) return@withContext emptyList()
            val arr = if (bodyStr.trim().startsWith("[")) JSONArray(bodyStr) else JSONArray()
            val list = mutableListOf<Conversation>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    Conversation(
                        id = obj.optString("id", obj.optString("_id", "")),
                        name = obj.optString("name", "محادثة"),
                        type = obj.optString("type", "direct"),
                        lastMessage = obj.optString("last_message", ""),
                        lastSender = obj.optString("last_sender", ""),
                        lastMessageTime = obj.optString("last_message_time", "")
                    )
                )
            }
            list
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
