package com.elhajri.noor.community
import com.elhajri.noor.settings.t

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.elhajri.noor.theme.GlassRecipe
import com.elhajri.noor.ui.AmiriFamily
import com.elhajri.noor.ui.AntiqueGoldDeep
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.TextMain
import com.elhajri.noor.ui.noorGlassCard
import com.elhajri.noor.ui.themeScreenBackground
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
            error = t("المعرف يجب أن يكون ٣ أحرف على الأقل", "Username must be at least 3 characters")
            return
        }
        if (cleanName.isEmpty()) {
            error = t("أدخل اسمك", "Enter your name")
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
                error = err.message ?: t("تعذر إنشاء الحساب، حاول مرة أخرى", "Account creation failed, try again")
            }
            loading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .themeScreenBackground()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .noorGlassCard(cornerRadius = 22.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Group,
                contentDescription = null,
                tint = Gold,
                modifier = Modifier.size(34.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = t("التسجيل في المجتمع", "Community Registration"),
            color = Gold,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = AmiriFamily
        )

        Text(
            text = t("أنشئ معرفك الخاص للمشاركة في مجتمع نور", "Create your profile to join the NOOR community"),
            color = GoldSoft.copy(alpha = 0.85f),
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .noorGlassCard(cornerRadius = 22.dp)
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
                            .background(NavyCard)
                            .border(1.5.dp, GlassRecipe.borderGradient, CircleShape)
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
                                contentDescription = t("صورة شخصية", "Profile Picture"),
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = t("اختر صورة", "Choose Image"),
                                tint = Gold,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Text(
                        text = t("صورة شخصية (اختياري)", "Profile Picture (Optional)"),
                        color = GoldSoft.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                // Handle Input
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = t("المعرف (اسم فريد لك)", "Username (Unique Handle)"),
                        color = GoldSoft,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = NavyCard,
                        border = BorderStroke(1.dp, Gold.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "@", color = Gold, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            TextField(
                                value = handle,
                                onValueChange = {
                                    val clean = it.replace(" ", "").lowercase()
                                    handle = clean
                                    checkHandle(clean)
                                },
                                placeholder = { Text("مثال: ahmed", color = TextMain.copy(alpha = 0.35f), fontSize = 13.sp) },
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
                                CircularProgressIndicator(color = Gold, modifier = Modifier.size(16.dp))
                            } else if (handleAvailable == true) {
                                Icon(Icons.Default.Check, contentDescription = t("متاح", "Available"), tint = Color(0xFF4CAF50), modifier = Modifier.size(18.dp))
                            } else if (handleAvailable == false) {
                                Icon(Icons.Default.Close, contentDescription = t("غير متاح", "Unavailable"), tint = Color.Red, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                    if (handleAvailable == false) {
                        Text(
                            text = t("هذا المعرف مستخدم بالفعل", "Handle already taken"),
                            color = Color.Red,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 2.dp, start = 4.dp)
                        )
                    }
                }

                // Display Name Input
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = t("الاسم الظاهر", "Display Name"),
                        color = GoldSoft,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = NavyCard,
                        border = BorderStroke(1.dp, Gold.copy(alpha = 0.3f))
                    ) {
                        TextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = { Text("مثال: أحمد محمد", color = TextMain.copy(alpha = 0.35f), fontSize = 13.sp) },
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

                if (error.isNotEmpty()) {
                    Text(text = error, color = Color.Red, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.horizontalGradient(listOf(GoldSoft, Gold, AntiqueGoldDeep))
                        )
                        .clickable(enabled = !loading && handleAvailable != false) { register() },
                    contentAlignment = Alignment.Center
                ) {
                    if (loading) {
                        CircularProgressIndicator(color = Navy, modifier = Modifier.size(22.dp))
                    } else {
                        Text(
                            text = t("إتمام التسجيل", "Complete Registration"),
                            color = Navy,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
