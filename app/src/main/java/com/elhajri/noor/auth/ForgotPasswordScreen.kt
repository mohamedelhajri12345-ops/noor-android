package com.elhajri.noor.auth

import com.elhajri.noor.ui.themeScreenBackground
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.noorGlassCard
import kotlinx.coroutines.launch
import com.elhajri.noor.ui.NoorGradients
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isSubmitted by remember { mutableStateOf(false) }
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
                            imageVector = Icons.Filled.Mail,
                            contentDescription = null,
                            tint = Navy,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "نسيت كلمة المرور؟",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Gold,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "أدخل بريدك الإلكتروني وسنرسل لك رابطاً لإعادة التعيين",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GoldSoft.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth().noorGlassCard(cornerRadius = 24.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        if (isSubmitted) {
                            Text(
                                text = "إذا كان هناك حساب مرتبط بهذا البريد، فستتلقى رابطاً لإعادة تعيين كلمة المرور قريباً.",
                                color = GoldSoft,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
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
                                value = email,
                                onValueChange = { email = it; errorMessage = null },
                                label = { Text("البريد الإلكتروني", color = GoldSoft) },
                                leadingIcon = {
                                    Icon(Icons.Filled.Mail, contentDescription = null, tint = Gold)
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
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
                                    if (email.isBlank()) {
                                        errorMessage = "يرجى إدخال البريد الإلكتروني"
                                        return@Button
                                    }
                                    isLoading = true
                                    errorMessage = null
                                    scope.launch {
                                        Base44Auth.forgotPassword(email)
                                        isLoading = false
                                        isSubmitted = true
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
                                    Text("إرسال رابط إعادة التعيين", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
