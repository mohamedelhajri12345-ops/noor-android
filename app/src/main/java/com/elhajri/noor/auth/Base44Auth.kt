package com.elhajri.noor.auth

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

private const val BASE_URL = "https://app.base44.com"
private const val APP_ID = "6a833faeb9e42cca9a6576fa"
private const val LOGIN_PATH = "/api/apps/$APP_ID/auth/login"
private const val REGISTER_PATH = "/api/apps/$APP_ID/auth/register"
private const val FORGOT_PASSWORD_PATH = "/api/apps/$APP_ID/auth/forgot-password"
private const val RESET_PASSWORD_PATH = "/api/apps/$APP_ID/auth/reset-password"
private const val VERIFY_OTP_PATH = "/api/apps/$APP_ID/auth/verify-otp"
private const val RESEND_OTP_PATH = "/api/apps/$APP_ID/auth/resend-otp"

/** جسر مؤقت لبيانات التحقق بين شاشة التسجيل وشاشة الرمز — في الذاكرة فقط */
object OtpFlow {
    var pendingEmail: String = ""
    var pendingPassword: String = ""
}

object Base44Auth {
    private const val PREFS_NAME = "noor_auth_prefs"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_AUTH_TOKEN = "auth_token"

    private var inMemoryLoggedIn: Boolean = false
    private var authToken: String? = null

    private val cookieStore = mutableMapOf<String, List<Cookie>>()

    private val cookieJar = object : CookieJar {
        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            cookieStore[url.host] = cookies
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            return cookieStore[url.host] ?: emptyList()
        }
    }

    // Attaches "Authorization: Bearer <token>" to every request once logged in —
    // without this, entity/community calls always look unauthenticated even after a
    // successful login (the token was fetched but never sent back to the server).
    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val token = authToken
        val request = if (!token.isNullOrBlank() && original.header("Authorization") == null) {
            original.newBuilder().header("Authorization", "Bearer $token").build()
        } else {
            original
        }
        chain.proceed(request)
    }

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor(authInterceptor)
            .build()
    }

    fun getClient(): OkHttpClient = httpClient
    fun apiClient(): OkHttpClient = httpClient
    fun getAuthToken(): String? = authToken

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    fun init(context: Context) {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        inMemoryLoggedIn = sp.getBoolean(KEY_IS_LOGGED_IN, false)
        authToken = sp.getString(KEY_AUTH_TOKEN, null)
    }

    fun isLoggedIn(): Boolean = inMemoryLoggedIn

    fun logout() {
        inMemoryLoggedIn = false
        authToken = null
        cookieStore.clear()
    }

    fun logout(context: Context) {
        logout()
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sp.edit().clear().apply()
    }

    suspend fun login(email: String, password: String): Result<Unit> = login(null, email, password)

    suspend fun login(context: Context?, email: String, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("email", email)
                put("password", password)
            }
            val requestBody = json.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(BASE_URL + LOGIN_PATH)
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val bodyStr = response.body?.string() ?: ""
                if (bodyStr.isNotEmpty()) {
                    try {
                        val respObj = JSONObject(bodyStr)
                        authToken = respObj.optString("access_token", respObj.optString("token", null))
                    } catch (_: Exception) {}
                }
                inMemoryLoggedIn = true
                context?.let { ctx ->
                    val sp = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    sp.edit()
                        .putBoolean(KEY_IS_LOGGED_IN, true)
                        .putString(KEY_AUTH_TOKEN, authToken)
                        .apply()
                }
                Result.success(Unit)
            } else {
                val errBody = response.body?.string()
                val message = parseErrorMessage(errBody) ?: "فشل تسجيل الدخول (${response.code})"
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(email: String, password: String, fullName: String): Result<Unit> = register(null, email, password, fullName)

    suspend fun register(context: Context?, email: String, password: String, fullName: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("email", email)
                put("password", password)
                put("full_name", fullName)
                put("name", fullName)
            }
            val requestBody = json.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(BASE_URL + REGISTER_PATH)
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                inMemoryLoggedIn = true
                context?.let { ctx ->
                    val sp = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    sp.edit().putBoolean(KEY_IS_LOGGED_IN, true).apply()
                }
                Result.success(Unit)
            } else {
                val errBody = response.body?.string()
                val message = parseErrorMessage(errBody) ?: "فشل إنشاء الحساب (${response.code})"
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * التحقق من رمز OTP المرسل إلى البريد الإلكتروني بعد إنشاء الحساب.
     * إذا أعاد الخادم access_token خُزّن واستُخدم تلقائياً.
     */
    suspend fun verifyOtp(context: Context?, email: String, otpCode: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("email", email)
                put("otpCode", otpCode)
            }
            val requestBody = json.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(BASE_URL + VERIFY_OTP_PATH)
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                val bodyStr = response.body?.string() ?: ""
                if (bodyStr.isNotEmpty()) {
                    try {
                        val respObj = JSONObject(bodyStr)
                        val token = respObj.optString("access_token", respObj.optString("token", ""))
                        if (token.isNotBlank()) authToken = token
                    } catch (_: Exception) {}
                }
                inMemoryLoggedIn = true
                context?.let { ctx ->
                    val sp = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    sp.edit()
                        .putBoolean(KEY_IS_LOGGED_IN, true)
                        .putString(KEY_AUTH_TOKEN, authToken)
                        .apply()
                }
                Result.success(Unit)
            } else {
                val errBody = response.body?.string()
                val message = parseErrorMessage(errBody) ?: "رمز التحقق غير صحيح أو منتهي (${response.code})"
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** إعادة إرسال رمز التحقق إلى البريد الإلكتروني */
    suspend fun resendOtp(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply { put("email", email) }
            val requestBody = json.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(BASE_URL + RESEND_OTP_PATH)
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errBody = response.body?.string()
                val message = parseErrorMessage(errBody) ?: "فشل إعادة إرسال الرمز (${response.code})"
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun forgotPassword(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("email", email)
            }
            val requestBody = json.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(BASE_URL + FORGOT_PASSWORD_PATH)
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errBody = response.body?.string()
                val message = parseErrorMessage(errBody) ?: "فشل إرسال رابط إعادة التعيين"
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetPassword(token: String, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("token", token)
                put("reset_token", token)
                put("password", password)
                put("new_password", password)
            }
            val requestBody = json.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(BASE_URL + RESET_PASSWORD_PATH)
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errBody = response.body?.string()
                val message = parseErrorMessage(errBody) ?: "فشل إعادة تعيين كلمة المرور"
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseErrorMessage(jsonStr: String?): String? {
        if (jsonStr.isNullOrEmpty()) return null
        return try {
            val obj = JSONObject(jsonStr)
            obj.optString("message", obj.optString("error", null))
        } catch (_: Exception) {
            null
        }
    }
}
