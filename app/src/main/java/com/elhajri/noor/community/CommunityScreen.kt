package com.elhajri.noor.community

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.elhajri.noor.ui.Gold
import com.elhajri.noor.ui.GoldSoft
import com.elhajri.noor.ui.Navy
import com.elhajri.noor.ui.NavyCard
import com.elhajri.noor.ui.TextMain
import com.elhajri.noor.ui.noorGlassCard
import com.elhajri.noor.ui.themeScreenBackground
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed class AuthProfileState {
    object Checking : AuthProfileState()
    object NotLoggedIn : AuthProfileState()
    data class NeedsRegistration(val userEmail: String) : AuthProfileState()
    data class Ready(val profile: CommunityProfile) : AuthProfileState()
}

@Composable
fun CommunityScreen(
    onBack: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var state by remember { mutableStateOf<AuthProfileState>(AuthProfileState.Checking) }
    var conversations by remember { mutableStateOf<List<Conversation>>(emptyList()) }
    var selectedConv by remember { mutableStateOf<Conversation?>(null) }
    var showNewChat by remember { mutableStateOf(false) }

    fun loadConversations(myHandle: String) {
        coroutineScope.launch {
            val res = CommunityApi.getConversations(myHandle)
            res.onSuccess { list ->
                conversations = list
            }
        }
    }

    // Check user & profile
    LaunchedEffect(Unit) {
        val userRes = CommunityApi.getCurrentUser()
        userRes.onSuccess { user ->
            val profRes = CommunityApi.getProfileByEmail(user.email)
            profRes.onSuccess { profile ->
                if (profile != null) {
                    val sp = context.getSharedPreferences("noor_community_prefs", Context.MODE_PRIVATE)
                    sp.edit()
                        .putString("nur_community_handle", profile.handle)
                        .putString("nur_community_name", profile.displayName)
                        .putString("nur_community_avatar", profile.avatarUrl)
                        .apply()
                    state = AuthProfileState.Ready(profile)
                    loadConversations(profile.handle)
                } else {
                    state = AuthProfileState.NeedsRegistration(user.email)
                }
            }.onFailure {
                state = AuthProfileState.NeedsRegistration(user.email)
            }
        }.onFailure {
            state = AuthProfileState.NotLoggedIn
        }
    }

    // 4-second polling loop when profile is ready
    LaunchedEffect(state) {
        val currentState = state
        if (currentState is AuthProfileState.Ready) {
            val myHandle = currentState.profile.handle
            while (isActive) {
                delay(4000)
                val res = CommunityApi.getConversations(myHandle)
                res.onSuccess { list ->
                    conversations = list
                }
            }
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .themeScreenBackground(),
        color = Color.Transparent
    ) {
        when (val currentState = state) {
            is AuthProfileState.Checking -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Gold)
                }
            }

            is AuthProfileState.NotLoggedIn -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .noorGlassCard(cornerRadius = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = Gold,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "مجتمع القرآن الكريم",
                        color = Gold,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = AmiriFamily
                    )

                    Text(
                        text = "يجب تسجيل الدخول للمشاركة وتواصل مع باقي المسلمين",
                        color = GoldSoft.copy(alpha = 0.85f),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 6.dp, bottom = 28.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .height(50.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(GoldSoft, Gold, com.elhajri.noor.ui.AntiqueGoldDeep)
                                )
                            )
                            .clickable { onNavigateToLogin() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "تسجيل الدخول",
                            color = Navy,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            is AuthProfileState.NeedsRegistration -> {
                RegistrationScreen(
                    userEmail = currentState.userEmail,
                    onRegistered = { profile ->
                        state = AuthProfileState.Ready(profile)
                        loadConversations(profile.handle)
                    }
                )
            }

            is AuthProfileState.Ready -> {
                val profile = currentState.profile
                val currentUser = CurrentUser(
                    handle = profile.handle,
                    name = profile.displayName,
                    avatar = profile.avatarUrl
                )

                if (selectedConv != null) {
                    ChatView(
                        conversation = selectedConv!!,
                        currentUser = currentUser,
                        onBack = { selectedConv = null }
                    )
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Profile glass card header at top
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                                .noorGlassCard(cornerRadius = 20.dp)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(NavyCard)
                                    .border(1.5.dp, GlassRecipe.borderGradient, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (profile.avatarUrl.isNotEmpty()) {
                                    AsyncImage(
                                        model = profile.avatarUrl,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.linearGradient(listOf(GoldSoft, Gold))
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = profile.displayName.trim().take(1).ifEmpty { "ن" },
                                            color = Navy,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = profile.displayName,
                                    color = Gold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "اسم المعرف: @${profile.handle}",
                                    color = GoldSoft.copy(alpha = 0.85f),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        ConversationList(
                            conversations = conversations,
                            currentUser = currentUser,
                            onSelectConv = { conv -> selectedConv = conv },
                            onNewChat = { showNewChat = true }
                        )
                    }

                    if (showNewChat) {
                        NewChatDialog(
                            currentUser = currentUser,
                            conversations = conversations,
                            onClose = { showNewChat = false },
                            onCreated = { conv ->
                                showNewChat = false
                                selectedConv = conv
                                loadConversations(profile.handle)
                            }
                        )
                    }
                }
            }
        }
    }
}
