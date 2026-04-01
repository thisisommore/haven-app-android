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
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Dispatchers
import android.util.Log

@Composable
internal fun HavenApp() {
    val context = LocalContext.current.applicationContext
    val appStorage = remember { HavenApplication.getStorage(context) }
    val xxdk = remember { HavenApplication.getXXDK(context) }
    // Use application-scoped coroutines that survive config changes
    val scope = remember { CoroutineScope(SupervisorJob() + Dispatchers.Main) }
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
    // For existing users: start directly at home
    var route by rememberSaveable { 
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
                Log.e("HavenApp", "Failed to load cmix for existing user: ${it.message}")
            }
        }
    }
    var chatId by rememberSaveable { mutableStateOf("") }

    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    // Codenames with custom saver - store as list of simple data (pubkey,codename,codeset only)
    var codenames by rememberSaveable(
        stateSaver = androidx.compose.runtime.saveable.listSaver(
            save = { list ->
                list.map { listOf(it.pubkey, it.codename, it.codeset.toString()) }
            },
            restore = { savedList ->
                savedList.map { 
                    GeneratedIdentity(
                        privateIdentity = byteArrayOf(), // Will be regenerated if needed
                        codename = it[1],
                        codeset = it[2].toInt(),
                        pubkey = it[0]
                    )
                }
            }
        )
    ) { mutableStateOf<List<GeneratedIdentity>>(emptyList()) }
    var selectedCodename by rememberSaveable { mutableStateOf("") }
    var logFilter by rememberSaveable { mutableStateOf("ALL") }
    var logSearch by rememberSaveable { mutableStateOf("") }
    var passwordBusy by rememberSaveable { mutableStateOf(false) }
    var passwordError by rememberSaveable { mutableStateOf<String?>(null) }
    // NDF download state - starts when password page opens
    var ndfDeferred by remember { mutableStateOf<Deferred<ByteArray>?>(null) }
    var codenameBusy by rememberSaveable { mutableStateOf(false) }
    var codenameError by rememberSaveable { mutableStateOf<String?>(null) }

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

            Route.password -> {
                // Automatically reset setup and start NDF download on page entry
                // Only run once when first entering password page (not on config change)
                LaunchedEffect(Unit) {
                    if (!appStorage.isSetupComplete && ndfDeferred == null) {
                        runCatching {
                            appStorage.clearAll()
                            password = ""
                            confirm = ""
                            passwordError = null
                            ndfDeferred = scope.async {
                                xxdk.downloadNdf()
                            }
                        }.onFailure {
                            Log.e("HavenApp", "Reset setup failed: ${it.message}")
                        }
                    }
                }
                
                PasswordPage(
                    modifier = Modifier.fillMaxSize(),
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
                        passwordError = "Import needs a real encrypted identity file."
                    },
                    status = xxdk.status,
                    isLoading = passwordBusy,
                    error = passwordError
                )
            }

            Route.codenameGenerator -> {
                // Regenerate identities if restored from config change with empty private data
                // Only regenerate if cmix is ready and we have empty private identities
                LaunchedEffect(codenames) {
                    if (codenames.isNotEmpty() && 
                        codenames.first().privateIdentity.isEmpty() &&
                        xxdk.cmix != null) {
                        codenameBusy = true
                        runCatching {
                            codenames = xxdk.generateIdentities(10)
                            selectedCodename = ""
                        }.onFailure {
                            codenameError = it.message ?: "Could not regenerate codenames"
                        }
                        codenameBusy = false
                    }
                }
                
                CodenamePage(
                    modifier = Modifier.fillMaxSize(),
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
                        // Navigate to landing immediately, setup continues in background
                        route = Route.landing
                        scope.launch {
                            runCatching {
                                val identity = codenames.first { it.pubkey == selectedCodename }
                                xxdk.applyIdentity(identity)
                                xxdk.setupClients(identity.privateIdentity) {
                                    appStorage.isSetupComplete = true
                                }
                            }.onFailure {
                                Log.e("HavenApp", "Claim failed: ${it.message}")
                            }
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
                                xxdk.logout()
                                appStorage.clearAll()
                                HavenApplication.reset()
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
