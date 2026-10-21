package com.elhajri.noor.community
import com.elhajri.noor.settings.t

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elhajri.noor.theme.GlassRecipe
import com.elhajri.noor.ui.AntiqueGoldDeep
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.TextMain
import com.elhajri.noor.ui.noorGlassCard
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
                    error = t("بريد إلكتروني غير صالح", "Invalid email address")
                    loading = false
                    return@launch
                }
                val res = CommunityApi.getProfileByEmail(e)
                res.onSuccess { friend ->
                    if (friend == null) {
                        error = t("لا يوجد مستخدم بهذا البريد", "No user found with this email")
                    } else if (friend.handle == currentUser.handle) {
                        error = t("لا يمكنك محادثة نفسك", "You cannot chat with yourself")
                    } else {
                        val createRes = CommunityApi.createConversation(
                            name = friend.displayName,
                            type = "direct",
                            memberHandles = listOf(currentUser.handle, friend.handle),
                            memberNames = listOf(currentUser.name, friend.displayName),
                            memberAvatars = listOf(currentUser.avatar, friend.avatarUrl)
                        )
                        createRes.onSuccess { conv -> onCreated(conv) }
                            .onFailure { error = t("تعذر إنشاء المحادثة", "Could not create conversation") }
                    }
                }.onFailure {
                    error = t("تعذر البحث عن المستخدم", "Could not search for user")
                }
            } else {
                val h = handleInput.trim().lowercase().removePrefix("@")
                if (h.length < 3) {
                    error = t("معرف غير صالح", "Invalid ID")
                    loading = false
                    return@launch
                }
                if (h == currentUser.handle) {
                    error = t("لا يمكنك محادثة نفسك", "You cannot chat with yourself")
                    loading = false
                    return@launch
                }
                val res = CommunityApi.getProfileByHandle(h)
                res.onSuccess { friend ->
                    if (friend == null) {
                        error = t("لا يوجد مستخدم بهذا المعرف", "No user found with this ID")
                    } else {
                        val createRes = CommunityApi.createConversation(
                            name = friend.displayName,
                            type = "direct",
                            memberHandles = listOf(currentUser.handle, friend.handle),
                            memberNames = listOf(currentUser.name, friend.displayName),
                            memberAvatars = listOf(currentUser.avatar, friend.avatarUrl)
                        )
                        createRes.onSuccess { conv -> onCreated(conv) }
                            .onFailure { error = t("تعذر إنشاء المحادثة", "Could not create conversation") }
                    }
                }.onFailure {
                    error = t("تعذر البحث عن المستخدم", "Could not search for user")
                }
            }
            loading = false
        }
    }

    fun createGroup() {
        val name = groupName.trim()
        if (name.isEmpty()) {
            error = t("أدخل اسم المجموعة", "Enter group name")
            return
        }
        if (selectedContacts.isEmpty()) {
            error = t("اختر عضواً واحداً على الأقل", "Select at least one member")
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
                .onFailure { error = t("تعذر إنشاء المجموعة", "Could not create group") }

            loading = false
        }
    }

    AlertDialog(
        onDismissRequest = onClose,
        modifier = Modifier.fillMaxWidth(0.92f),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .noorGlassCard(cornerRadius = 22.dp)
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
                        text = t("محادثة جديدة", "New Conversation"),
                        color = Gold,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = t("إغلاق", "Close"), tint = GoldSoft.copy(alpha = 0.8f))
                    }
                }

                // Mode Tabs (Direct vs Group)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (mode == "direct") Brush.horizontalGradient(listOf(GoldSoft, Gold))
                                else Brush.horizontalGradient(listOf(NavyCard, NavyCard))
                            )
                            .border(
                                1.dp,
                                if (mode == "direct") GlassRecipe.borderGradient else BorderStroke(1.dp, Gold.copy(alpha = 0.2f)).brush,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { mode = "direct"; error = "" },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, tint = if (mode == "direct") Navy else Gold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "محادثة خاصة", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (mode == "direct") Navy else TextMain)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (mode == "group") Brush.horizontalGradient(listOf(GoldSoft, Gold))
                                else Brush.horizontalGradient(listOf(NavyCard, NavyCard))
                            )
                            .border(
                                1.dp,
                                if (mode == "group") GlassRecipe.borderGradient else BorderStroke(1.dp, Gold.copy(alpha = 0.2f)).brush,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { mode = "group"; error = "" },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Icon(Icons.Default.GroupAdd, contentDescription = null, tint = if (mode == "group") Navy else Gold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "مجموعة جديدة", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (mode == "group") Navy else TextMain)
                        }
                    }
                }

                if (mode == "direct") {
                    // Search mode filter chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = searchMode == "handle",
                            onClick = { searchMode = "handle"; error = "" },
                            label = { Text("بواسطة المعرف", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Gold.copy(alpha = 0.25f),
                                selectedLabelColor = Gold
                            )
                        )
                        FilterChip(
                            selected = searchMode == "email",
                            onClick = { searchMode = "email"; error = "" },
                            label = { Text("بواسطة البريد", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Gold.copy(alpha = 0.25f),
                                selectedLabelColor = Gold
                            )
                        )
                    }

                    if (searchMode == "handle") {
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
                                Text("@", color = Gold, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(6.dp))
                                TextField(
                                    value = handleInput,
                                    onValueChange = { handleInput = it.replace(" ", "").lowercase(); error = "" },
                                    placeholder = { Text("أدخل معرف الصديق...", color = TextMain.copy(alpha = 0.4f), fontSize = 12.sp) },
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
                    } else {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = NavyCard,
                            border = BorderStroke(1.dp, Gold.copy(alpha = 0.3f))
                        ) {
                            TextField(
                                value = emailInput,
                                onValueChange = { emailInput = it; error = "" },
                                placeholder = { Text("أدخل البريد الإلكتروني للصديق...", color = TextMain.copy(alpha = 0.4f), fontSize = 12.sp) },
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

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Brush.horizontalGradient(listOf(GoldSoft, Gold, AntiqueGoldDeep)))
                            .clickable(enabled = !loading) { createDirect() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (loading) {
                            CircularProgressIndicator(color = Navy, modifier = Modifier.size(20.dp))
                        } else {
                            Text("بدء المحادثة", color = Navy, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // Group mode
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = NavyCard,
                        border = BorderStroke(1.dp, Gold.copy(alpha = 0.3f))
                    ) {
                        TextField(
                            value = groupName,
                            onValueChange = { groupName = it; error = "" },
                            placeholder = { Text("اسم المجموعة...", color = TextMain.copy(alpha = 0.4f), fontSize = 12.sp) },
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

                    // Add member by handle directly
                    Text("أضف عضو بالمعرف:", color = GoldSoft, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                            Text("@", color = Gold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(6.dp))
                            TextField(
                                value = newHandleInput,
                                onValueChange = { newHandleInput = it.replace(" ", "").lowercase() },
                                placeholder = { Text("معرف الصديق لأضافته...", color = TextMain.copy(alpha = 0.4f), fontSize = 12.sp) },
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
                            IconButton(
                                onClick = {
                                    val clean = newHandleInput.trim().lowercase().removePrefix("@")
                                    if (clean.length >= 3 && clean != currentUser.handle) {
                                        selectedContacts = selectedContacts + clean
                                        newHandleInput = ""
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = t("إضافة", "Add"), tint = Gold)
                            }
                        }
                    }

                    if (contacts.isNotEmpty()) {
                        Text("أو اختر من أصدقائك السابقين:", color = GoldSoft, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 160.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(contacts, key = { it.handle }) { c ->
                                val isSelected = selectedContacts.contains(c.handle)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) Gold.copy(alpha = 0.2f) else NavyCard)
                                        .border(1.dp, if (isSelected) Gold else Color.Transparent, RoundedCornerShape(10.dp))
                                        .clickable {
                                            selectedContacts = if (isSelected) selectedContacts - c.handle else selectedContacts + c.handle
                                        }
                                        .padding(8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column {
                                            Text(c.name, color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Text("@${c.handle}", color = GoldSoft, fontSize = 10.sp)
                                        }
                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = {
                                                selectedContacts = if (it) selectedContacts + c.handle else selectedContacts - c.handle
                                            },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = Gold,
                                                uncheckedColor = Gold.copy(alpha = 0.4f)
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Brush.horizontalGradient(listOf(GoldSoft, Gold, AntiqueGoldDeep)))
                            .clickable(enabled = !loading) { createGroup() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (loading) {
                            CircularProgressIndicator(color = Navy, modifier = Modifier.size(20.dp))
                        } else {
                            Text("إنشاء المجموعة", color = Navy, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (error.isNotEmpty()) {
                    Text(error, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
