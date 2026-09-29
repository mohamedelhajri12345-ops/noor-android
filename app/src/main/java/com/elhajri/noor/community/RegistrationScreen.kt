package com.elhajri.noor.community

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.elhajri.noor.ui.AmiriFamily
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.TextMain
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    userEmail: String,
    onRegistered: (CommunityProfile) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var handle by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var avatar by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var uploadingAvatar by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var checkingHandle by remember { mutableStateOf(false) }
    var handleAvailable by remember { mutableStateOf<Boolean?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            uploadingAvatar = true
            coroutineScope.launch {
                try {
                    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    if (bytes != null) {
                        val result = CommunityApi.uploadFile(bytes, "avatar_${System.currentTimeMillis()}.jpg", "image/jpeg")
                        result.onSuccess { url -> avatar = url }
                    }
                } catch (_: Exception) {}
                uploadingAvatar = false
            }
        }
    }

    fun checkHandle(h: String) {
        val clean = h.replace(" ", "").lowercase()
        if (clean.length < 3) {
            handleAvailable = null
            return
        }
        checkingHandle = true
        coroutineScope.launch {
            val res = CommunityApi.checkHandleAvailable(clean)
            res.onSuccess { available ->
                handleAvailable = available
            }.onFailure {
                handleAvailable = null
            }
            checkingHandle = false
        }
    }

    fun register() {
        val cleanHandle = handle.replace(" ", "").lowercase().trim()
        val cleanName = name.trim()
        if (cleanHandle.length < 3) {
            error = "المعرف يجب أن يكون ٣ أحرف على الأقل"
            return
        }
        if (cleanName.isEmpty()) {
            error = "أدخل اسمك"
            return
        }
        loading = true
        error = ""
        coroutineScope.launch {
            val res = CommunityApi.createProfile(
                handle = cleanHandle,
                displayName = cleanName,
                avatarUrl = avatar,
                userEmail = userEmail
            )
            res.onSuccess { profile ->
                val sp = context.getSharedPreferences("noor_community_prefs", Context.MODE_PRIVATE)
                sp.edit()
                    .putString("nur_community_handle", profile.handle)
                    .putString("nur_community_name", profile.displayName)
                    .putString("nur_community_avatar", profile.avatarUrl)
                    .apply()
                onRegistered(profile)
            }.onFailure { err ->
                error = err.message ?: "تعذر إنشاء الحساب، حاول مرة أخرى"
            }
            loading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Gold.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Group,
                contentDescription = null,
                tint = Gold,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "التسجيل في المجتمع",
            color = Gold,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = AmiriFamily
        )

        Text(
            text = "أنشئ معرفك الخاص للمشاركة",
            color = TextMain.copy(alpha = 0.7f),
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = NavyCard,
            border = BorderStroke(1.dp, Gold.copy(alpha = 0.25f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Avatar Picker
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Gold.copy(alpha = 0.15f))
                            .clickable {
                                imagePickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (uploadingAvatar) {
                            CircularProgressIndicator(color = Gold, modifier = Modifier.size(24.dp))
                        } else if (avatar.isNotEmpty()) {
                            AsyncImage(
                                model = avatar,
                                contentDescription = "صورة شخصية",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "اختر صورة",
                                tint = Gold,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Text(
                        text = "صورة شخصية (اختياري)",
                        color = TextMain.copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                // Handle Input
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "المعرف (اسم فريد لك)",
                        color = TextMain.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0A0F1A),
                        border = BorderStroke(1.dp, Gold.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "@", color = Gold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            TextField(
                                value = handle,
                                onValueChange = {
                                    val clean = it.replace(" ", "").lowercase()
                                    handle = clean
                                    checkHandle(clean)
                                },
                                placeholder = { Text("مثال: ahmed", color = TextMain.copy(alpha = 0.3f), fontSize = 13.sp) },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = TextMain,
                                    unfocusedTextColor = TextMain
                                ),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )

                            if (checkingHandle) {
                                CircularProgressIndicator(color = Gold, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else if (handleAvailable == true) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.Green, modifier = Modifier.size(18.dp))
                            } else if (handleAvailable == false) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = Color.Red, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                    Text(
                        text = "شارك هذا المعرف مع أصدقائك ليبدؤوا محادثة معك",
                        color = TextMain.copy(alpha = 0.5f),
                        fontSize = 10.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Display Name Input
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "الاسم المعروض",
                        color = TextMain.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0A0F1A),
                        border = BorderStroke(1.dp, Gold.copy(alpha = 0.2f))
                    ) {
                        TextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = { Text("اسمك", color = TextMain.copy(alpha = 0.3f), fontSize = 13.sp) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = TextMain,
                                unfocusedTextColor = TextMain
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                Button(
                    onClick = { register() },
                    enabled = !loading && handleAvailable == true && name.trim().isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Gold,
                        contentColor = Navy,
                        disabledContainerColor = Gold.copy(alpha = 0.4f),
                        disabledContentColor = Navy.copy(alpha = 0.5f)
                    )
                ) {
                    if (loading) {
                        CircularProgressIndicator(color = Navy, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text(text = "إنشاء الحساب", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (error.isNotEmpty()) {
                    Text(
                        text = error,
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
