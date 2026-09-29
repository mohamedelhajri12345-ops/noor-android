package com.elhajri.noor.community

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.TextMain
import kotlinx.coroutines.launch

data class ContactItem(
    val handle: String,
    val name: String,
    val avatar: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewChatDialog(
    currentUser: CurrentUser,
    conversations: List<Conversation>,
    onClose: () -> Unit,
    onCreated: (Conversation) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    var mode by remember { mutableStateOf("direct") } // "direct" or "group"
    var searchMode by remember { mutableStateOf("handle") } // "handle" or "email"
    var handleInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }

    var groupName by remember { mutableStateOf("") }
    var selectedContacts by remember { mutableStateOf(setOf<String>()) }
    var newHandleInput by remember { mutableStateOf("") }

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    // Extract unique contacts from existing conversations
    val contacts = remember(conversations, currentUser.handle) {
        val map = LinkedHashMap<String, ContactItem>()
        conversations.forEach { conv ->
            conv.memberHandles.forEachIndexed { i, h ->
                if (h != currentUser.handle && !map.containsKey(h)) {
                    val name = conv.memberNames.getOrNull(i).takeIf { !it.isNullOrBlank() } ?: h
                    val avatar = conv.memberAvatars.getOrNull(i) ?: ""
                    map[h] = ContactItem(h, name, avatar)
                }
            }
        }
        map.values.toList()
    }

    fun createDirect() {
        error = ""
        loading = true
        coroutineScope.launch {
            if (searchMode == "email") {
                val e = emailInput.trim().lowercase()
                if (e.isBlank() || !e.contains("@")) {
                    error = "بريد إلكتروني غير صالح"
                    loading = false
                    return@launch
                }
                val res = CommunityApi.getProfileByEmail(e)
                res.onSuccess { friend ->
                    if (friend == null) {
                        error = "لا يوجد مستخدم بهذا البريد"
                    } else if (friend.handle == currentUser.handle) {
                        error = "لا يمكنك محادثة نفسك"
                    } else {
                        val createRes = CommunityApi.createConversation(
                            name = friend.displayName,
                            type = "direct",
                            memberHandles = listOf(currentUser.handle, friend.handle),
                            memberNames = listOf(currentUser.name, friend.displayName),
                            memberAvatars = listOf(currentUser.avatar, friend.avatarUrl)
                        )
                        createRes.onSuccess { conv -> onCreated(conv) }
                            .onFailure { error = "تعذر إنشاء المحادثة" }
                    }
                }.onFailure {
                    error = "تعذر البحث عن المستخدم"
                }
            } else {
                val h = handleInput.trim().lowercase().removePrefix("@")
                if (h.length < 3) {
                    error = "معرف غير صالح"
                    loading = false
                    return@launch
                }
                if (h == currentUser.handle) {
                    error = "لا يمكنك محادثة نفسك"
                    loading = false
                    return@launch
                }
                val res = CommunityApi.getProfileByHandle(h)
                res.onSuccess { friend ->
                    if (friend == null) {
                        error = "لا يوجد مستخدم بهذا المعرف"
                    } else {
                        val createRes = CommunityApi.createConversation(
                            name = friend.displayName,
                            type = "direct",
                            memberHandles = listOf(currentUser.handle, friend.handle),
                            memberNames = listOf(currentUser.name, friend.displayName),
                            memberAvatars = listOf(currentUser.avatar, friend.avatarUrl)
                        )
                        createRes.onSuccess { conv -> onCreated(conv) }
                            .onFailure { error = "تعذر إنشاء المحادثة" }
                    }
                }.onFailure {
                    error = "تعذر البحث عن المستخدم"
                }
            }
            loading = false
        }
    }

    fun createGroup() {
        val name = groupName.trim()
        if (name.isEmpty()) {
            error = "أدخل اسم المجموعة"
            return
        }
        if (selectedContacts.isEmpty()) {
            error = "اختر عضواً واحداً على الأقل"
            return
        }
        error = ""
        loading = true
        coroutineScope.launch {
            val handlesList = selectedContacts.toList()
            val namesList = mutableListOf(currentUser.name)
            val avatarsList = mutableListOf(currentUser.avatar)
            val allHandles = mutableListOf(currentUser.handle).apply { addAll(handlesList) }

            for (h in handlesList) {
                val profRes = CommunityApi.getProfileByHandle(h)
                val prof = profRes.getOrNull()
                if (prof != null) {
                    namesList.add(prof.displayName)
                    avatarsList.add(prof.avatarUrl)
                } else {
                    namesList.add(h)
                    avatarsList.add("")
                }
            }

            val createRes = CommunityApi.createConversation(
                name = name,
                type = "group",
                memberHandles = allHandles,
                memberNames = namesList,
                memberAvatars = avatarsList
            )
            createRes.onSuccess { conv -> onCreated(conv) }
                .onFailure { error = "تعذر إنشاء المجموعة" }

            loading = false
        }
    }

    AlertDialog(
        onDismissRequest = onClose,
        modifier = Modifier.fillMaxWidth(0.92f),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = NavyCard,
            border = BorderStroke(1.dp, Gold.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "محادثة جديدة",
                        color = Gold,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = TextMain.copy(alpha = 0.6f))
                    }
                }

                // Mode Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { mode = "direct"; error = "" },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (mode == "direct") Gold else Color(0xFF0A0F1A),
                            contentColor = if (mode == "direct") Navy else TextMain
                        )
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "محادثة خاصة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { mode = "group"; error = "" },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (mode == "group") Gold else Color(0xFF0A0F1A),
                            contentColor = if (mode == "group") Navy else TextMain
                        )
                    ) {
                        Icon(Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "مجموعة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (mode == "direct") {
                    // Search toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = searchMode == "handle",
                            onClick = { searchMode = "handle"; error = "" },
                            label = { Text("بالمعرف", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Gold.copy(alpha = 0.2f),
                                selectedLabelColor = Gold
                            )
                        )
                        FilterChip(
                            selected = searchMode == "email",
                            onClick = { searchMode = "email"; error = "" },
                            label = { Text("بالبريد الإلكتروني", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Gold.copy(alpha = 0.2f),
                                selectedLabelColor = Gold
                            )
                        )
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0A0F1A),
                        border = BorderStroke(1.dp, Gold.copy(alpha = 0.2f))
                    ) {
                        if (searchMode == "email") {
                            TextField(
                                value = emailInput,
                                onValueChange = { emailInput = it },
                                placeholder = { Text("أدخل البريد الإلكتروني...", color = TextMain.copy(alpha = 0.3f), fontSize = 12.sp) },
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
                        } else {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("@", color = Gold, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(4.dp))
                                TextField(
                                    value = handleInput,
                                    onValueChange = { handleInput = it.replace(" ", "").lowercase() },
                                    placeholder = { Text("المعرف...", color = TextMain.copy(alpha = 0.3f), fontSize = 12.sp) },
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
                            }
                        }
                    }

                    Button(
                        onClick = { createDirect() },
                        enabled = !loading,
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy)
                    ) {
                        if (loading) {
                            CircularProgressIndicator(color = Navy, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text("بدء المحادثة", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // Group mode
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0A0F1A),
                        border = BorderStroke(1.dp, Gold.copy(alpha = 0.2f))
                    ) {
                        TextField(
                            value = groupName,
                            onValueChange = { groupName = it },
                            placeholder = { Text("اسم المجموعة...", color = TextMain.copy(alpha = 0.3f), fontSize = 12.sp) },
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

                    // Add custom handle field
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF0A0F1A),
                            border = BorderStroke(1.dp, Gold.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("@", color = Gold, fontSize = 12.sp)
                                TextField(
                                    value = newHandleInput,
                                    onValueChange = { newHandleInput = it.replace(" ", "").lowercase() },
                                    placeholder = { Text("إضافة معرف عضو", color = TextMain.copy(alpha = 0.3f), fontSize = 11.sp) },
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
                            }
                        }

                        IconButton(
                            onClick = {
                                val clean = newHandleInput.trim().lowercase().removePrefix("@")
                                if (clean.length >= 3 && clean != currentUser.handle) {
                                    selectedContacts = selectedContacts + clean
                                    newHandleInput = ""
                                }
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Gold)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "إضافة", tint = Navy, modifier = Modifier.size(18.dp))
                        }
                    }

                    // Selected contact badges
                    if (selectedContacts.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 60.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            selectedContacts.forEach { handle ->
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Gold.copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, Gold.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text("@$handle", color = Gold, fontSize = 11.sp)
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "إزالة",
                                            tint = Gold,
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clickable { selectedContacts = selectedContacts - handle }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Contacts list checkable
                    if (contacts.isNotEmpty()) {
                        Text("الأصدقاء:", color = TextMain.copy(alpha = 0.7f), fontSize = 11.sp)
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 140.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(contacts) { contact ->
                                val isSelected = selectedContacts.contains(contact.handle)
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedContacts = if (isSelected) selectedContacts - contact.handle
                                            else selectedContacts + contact.handle
                                        },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) Gold.copy(alpha = 0.15f) else Color.Transparent
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = { checked ->
                                                selectedContacts = if (checked) selectedContacts + contact.handle
                                                else selectedContacts - contact.handle
                                            },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = Gold,
                                                uncheckedColor = Gold.copy(alpha = 0.4f)
                                            )
                                        )
                                        Text(contact.name, color = TextMain, fontSize = 12.sp)
                                        Text("@${contact.handle}", color = Gold, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { createGroup() },
                        enabled = !loading,
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Navy)
                    ) {
                        if (loading) {
                            CircularProgressIndicator(color = Navy, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text("إنشاء المجموعة", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (error.isNotEmpty()) {
                    Text(error, color = Color.Red, fontSize = 11.sp)
                }
            }
        }
    }
}
