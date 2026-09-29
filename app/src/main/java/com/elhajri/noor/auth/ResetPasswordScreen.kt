package com.elhajri.noor.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import kotlinx.coroutines.launch
import com.elhajri.noor.ui.NoorGradients
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var token by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = Color(0xFF070B14),
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = Gold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF070B14))
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(NoorGradients.ScreenBackground),
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
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = Navy,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "إعادة تعيين كلمة المرور",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Gold,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "أدخل رمز التحقق وكلمة المرور الجديدة",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GoldSoft.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = NavyCard),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        if (isSuccess) {
                            Text(
                                text = "تم تغيير كلمة المرور بنجاح! يمكنك الآن تسجيل الدخول بكلمة المرور الجديدة.",
                                color = GoldSoft,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onBack,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy)
                            ) {
                                Text("الانتقال لتسجيل الدخول", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = token,
                                onValueChange = { token = it; errorMessage = null },
                                label = { Text("رمز التحقق (Token)", color = GoldSoft) },
                                leadingIcon = {
                                    Icon(Icons.Filled.Key, contentDescription = null, tint = Gold)
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
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

                            OutlinedTextField(
                                value = newPassword,
                                onValueChange = { newPassword = it; errorMessage = null },
                                label = { Text("كلمة المرور الجديدة", color = GoldSoft) },
                                leadingIcon = {
                                    Icon(Icons.Filled.Lock, contentDescription = null, tint = Gold)
                                },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
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

                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it; errorMessage = null },
                                label = { Text("تأكيد كلمة المرور الجديدة", color = GoldSoft) },
                                leadingIcon = {
                                    Icon(Icons.Filled.Lock, contentDescription = null, tint = Gold)
                                },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
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
                                    if (token.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
                                        errorMessage = "يرجى ملء جميع الحقول"
                                        return@Button
                                    }
                                    if (newPassword != confirmPassword) {
                                        errorMessage = "كلمتا المرور غير متطابقتين"
                                        return@Button
                                    }
                                    isLoading = true
                                    errorMessage = null
                                    scope.launch {
                                        val res = Base44Auth.resetPassword(token, newPassword)
                                        isLoading = false
                                        res.fold(
                                            onSuccess = { isSuccess = true },
                                            onFailure = { err ->
                                                errorMessage = err.message ?: "فشل إعادة تعيين كلمة المرور"
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
                                    Text("تأكيد تغيير كلمة المرور", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                TextButton(onClick = onBack) {
                    Text("العودة إلى تسجيل الدخول", color = Gold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}
