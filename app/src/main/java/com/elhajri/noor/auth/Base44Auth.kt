package com.elhajri.noor.auth

import com.elhajri.noor.settings.t

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
import java.io.IOException

private const val BASE_URL = "https://app.base44.com"
private const val APP_ID = "6a833faeb9e42cca9a6576fa"

private const val LOGIN_PATH = "/api/apps/$APP_ID/auth/login"
private const val REGISTER_PATH = "/api/apps/$APP_ID/auth/register"
private const val FORGOT_PASSWORD_PATH = "/api/apps/$APP_ID/auth/forgot-password"
private const val RESET_PASSWORD_PATH = "/api/apps/$APP_ID/auth/reset-password"
private const val VERIFY_OTP_PATH = "/api/apps/$APP_ID/auth/verify-otp"
private const val RESEND_OTP_PATH = "/api/apps/$APP_ID/auth/resend-otp"

/**
 * Temporary in-memory state for email & password during registration and OTP verification.
 */
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
        val cleanEmail = email.trim().lowercase()
        try {
            val json = JSONObject().apply {
                put("email", cleanEmail)
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
                val parsed = parseErrorMessage(errBody)
                val message = when {
                    parsed != null -> parsed
                response.code == 401 -> t("البريد الإلكتروني أو كلمة المرور غير صحيحة", "Incorrect email or password")
                else -> t("فشل تسجيل الدخول ", "Login failed (") + "(${response.code})" + t("", ")")
                }
                Result.failure(Exception(message))
            }
        } catch (e: IOException) {
            Result.failure(Exception(t("تعذر الاتصال بالشبكة، يرجى التحقق من الاتصال بالإنترنت", "Network error, please check your internet connection")))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: t("حدث خطأ غير متوقع", "An unexpected error occurred")))
        }
    }

    suspend fun register(email: String, password: String, fullName: String): Result<Unit> = register(null, email, password, fullName)

    suspend fun register(context: Context?, email: String, password: String, fullName: String): Result<Unit> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        val cleanName = fullName.trim()
        try {
            val json = JSONObject().apply {
                put("email", cleanEmail)
                put("password", password)
                put("full_name", cleanName)
                put("name", cleanName)
            }
            val requestBody = json.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(BASE_URL + REGISTER_PATH)
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errBody = response.body?.string()
                val parsed = parseErrorMessage(errBody)
                val message = when {
                    parsed != null -> parsed
                    response.code == 409 || (errBody?.contains("already", ignoreCase = true) == true) -> t("البريد الإلكتروني مسجل بالفعل، يرجى تسجيل الدخول", "Email already registered, please log in")
                    else -> t("فشل إنشاء الحساب ", "Account creation failed (") + "(${response.code})" + t("", ")")
                }
                Result.failure(Exception(message))
            }
        } catch (e: IOException) {
            Result.failure(Exception(t("تعذر الاتصال بالشبكة، يرجى التحقق من الاتصال بالإنترنت", "Network error, please check your internet connection")))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: t("حدث خطأ أثناء إنشاء الحساب", "Error during account creation")))
        }
    }

    suspend fun verifyOtp(email: String, otpCode: String): Result<Unit> = verifyOtp(null, email, otpCode)

    suspend fun verifyOtp(context: Context?, email: String, otpCode: String): Result<Unit> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        val cleanOtp = otpCode.filter { it.isDigit() }
        try {
            val json = JSONObject().apply {
                put("email", cleanEmail)
                put("otp_code", cleanOtp)
                put("code", cleanOtp)
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
                val rawMsg = parseErrorMessage(errBody)
                val rawLower = rawMsg?.lowercase() ?: ""
                val message = when {
                    rawLower.contains("expire") || rawLower.contains("منتهي") || response.code == 410 ->
                t("رمز التحقق منتهي الصلاحية، يرجى طلب رمز جديد", "Verification code expired, please request a new code")
                    rawLower.contains("invalid") || rawLower.contains("wrong") || rawLower.contains("incorrect") || rawLower.contains("غير صحيح") || response.code == 400 || response.code == 401 || response.code == 422 ->
                t("رمز التحقق غير صحيح، يرجى التأكد من الرمز المدخل", "Incorrect verification code, please check and try again")
                    rawMsg != null -> rawMsg
                else -> t("رمز التحقق غير صحيح أو منتهي الصلاحية ", "Verification code invalid or expired (") + "(${response.code})" + t("", ")")
                }
                Result.failure(Exception(message))
            }
        } catch (e: IOException) {
            Result.failure(Exception(t("تعذر الاتصال بالشبكة، يرجى التأكد من اتصال الإنترنت والمحاولة مرة أخرى", "Network error, please check your internet connection and try again")))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: t("حدث خطأ أثناء التحقق من الرمز", "Error during code verification")))
        }
    }

    suspend fun resendOtp(email: String): Result<Unit> = resendOtp(null, email)

    suspend fun resendOtp(context: Context?, email: String): Result<Unit> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        try {
            val json = JSONObject().apply {
                put("email", cleanEmail)
            }
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
                val parsed = parseErrorMessage(errBody)
            val message = parsed ?: (t("فشل إعادة إرسال الرمز ", "Failed to resend code (") + "(${response.code})" + t("", ")"))
                Result.failure(Exception(message))
            }
        } catch (e: IOException) {
            Result.failure(Exception(t("تعذر الاتصال بالشبكة، يرجى التأكد من اتصال الإنترنت", "Network error, please check your internet connection")))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: t("فشل إعادة إرسال الرمز", "Failed to resend code")))
        }
    }

    suspend fun forgotPassword(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        try {
            val json = JSONObject().apply {
                put("email", cleanEmail)
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
            val message = parseErrorMessage(errBody) ?: t("فشل إرسال رابط إعادة التعيين", "Failed to send reset link")
                Result.failure(Exception(message))
            }
        } catch (e: IOException) {
            Result.failure(Exception(t("تعذر الاتصال بالشبكة، يرجى التحقق من اتصالك بالإنترنت", "Network error, please check your internet connection")))
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
            val message = parseErrorMessage(errBody) ?: t("فشل إعادة تعيين كلمة المرور", "Failed to reset password")
                Result.failure(Exception(message))
            }
        } catch (e: IOException) {
            Result.failure(Exception(t("تعذر الاتصال بالشبكة، يرجى التحقق من اتصالك بالإنترنت", "Network error, please check your internet connection")))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseErrorMessage(jsonStr: String?): String? {
        if (jsonStr.isNullOrBlank()) return null
        return try {
            val obj = JSONObject(jsonStr)
            val msg = when {
                obj.has("message") && obj.getString("message").isNotBlank() -> obj.getString("message")
                obj.has("detail") -> {
                    val detailObj = obj.get("detail")
                    if (detailObj is JSONObject) detailObj.optString("message", detailObj.toString())
                    else detailObj.toString()
                }
                obj.has("error") -> {
                    val errObj = obj.get("error")
                    if (errObj is JSONObject) errObj.optString("message", errObj.toString())
                    else errObj.toString()
                }
                obj.has("error_description") && obj.getString("error_description").isNotBlank() -> obj.getString("error_description")
                else -> ""
            }
            if (msg.isNotBlank()) msg else null
        } catch (_: Exception) {
            if (jsonStr.length in 1..300 && !jsonStr.startsWith("<")) jsonStr.trim() else null
        }
    }
}
