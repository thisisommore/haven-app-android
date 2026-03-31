package com.example.haven

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.haven.ui.pages.chat.ChatScreen
import com.example.haven.ui.pages.chat.ChatViewModel
import com.example.haven.ui.pages.codename.CodenamePage
import com.example.haven.ui.pages.home.HomeScreen
import com.example.haven.ui.pages.home.HomeViewModel
import com.example.haven.ui.pages.landing.LandingPage
import com.example.haven.ui.pages.password.PasswordPage
import com.example.haven.ui.views.logviewer.LogPage
import com.example.haven.xxdk.GeneratedIdentity
import com.example.haven.xxdk.XXDK
import com.example.haven.xxdk.XXDKStorage
import com.example.haven.xxdk.applyIdentity
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import android.util.Log

@Composable
internal fun HavenApp() {
    val context = LocalContext.current.applicationContext
    val appStorage = remember { XXDKStorage.getInstance(context) }
    val xxdk = remember { XXDK(context).apply { setAppStorage(appStorage) } }
    val scope = rememberCoroutineScope()
    val chatViewModel: ChatViewModel = viewModel(factory = ChatViewModel.Factory(context, xxdk))
    val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory(context))
    
    // Observe real messages from database
    val chatMessages by chatViewModel.messages.collectAsState(initial = emptyList())
    val inputText by chatViewModel.inputText.collectAsState()
    
    val allLogs = remember {
        listOf(
            "INFO App launched",
            "INFO Haven using real database",
            "DEBUG Home page with DB data",
            "INFO Chat page with DB messages",
        )
    }
    // For new users: start directly at password setup
    // For existing users: start directly at home (with loading indicator)
    // Use remember (not rememberSaveable) to always check isSetupComplete on app start
    var route by remember { 
        mutableStateOf(if (appStorage.isSetupComplete) Route.home else Route.password) 
    }
    
    // For returning users: auto-load cmix when home page is shown
    LaunchedEffect(route) {
        if (route == Route.home && appStorage.isSetupComplete && xxdk.cmix == null) {
            runCatching {
                xxdk.setAppStorage(appStorage)
                xxdk.loadCmix()
                xxdk.startNetworkFollower()
                val identity = xxdk.loadSavedPrivateIdentity()
                xxdk.loadClients(identity)
            }.onFailure {
                // If loading fails for existing user, don't reset isSetupComplete
                // Just log the error - user stays on home page
                Log.e("HavenApp", "Failed to load cmix for existing user: ${it.message}")
            }
        }
    }
    var chatId by rememberSaveable { mutableStateOf("") }

    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var codenames by remember { mutableStateOf<List<GeneratedIdentity>>(emptyList()) }
    var selectedCodename by rememberSaveable { mutableStateOf("") }
    var logFilter by rememberSaveable { mutableStateOf("ALL") }
    var logSearch by rememberSaveable { mutableStateOf("") }
    var passwordBusy by rememberSaveable { mutableStateOf(false) }
    var passwordError by rememberSaveable { mutableStateOf<String?>(null) }
    // NDF download state - starts when password page opens
    var ndfDeferred by remember { mutableStateOf<Deferred<ByteArray>?>(null) }
    var codenameBusy by rememberSaveable { mutableStateOf(false) }
    var codenameError by rememberSaveable { mutableStateOf<String?>(null) }
    val currentChatTitle by homeViewModel.filteredChats.collectAsState(initial = emptyList())
    val currentChatName = currentChatTitle.firstOrNull { it.id == chatId }?.title ?: "Chat"

    // Handle back button navigation
    BackHandler(enabled = route != Route.home && route != Route.password) {
        when (route) {
            Route.chat -> route = Route.home
            Route.codenameGenerator -> route = Route.password
            Route.landing -> route = Route.home
            Route.logViewer -> route = Route.home
            else -> { /* Let system handle back */ }
        }
    }

    AnimatedContent(
        targetState = route,
        transitionSpec = {
            // Home to Chat: slide in from right, slide out to left
            // Chat to Home: slide in from left, slide out to right
            val isForward = targetState == Route.chat && initialState == Route.home
            slideInHorizontally(
                animationSpec = tween(300),
                initialOffsetX = { if (isForward) it else -it }
            ) + fadeIn(animationSpec = tween(300)) togetherWith
            slideOutHorizontally(
                animationSpec = tween(300),
                targetOffsetX = { if (isForward) -it else it }
            ) + fadeOut(animationSpec = tween(300))
        },
        modifier = Modifier.fillMaxSize()
    ) { targetRoute ->
        when (targetRoute) {
            Route.landing -> {
                // Start network follower when landing page is shown and setup is complete
                LaunchedEffect(appStorage.isSetupComplete) {
                    if (appStorage.isSetupComplete && xxdk.cmix != null) {
                        runCatching {
                            xxdk.startNetworkFollower()
                            val identity = xxdk.loadSavedPrivateIdentity()
                            xxdk.loadClients(identity)
                        }.onFailure {
                            Log.e("HavenApp", "Failed to start network: ${it.message}")
                        }
                    }
                }
                
                LandingPage(
                    modifier = Modifier.fillMaxSize(),
                    status = xxdk.status,
                    statusPercentage = xxdk.statusPercentage,
                    isSetupComplete = appStorage.isSetupComplete,
                    onLoadingComplete = { route = Route.home },
                    appStorage = appStorage
                )
            }

            Route.password -> Page("Join the alpha", onBack = null) { p ->
                // Start NDF download as soon as password page opens
                LaunchedEffect(Unit) {
                    if (ndfDeferred == null) {
                        ndfDeferred = scope.async {
                            xxdk.downloadNdf()
                        }
                    }
                }
                
                PasswordPage(
                    modifier = Modifier.padding(p),
                    password = password,
                    confirm = confirm,
                    onPassword = { password = it },
                    onConfirm = { confirm = it },
                    onContinue = {
                        scope.launch {
                            passwordBusy = true
                            passwordError = null
                            runCatching {
                                appStorage.password = password
                                xxdk.setAppStorage(appStorage)
                                // Wait for NDF download to complete
                                val ndf = ndfDeferred?.await() ?: xxdk.downloadNdf()
                                xxdk.newCmix(ndf)
                                xxdk.loadCmix()
                                codenames = xxdk.generateIdentities(10)
                                require(codenames.isNotEmpty()) { "No identities generated" }
                                selectedCodename = ""
                                route = Route.codenameGenerator
                            }.onFailure {
                                passwordError = it.message ?: "Password setup failed"
                            }
                            passwordBusy = false
                        }
                    },
                    onImport = {
                        passwordError = "Import needs a real encrypted identity file. File selection is not wired yet."
                    },
                    onClearAll = {
                        scope.launch {
                            runCatching {
                                appStorage.clearAll()
                                // Reset all state
                                password = ""
                                confirm = ""
                                ndfDeferred = null
                                passwordError = null
                                // Re-trigger NDF download
                                ndfDeferred = scope.async {
                                    xxdk.downloadNdf()
                                }
                            }.onFailure {
                                Log.e("HavenApp", "Clear all failed: ${it.message}")
                            }
                        }
                    },
                    status = xxdk.status,
                    isLoading = passwordBusy,
                    error = passwordError,
                    showClearAll = ndfDeferred != null
                )
            }

            Route.codenameGenerator -> Page("Codename", { route = Route.password }) { p ->
                CodenamePage(
                    modifier = Modifier.padding(p),
                    codenames = codenames,
                    selected = selectedCodename,
                    onSelect = { selectedCodename = it },
                    onGenerate = {
                        scope.launch {
                            codenameBusy = true
                            codenameError = null
                            runCatching {
                                codenames = xxdk.generateIdentities(10)
                                require(codenames.isNotEmpty()) { "No identities generated" }
                                selectedCodename = ""
                            }.onFailure {
                                codenameError = it.message ?: "Could not generate codenames"
                            }
                            codenameBusy = false
                        }
                    },
                    onClaim = {
                        // Apply identity immediately and go to landing
                        // Setup clients will happen in background on landing page
                        scope.launch {
                            codenameBusy = true
                            runCatching {
                                val identity = codenames.first { it.pubkey == selectedCodename }
                                xxdk.applyIdentity(identity)
                                // Don't wait for setupClients - do it in background on landing
                                scope.launch {
                                    xxdk.setupClients(identity.privateIdentity) {
                                        appStorage.isSetupComplete = true
                                    }
                                }
                                route = Route.landing
                            }.onFailure {
                                codenameError = it.message ?: "Could not claim codename"
                            }
                            codenameBusy = false
                        }
                    },
                    status = xxdk.status,
                    isLoading = codenameBusy,
                    error = codenameError
                )
            }

            Route.home -> {
                HomeScreen(
                    viewModel = homeViewModel,
                    onOpenChat = { id -> 
                        chatId = id
                        chatViewModel.loadChat(id)
                        route = Route.chat 
                    },
                    onNewChat = { /* TODO: implement new chat */ },
                    isSetupComplete = appStorage.isSetupComplete,
                    onLogout = {
                        scope.launch {
                            runCatching {
                                // 1. Call xxdk logout (stops network, clears bindings, deletes state)
                                xxdk.logout()
                                
                                // 2. Clear database (messages, reactions, senders, chats)
                                // TODO: Clear database tables like iOS does
                                // For now, we'll just clear the storage
                                
                                // 3. Clear app storage
                                appStorage.clearAll()
                                
                                // 4. Reset navigation to password page
                                route = Route.password
                            }.onFailure {
                                Log.e("HavenApp", "Logout failed: ${it.message}")
                            }
                        }
                    },
                    statusPercentage = xxdk.statusPercentage,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Route.chat -> {
                val currentChat by chatViewModel.currentChat.collectAsState()
                ChatScreen(
                    chat = currentChat,
                    messages = chatMessages,
                    inputText = inputText,
                    onInputChange = { chatViewModel.onInputChange(it) },
                    onSendClick = { chatViewModel.sendMessage() },
                    onReplyClick = { /* TODO: implement reply */ },
                    onBackClick = { route = Route.home },
                    getSenderName = { senderId -> chatViewModel.getSenderName(senderId) },
                    modifier = Modifier.fillMaxSize()
                )
            }

            Route.logViewer -> Page("Logs", { route = Route.home }) { p ->
                LogPage(
                    modifier = Modifier.padding(p),
                    logs = allLogs,
                    filter = logFilter,
                    search = logSearch,
                    onFilter = { logFilter = it },
                    onSearch = { logSearch = it }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Page(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = { if (onBack != null) TextButton(onClick = { onBack() }) { Text("Back") } },
                actions = actions
            )
        },
        bottomBar = bottomBar,
        content = content
    )
}
