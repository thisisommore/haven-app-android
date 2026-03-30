package com.example.haven

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.haven.ui.ChatViewModel
import com.example.haven.ui.HomeViewModel
import com.example.haven.xxdk.GeneratedIdentity
import com.example.haven.xxdk.XXDK
import com.example.haven.xxdk.XXDKStorage
import com.example.haven.xxdk.applyIdentity
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

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
    // Start with landing if setup complete (will auto-route to home when ready)
    // Start with password if setup not complete
    var route by rememberSaveable { mutableStateOf(if (appStorage.isSetupComplete) Route.landing else Route.password) }
    
    // For returning users: auto-load cmix when landing page is shown
    LaunchedEffect(route) {
        if (route == Route.landing && appStorage.isSetupComplete && xxdk.cmix == null) {
            runCatching {
                xxdk.setAppStorage(appStorage)
                xxdk.loadCmix()
                xxdk.startNetworkFollower()
                val identity = xxdk.loadSavedPrivateIdentity()
                xxdk.loadClients(identity)
            }.onFailure {
                // If loading fails, go to password setup
                appStorage.isSetupComplete = false
                route = Route.password
            }
        }
    }
    
    // Auto-route from landing to home when setup is complete and status is ready
    LaunchedEffect(appStorage.isSetupComplete, xxdk.statusPercentage) {
        if (route == Route.landing && appStorage.isSetupComplete && xxdk.statusPercentage == 100) {
            route = Route.home
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
    var codenameBusy by rememberSaveable { mutableStateOf(false) }
    var codenameError by rememberSaveable { mutableStateOf<String?>(null) }
    val currentChatTitle by homeViewModel.filteredChats.collectAsState(initial = emptyList())
    val currentChatName = currentChatTitle.firstOrNull { it.id == chatId }?.title ?: "Chat"

    when (route) {
        Route.landing -> LandingPage(
            modifier = Modifier.fillMaxSize(),
            status = xxdk.status,
            statusPercentage = xxdk.statusPercentage,
            isSetupComplete = appStorage.isSetupComplete
        )

        Route.password -> Page("Join the alpha", onBack = null) { p ->
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
                            val ndf = xxdk.downloadNdf()
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
                status = xxdk.status,
                isLoading = passwordBusy,
                error = passwordError
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
                    scope.launch {
                        codenameBusy = true
                        codenameError = null
                        runCatching {
                            val identity = codenames.first { it.pubkey == selectedCodename }
                            xxdk.applyIdentity(identity)
                            xxdk.setupClients(identity.privateIdentity) {
                                appStorage.isSetupComplete = true
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
