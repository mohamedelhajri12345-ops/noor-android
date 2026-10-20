package com.elhajri.noor.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.settings.t
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.noorGlassCard
import com.elhajri.noor.ui.themeScreenBackground
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpScreen(
    email: String = OtpFlow.pendingEmail,
    onSuccess: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var otpCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isResending by remember { mutableStateOf(false) }
    var isSuccessState by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }

    var secondsRemaining by remember { mutableStateOf(60) }

    // 60-second countdown timer
    LaunchedEffect(secondsRemaining) {
        if (secondsRemaining > 0) {
            delay(1000L)
            secondsRemaining -= 1
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color(0xFF070B14)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .themeScreenBackground(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Gold
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isSuccessState) Icons.Filled.CheckCircle else Icons.Filled.MarkEmailRead,
                            contentDescription = null,
                            tint = Navy,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = t("أدخل رمز التحقق", "Enter Verification Code"),
                    style = MaterialTheme.typography.headlineLarge,
                    color = Gold,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (email.isNotBlank()) "تم إرسال رمز التحقق إلى $email" else t("تم إرسال رمز التحقق إلى بريدك الإلكتروني", "A verification code has been sent to your email"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = GoldSoft.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .noorGlassCard(cornerRadius = 24.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isSuccessState) {
                            AnimatedVisibility(visible = true, enter = fadeIn()) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(vertical = 16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = Gold,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = t("تم التحقق بنجاح!", "Verified successfully!"),
                                        color = Gold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                            }
                        } else {
                            if (errorMessage != null) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.errorContainer
                                ) {
                                    Text(
                                        text = errorMessage!!,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(12.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            if (infoMessage != null) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = Gold.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = infoMessage!!,
                                        color = Gold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(12.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            // 6-digit input textfield
                            OutlinedTextField(
                                value = otpCode,
                                onValueChange = { newValue ->
                                    val digitsOnly = newValue.filter { it.isDigit() }
                                    if (digitsOnly.length <= 6) {
                                        otpCode = digitsOnly
                                        errorMessage = null
                                    }
                                },
                                label = { Text("رمز التحقق (٦ أرقام)", color = GoldSoft) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Gold,
                                    unfocusedBorderColor = Gold.copy(alpha = 0.4f),
                                    focusedLabelColor = Gold,
                                    cursorColor = Gold,
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Submit Button
                            Button(
                                onClick = {
                                    val cleanOtp = otpCode.filter { it.isDigit() }
                                    if (cleanOtp.length < 6) {
                                        errorMessage = "يرجى إدخال رمز التحقق المكون من ٦ أرقام"
                                        return@Button
                                    }
                                    isLoading = true
                                    errorMessage = null
                                    scope.launch {
                                        val res = Base44Auth.verifyOtp(context, email, cleanOtp)
                                        if (res.isSuccess) {
                                            if (OtpFlow.pendingPassword.isNotEmpty()) {
                                                Base44Auth.login(context, email, OtpFlow.pendingPassword)
                                            }
                                            isLoading = false
                                            isSuccessState = true
                                            delay(1000L)
                                            onSuccess()
                                        } else {
                                            isLoading = false
                                            errorMessage = res.exceptionOrNull()?.message ?: "رمز التحقق غير صحيح"
                                        }
                                    }
                                },
                                enabled = !isLoading && otpCode.filter { it.isDigit() }.length == 6,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Navy,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("تأكيد الرمز", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Resend OTP Button with 60s countdown
                            TextButton(
                                onClick = {
                                    if (secondsRemaining == 0 && !isResending) {
                                        isResending = true
                                        errorMessage = null
                                        infoMessage = null
                                        scope.launch {
                                            val res = Base44Auth.resendOtp(context, email)
                                            isResending = false
                                            res.fold(
                                                onSuccess = {
                                                    secondsRemaining = 60
                                                    infoMessage = "تم إعادة إرسال رمز التحقق بنجاح"
                                                },
                                                onFailure = { err ->
                                                    errorMessage = err.message ?: "فشل إعادة إرسال الرمز"
                                                }
                                            )
                                        }
                                    }
                                },
                                enabled = secondsRemaining == 0 && !isResending
                            ) {
                                if (isResending) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Gold,
                                        strokeWidth = 2.dp
                                    )
                                } else if (secondsRemaining > 0) {
                                    Text(
                                        text = "إعادة إرسال الرمز خلال ($secondsRemaining ثانية)",
                                        color = GoldSoft.copy(alpha = 0.6f),
                                        fontSize = 13.sp
                                    )
                                } else {
                                    Text(
                                        text = t("إعادة إرسال الرمز", "Resend Code"),
                                        color = Gold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
