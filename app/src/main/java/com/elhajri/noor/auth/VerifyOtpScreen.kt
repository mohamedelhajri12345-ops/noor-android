package com.elhajri.noor.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.ui.AmiriFamily
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.NavyLight
import com.elhajri.noor.ui.TextMain
import kotlinx.coroutines.launch

/**
 * شاشة إدخال رمز التحقق (OTP) — بعد إنشاء حساب المجتمع يرسل الخادم
 * رمزاً من 6 أرقام إلى البريد الإلكتروني؛ يكتبه المستخدم هنا، ثم
 * يتحقق الخادم ويُفتح المجتمع مباشرة.
 */
@Composable
fun VerifyOtpScreen(
    onVerified: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var code by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isResending by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val email = OtpFlow.pendingEmail
    val password = OtpFlow.pendingPassword

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Icon(
            Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = null,
            tint = Gold,
            modifier = Modifier.size(24.dp).clip(RoundedCornerShape(8.dp)).clickable { onBack() }
        )

        Spacer(Modifier.height(36.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Filled.Email,
                contentDescription = null,
                tint = Gold,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "تفعيل حسابك",
                color = Gold,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = AmiriFamily
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "أرسلنا رمز تحقق من 6 أرقام إلى بريدك\n$email",
                color = TextMain.copy(alpha = 0.65f),
                fontSize = 13.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = code,
            onValueChange = {
                if (it.length <= 6 && it.all { ch -> ch.isDigit() }) code = it
            },
            placeholder = { Text("••••••", color = TextMain.copy(alpha = 0.3f)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true,
            isError = errorMessage != null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Gold,
                unfocusedBorderColor = NavyLight,
                cursorColor = Gold,
                focusedTextColor = TextMain,
                unfocusedTextColor = TextMain
            ),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                letterSpacing = 8.sp
            ),
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMessage != null) {
            Spacer(Modifier.height(10.dp))
            Text(
                errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (successMessage != null) {
            Spacer(Modifier.height(10.dp))
            Text(
                successMessage!!,
                color = GoldSoft,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(22.dp))

        Button(
            onClick = {
                if (code.length < 6 || isLoading) return@Button
                isLoading = true
                errorMessage = null
                scope.launch {
                    val res = Base44Auth.verifyOtp(context, email, code)
                    res.fold(
                        onSuccess = {
                            // التحقق تم — سجّل الدخول تلقائياً ثم افتح المجتمع
                            // التحقق نجح — سجّل الدخول تلقائياً إن وُجدت كلمة المرور
                            if (password.isNotBlank()) {
                                Base44Auth.login(context, email, password)
                            }
                            isLoading = false
                            OtpFlow.pendingPassword = ""
                            onVerified()
                        },
                        onFailure = { err ->
                            isLoading = false
                            errorMessage = err.message ?: "رمز التحقق غير صحيح"
                        }
                    )
                }
            },
            enabled = !isLoading && code.length == 6,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Navy, strokeWidth = 2.dp)
            } else {
                Text("تحقق", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Spacer(Modifier.height(14.dp))

        TextButton(
            onClick = {
                if (isResending) return@TextButton
                isResending = true
                errorMessage = null
                scope.launch {
                    val res = Base44Auth.resendOtp(email)
                    isResending = false
                    res.fold(
                        onSuccess = { successMessage = "أُرسل رمز جديد إلى بريدك — تحقق من صندوق الوارد" },
                        onFailure = { err -> errorMessage = err.message ?: "فشل إعادة الإرسال" }
                    )
                }
            },
            enabled = !isResending
        ) {
            Text(
                if (isResending) "جارٍ إعادة الإرسال..." else "لم يصلك الرمز؟ أعد الإرسال",
                color = GoldSoft,
                fontSize = 13.sp
            )
        }
    }
}
