package com.elhajri.noor.auth

import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.settings.t
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.noorGlassCard
import com.elhajri.noor.ui.themeScreenBackground
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateToOtp: () -> Unit,
    onNavigateToLogin: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPassword by remember { mutableStateOf("") }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun isValidEmail(target: String): Boolean {
        return target.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(target).matches()
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
                            imageVector = Icons.Filled.PersonAdd,
                            contentDescription = null,
                            tint = Navy,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "إنشاء حساب جديد",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Gold,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = t("أدخل بياناتك لإنشاء حساب في نُور", "Enter your details to create a NOOR account"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = GoldSoft.copy(alpha = 0.8f),
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
                            .padding(24.dp)
                    ) {
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
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        // Full Name field with Arabic label
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it; errorMessage = null },
                            label = { Text("الاسم الكامل", color = GoldSoft) },
                            leadingIcon = {
                                Icon(Icons.Filled.Person, contentDescription = null, tint = Gold)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
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

                        Spacer(modifier = Modifier.height(16.dp))

                        // Email field with Arabic label
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it; errorMessage = null },
                            label = { Text("البريد الإلكتروني", color = GoldSoft) },
                            leadingIcon = {
                                Icon(Icons.Filled.Mail, contentDescription = null, tint = Gold)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
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

                        Spacer(modifier = Modifier.height(16.dp))

                        // Password field with Arabic label & eye toggle
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; errorMessage = null },
                            label = { Text("كلمة المرور", color = GoldSoft) },
                            leadingIcon = {
                                Icon(Icons.Filled.Lock, contentDescription = null, tint = Gold)
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                        contentDescription = if (passwordVisible) "إخفاء كلمة المرور" else "إظهار كلمة المرور",
                                        tint = Gold
                                    )
                                }
                            },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
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

                        Spacer(modifier = Modifier.height(16.dp))

                        // Confirm Password field with Arabic label & eye toggle
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it; errorMessage = null },
                            label = { Text("تأكيد كلمة المرور", color = GoldSoft) },
                            leadingIcon = {
                                Icon(Icons.Filled.Lock, contentDescription = null, tint = Gold)
                            },
                            trailingIcon = {
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(
                                        imageVector = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                        contentDescription = if (confirmPasswordVisible) "إخفاء كلمة المرور" else "إظهار كلمة المرور",
                                        tint = Gold
                                    )
                                }
                            },
                            singleLine = true,
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
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

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                val cleanEmail = email.trim()
                                val cleanName = fullName.trim()
                                if (cleanName.isBlank() || cleanEmail.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                                    errorMessage = "يرجى ملء جميع الحقول المطلوبة"
                                    return@Button
                                }
                                if (!isValidEmail(cleanEmail)) {
                                    errorMessage = "يرجى إدخال بريد إلكتروني صحيح"
                                    return@Button
                                }
                                if (password.length < 6) {
                                    errorMessage = "يجب أن تكون كلمة المرور ٦ أحرف على الأقل"
                                    return@Button
                                }
                                if (password != confirmPassword) {
                                    errorMessage = "كلمتا المرور غير متطابقتين، يرجى التأكد وإعادة المحاولة"
                                    return@Button
                                }

                                isLoading = true
                                errorMessage = null
                                scope.launch {
                                    val res = Base44Auth.register(context, cleanEmail, password, cleanName)
                                    isLoading = false
                                    res.fold(
                                        onSuccess = {
                                            OtpFlow.pendingEmail = cleanEmail
                                            OtpFlow.pendingPassword = password
                                            onNavigateToOtp()
                                        },
                                        onFailure = { err ->
                                            errorMessage = err.message ?: "فشل إنشاء الحساب، يرجى المحاولة مرة أخرى"
                                        }
                                    )
                                }
                            },
                            enabled = !isLoading,
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
                                Text("إنشاء حساب", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }

                        if (onNavigateToLogin != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            TextButton(
                                onClick = onNavigateToLogin,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "لديك حساب بالفعل؟ تسجيل الدخول",
                                    color = GoldSoft,
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
