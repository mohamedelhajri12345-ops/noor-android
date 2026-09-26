package com.elhajri.noor.community

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.NavyLight
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun CommunityScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val sp = remember { context.getSharedPreferences("noor_prefs", Context.MODE_PRIVATE) }
    
    var registeredName by remember { mutableStateOf(sp.getString("community_name", "") ?: "") }
    var registeredHandle by remember { mutableStateOf(sp.getString("community_handle", "") ?: "") }
    var registeredCity by remember { mutableStateOf(sp.getString("community_city", "") ?: "") }
    var registeredBio by remember { mutableStateOf(sp.getString("community_bio", "") ?: "") }

    var selectedProfileId by remember { mutableStateOf<String?>(null) }
    var selectedProfileName by remember { mutableStateOf<String?>(null) }

    var profiles by remember { mutableStateOf<List<CommunityProfile>>(emptyList()) }
    var conversations by remember { mutableStateOf<List<Conversation>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var showNewChatDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Registration Form State
    var regNameInput by remember { mutableStateOf("") }
    var regCityInput by remember { mutableStateOf("") }
    var regBioInput by remember { mutableStateOf("") }
    var regHandleInput by remember { mutableStateOf("") }
    var isRegistering by remember { mutableStateOf(false) }
    var regError by remember { mutableStateOf<String?>(null) }

    val isRegistered = registeredName.isNotBlank()

    // Poll-based refresh every 5s
    LaunchedEffect(isRegistered) {
        if (!isRegistered) return@LaunchedEffect
        while (isActive) {
            isLoading = profiles.isEmpty() && conversations.isEmpty()
            try {
                val fetchedProfiles = CommunityApi.listProfiles()
                val fetchedConvs = CommunityApi.listConversations()
                profiles = fetchedProfiles
                conversations = fetchedConvs
            } catch (_: Exception) { }
            isLoading = false
            delay(5000)
        }
    }

    if (selectedProfileId != null && selectedProfileName != null) {
        ChatView(
            profileId = selectedProfileId!!,
            profileName = selectedProfileName!!,
            onBack = {
                selectedProfileId = null
                selectedProfileName = null
            }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy)
            .padding(16.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "رجوع",
                    tint = Gold
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "مجتمع نور",
                    color = Gold,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "تواصل مع إخوانك في الله",
                    color = GoldSoft.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }
            if (isRegistered) {
                IconButton(onClick = { showNewChatDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "محادثة جديدة",
                        tint = Gold
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        if (!isRegistered) {
            // Registration Form
            Card(
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Gold.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Gold,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "التسجيل في مجتمع نور",
                        color = Gold,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "أنشئ حسابك للمشاركة والتواصل مع المصلين",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = regNameInput,
                        onValueChange = { regNameInput = it },
                        label = { Text("الاسم المعروض", color = GoldSoft) },
                        placeholder = { Text("مثال: أحمد عبد الله") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Gold,
                            unfocusedBorderColor = GoldSoft.copy(alpha = 0.4f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = regHandleInput,
                        onValueChange = { regHandleInput = it.lowercase().replace(" ", "_") },
                        label = { Text("المعرف الخاص (@username)", color = GoldSoft) },
                        placeholder = { Text("ahmed_123") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Gold,
                            unfocusedBorderColor = GoldSoft.copy(alpha = 0.4f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = regCityInput,
                        onValueChange = { regCityInput = it },
                        label = { Text("المدينة / البلد", color = GoldSoft) },
                        placeholder = { Text("مثال: الرباط، المغرب") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Gold,
                            unfocusedBorderColor = GoldSoft.copy(alpha = 0.4f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = regBioInput,
                        onValueChange = { regBioInput = it },
                        label = { Text("نبذة بسيطة (اختياري)", color = GoldSoft) },
                        placeholder = { Text("محب للقرآن وتلاوته") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Gold,
                            unfocusedBorderColor = GoldSoft.copy(alpha = 0.4f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    if (regError != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(regError!!, color = Color.Red, fontSize = 12.sp)
                    }

                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (regNameInput.isBlank()) {
                                regError = "الرجاء إدخال الاسم المعروض"
                                return@Button
                            }
                            isRegistering = true
                            regError = null
                        },
                        enabled = !isRegistering,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Gold)
                    ) {
                        if (isRegistering) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Navy,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("إنشاء الحساب وبدء التواصل", color = Navy, fontWeight = FontWeight.Bold)
                        }
                    }

                    LaunchedEffect(isRegistering) {
                        if (isRegistering) {
                            val prof = CommunityApi.registerProfile(
                                fullName = regNameInput.trim(),
                                city = regCityInput.trim(),
                                bio = regBioInput.trim(),
                                handle = regHandleInput.trim()
                            )
                            sp.edit()
                                .putString("community_name", prof.displayName)
                                .putString("community_handle", prof.handle)
                                .putString("community_city", prof.city ?: "")
                                .putString("community_bio", prof.bio ?: "")
                                .apply()
                            registeredName = prof.displayName
                            registeredHandle = prof.handle
                            registeredCity = prof.city ?: ""
                            registeredBio = prof.bio ?: ""
                            isRegistering = false
                        }
                    }
                }
            }
        } else {
            // Registered View - Conversations & Profiles List
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("بحث في الأعضاء والمحادثات...", color = GoldSoft.copy(alpha = 0.6f)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Gold) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Gold,
                    unfocusedBorderColor = GoldSoft.copy(alpha = 0.3f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(Modifier.height(12.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Gold)
                }
            } else {
                val filteredProfiles = profiles.filter {
                    it.displayName.contains(searchQuery, ignoreCase = true) ||
                            it.handle.contains(searchQuery, ignoreCase = true) ||
                            (it.city?.contains(searchQuery, ignoreCase = true) == true)
                }

                if (filteredProfiles.isEmpty() && conversations.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لا توجد محادثات أو أعضاء حالياً.\nاضغط (+) لبدء محادثة جديدة.",
                            color = GoldSoft,
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (conversations.isNotEmpty()) {
                            item {
                                Text(
                                    text = "المحادثات الأخيرة",
                                    color = Gold,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                            items(conversations) { conv ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = NavyCard),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedProfileId = conv.id
                                            selectedProfileName = conv.name
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .background(NavyLight),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = conv.name.take(1),
                                                color = Gold,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp
                                            )
                                        }
                                        Spacer(Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = conv.name,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            )
                                            if (conv.lastMessage.isNotBlank()) {
                                                Text(
                                                    text = conv.lastMessage,
                                                    color = GoldSoft.copy(alpha = 0.7f),
                                                    fontSize = 13.sp,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                text = "أعضاء المجتمع",
                                color = Gold,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                            )
                        }

                        items(filteredProfiles) { prof ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = NavyCard),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedProfileId = prof.id.ifEmpty { prof.handle }
                                        selectedProfileName = prof.displayName
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(Gold.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = prof.displayName.take(1).ifEmpty { "ع" },
                                            color = Gold,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = prof.displayName,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        if (!prof.city.isNullOrBlank()) {
                                            Text(
                                                text = "📍 ${prof.city}",
                                                color = GoldSoft.copy(alpha = 0.7f),
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                    Text(
                                        text = "محادثة",
                                        color = Gold,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // New Chat Dialog
    if (showNewChatDialog) {
        var newChatHandle by remember { mutableStateOf("") }
        var dialogError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showNewChatDialog = false },
            containerColor = NavyCard,
            title = { Text("بدء محادثة جديدة", color = Gold, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("أدخل معرف المستخدم (@username) أو اسم العضو للبدء:", color = Color.White, fontSize = 13.sp)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newChatHandle,
                        onValueChange = { newChatHandle = it },
                        placeholder = { Text("أدخل المعرف أو الاسم...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Gold,
                            unfocusedBorderColor = GoldSoft.copy(alpha = 0.4f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    if (dialogError != null) {
                        Spacer(Modifier.height(6.dp))
                        Text(dialogError!!, color = Color.Red, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newChatHandle.isBlank()) {
                            dialogError = "الرجاء إدخال المعرف"
                        } else {
                            showNewChatDialog = false
                            selectedProfileId = newChatHandle.trim().lowercase().replace("@", "")
                            selectedProfileName = newChatHandle.trim()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Gold)
                ) {
                    Text("محادثة", color = Navy, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewChatDialog = false }) {
                    Text("إلغاء", color = GoldSoft)
                }
            }
        )
    }
}
