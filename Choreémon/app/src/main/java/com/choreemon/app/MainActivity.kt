package com.choreemon.app

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.choreemon.app.data.*
import com.choreemon.app.ui.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT), navigationBarStyle = SystemBarStyle.light(android.graphics.Color.WHITE, android.graphics.Color.WHITE))
        setContent { ChoreemonTheme { ChoreemonApp() } }
    }
}

private enum class Tab(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    HOME("Home", Icons.Rounded.Home), QUESTS("Quests", Icons.Rounded.Checklist), HATCH("Hatch", Icons.Rounded.EggAlt),
    CREATURES("Buddies", Icons.Rounded.Pets), HEROES("Heroes", Icons.Rounded.EmojiEvents)
}

@Composable fun ChoreemonApp(vm: GameViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    var route by rememberSaveable { mutableStateOf("main") }
    var tab by rememberSaveable { mutableStateOf(Tab.HOME) }
    var choreId by rememberSaveable { mutableStateOf<String?>(null) }
    var petId by rememberSaveable { mutableStateOf<String?>(null) }
    var editId by rememberSaveable { mutableStateOf<String?>(null) }
    var authorized by remember { mutableStateOf(false) }
    var profiles by remember { mutableStateOf(false) }
    var activity by remember { mutableStateOf(false) }
    var reward by remember { mutableStateOf<ChoreReward?>(null) }
    var hatch by remember { mutableStateOf<HatchResult?>(null) }
    var nameId by remember { mutableStateOf<String?>(null) }
    var addMember by remember { mutableStateOf(false) }
    var changePin by remember { mutableStateOf(false) }
    var reset by remember { mutableStateOf(false) }
    var evolvedId by remember { mutableStateOf<String?>(null) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val view = LocalView.current
    val lifecycle = LocalLifecycleOwner.current
    val tone = remember { runCatching { ToneGenerator(AudioManager.STREAM_MUSIC, 35) }.getOrNull() }
    fun feedback(success: Boolean = false) {
        if (state.preferences.haptics) view.performHapticFeedback(if (success) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.CLOCK_TICK)
        if (success && state.preferences.sound) tone?.startTone(ToneGenerator.TONE_PROP_ACK, 160)
    }
    fun message(text: String) { scope.launch { snackbar.showSnackbar(text) } }
    fun attempt(action: () -> Unit) { runCatching(action).onFailure { message(it.message ?: "Let's try that again.") } }
    fun openQuest(chore: Chore) { choreId = chore.id; route = "quest"; feedback() }
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) vm.refresh()
            if (event == Lifecycle.Event.ON_STOP) authorized = false
        }
        lifecycle.lifecycle.addObserver(observer)
        onDispose { lifecycle.lifecycle.removeObserver(observer); tone?.release() }
    }
    LaunchedEffect(Unit) {
        if (vm.recovered) message("A damaged save was preserved for recovery. The demo has been restored.")
        while (true) { delay(60000); vm.refresh() }
    }
    val protectedRoute = route == "parent" || route == "editor"
    BackHandler(route != "main" || tab != Tab.HOME) {
        if (route == "editor" && authorized) route = "parent"
        else if (route != "main") route = "main"
        else tab = Tab.HOME
    }
    CompositionLocalProvider(LocalMotion provides !state.preferences.reducedMotion) {
        Box(Modifier.fillMaxSize().background(CanvasColor)) {
            when {
                !state.onboarded && route != "create" -> WelcomeScreen({ vm.startDemo(); feedback(true) }, { route = "create" })
                route == "create" -> CreateHouseholdScreen({ route = "main" }) { home, name, pin -> vm.createHousehold(home, name, pin); route = "main"; feedback(true) }
                route == "quest" && choreId != null -> state.chores.find { it.id == choreId }?.let { chore ->
                    QuestDetail(chore, state, { route = "main" }) { photo ->
                        attempt { reward = vm.complete(chore.id, photo); route = "main"; feedback(true) }
                    }
                }
                route == "creature" && petId != null -> state.creatures.find { it.id == petId }?.let { pet ->
                    CreatureDetail(state, pet, { route = "main" }, { nameId = pet.id }, {
                        vm.love(pet.id); feedback(); if (pet.lovedOn != java.time.LocalDate.now().toString()) message("A little love for ${pet.name}!")
                    }, { vm.favorite(pet.id); feedback() }, { vm.choose(pet.id); feedback(true); message("${pet.name} is your adventure buddy!") }, {
                        attempt { vm.evolve(pet.id); evolvedId = pet.id; feedback(true) }
                    })
                }
                route == "settings" -> SettingsScreen(state, { route = "main" }, { prefs -> vm.update { it.copy(preferences = prefs) } }, { profiles = true }, { route = "parent" })
                route == "parent" && authorized -> AdminScreen(state, { route = "main"; authorized = false }, { editId = null; route = "editor" }, { editId = it.id; route = "editor" }, { vm.archive(it); message("Quest updated.") }, { id, approve -> attempt { vm.review(id, approve); feedback(true); message(if (approve) "Quest approved. Rewards delivered!" else "Quest returned for another try.") } }, { addMember = true }, { changePin = true }, { reset = true })
                route == "editor" && authorized -> ChoreEditor(state, state.chores.find { it.id == editId }, { route = "parent" }) { chore -> vm.saveChore(chore); route = "parent"; feedback(true); message("Quest saved. Your adventurers are ready!") }
                else -> Scaffold(
                    containerColor = CanvasColor,
                    topBar = {
                        Column(Modifier.background(Color(0xFFF0F8FF)).statusBarsPadding().padding(start = 20.dp, end = 10.dp, bottom = 9.dp, top = 5.dp)) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Brand(Modifier.weight(1f))
                                IconButton({ activity = true }) { Icon(Icons.Rounded.NotificationsNone, "Your recent wins", tint = Ink) }
                                IconButton({ route = "settings" }) { Icon(Icons.Rounded.Settings, "Settings", tint = Ink) }
                            }
                            Row(Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClickLabel = "Open household profiles", onClick = { profiles = true }).padding(top = 7.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Home, null, Modifier.size(12.dp), tint = Muted)
                                Spacer(Modifier.width(4.dp)); Text(state.household, style = MaterialTheme.typography.bodySmall)
                                Icon(Icons.Rounded.ExpandMore, null, Modifier.size(14.dp), tint = Muted)
                            }
                        }
                    },
                    bottomBar = {
                        Surface(shadowElevation = 14.dp, color = Color.White, shape = RoundedCornerShape(topStart = 25.dp, topEnd = 25.dp)) {
                            Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 8.dp, vertical = 7.dp)) {
                                Tab.entries.forEach { item ->
                                    val selected = tab == item
                                    Column(Modifier.weight(1f).clip(RoundedCornerShape(18.dp)).background(if (selected) Sky else Color.Transparent)
                                        .clickable(role = androidx.compose.ui.semantics.Role.Tab, onClickLabel = item.label, onClick = { tab = item; feedback() })
                                        .padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        if (item == Tab.HATCH) Image(painterResource(R.drawable.egg), null, Modifier.size(25.dp), alpha = if (selected) 1f else .66f)
                                        else Icon(item.icon, null, Modifier.size(25.dp), tint = if (selected) Blue else Color(0xFF94A6C2))
                                        Spacer(Modifier.height(4.dp)); Text(item.label, style = MaterialTheme.typography.labelSmall, color = if (selected) Blue else Muted, letterSpacing = 0.sp)
                                    }
                                }
                            }
                        }
                    }
                ) { inset ->
                    Box(Modifier.padding(inset)) {
                        AnimatedContent(tab, transitionSpec = { fadeIn(androidx.compose.animation.core.tween(if (state.preferences.reducedMotion) 0 else 200)).togetherWith(fadeOut(androidx.compose.animation.core.tween(120))) }, label = "page navigation") { selected ->
                            when (selected) {
                                Tab.HOME -> HomeScreen(state, ::openQuest, { tab = Tab.QUESTS }, { tab = Tab.HATCH }, { feedback() }, { profiles = true }, { attempt { vm.claimGift(); feedback(true); message("25 bonus points! Your next friend is a little closer.") } })
                                Tab.QUESTS -> QuestsScreen(state, ::openQuest)
                                Tab.HATCH -> HatcheryScreen(state, { kind -> attempt { hatch = vm.hatch(kind); feedback(true) } }, { tab = Tab.QUESTS })
                                Tab.CREATURES -> CreaturesScreen(state, { petId = it.id; route = "creature"; feedback() }, { tab = Tab.HATCH })
                                Tab.HEROES -> LeaderboardScreen(state)
                            }
                        }
                    }
                }
            }
            SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(16.dp))
        }
        if (protectedRoute && !authorized) ParentPinDialog(state, { route = "main" }, { authorized = true; feedback() })
        if (profiles) AlertDialog(onDismissRequest = { profiles = false }, title = { Text("Who's adventuring?") }, text = {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                state.members.forEach { member ->
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(if (state.activeMember == member.id) Sky else Color.Transparent).clickable {
                        vm.switchMember(member.id); profiles = false; route = "main"; tab = Tab.HOME; feedback()
                    }.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Avatar(member, size = 40.dp); Spacer(Modifier.width(12.dp)); Text(member.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        if (state.activeMember == member.id) Icon(Icons.Rounded.CheckCircle, null, tint = Blue)
                    }
                }
                Text("One shared home. Everyone's own little adventure.", style = MaterialTheme.typography.bodySmall)
            }
        }, confirmButton = { TextButton({ profiles = false; route = "parent" }) { Icon(Icons.Rounded.Lock, null, Modifier.size(17.dp)); Spacer(Modifier.width(5.dp)); Text("Grown-up corner") } }, dismissButton = { TextButton({ profiles = false }) { Text("Close") } })
        if (activity) AlertDialog(onDismissRequest = { activity = false }, title = { Text("Your recent little wins") }, text = {
            Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
                val entries = state.completions.filter { it.memberId == state.activeMember }.takeLast(6).reversed()
                if (entries.isEmpty()) Text("Your first little win is waiting. Try a quest!", style = MaterialTheme.typography.bodyMedium)
                entries.forEach { entry ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (entry.status == CompletionStatus.APPROVED) Icons.Rounded.CheckCircle else Icons.Rounded.Schedule, null, Modifier.size(22.dp), tint = if (entry.status == CompletionStatus.APPROVED) Green else Purple)
                        Spacer(Modifier.width(10.dp)); Column { Text(entry.title, style = MaterialTheme.typography.titleSmall); Text("${entry.date} • ${when (entry.status) { CompletionStatus.APPROVED -> "+${entry.points} pts"; CompletionStatus.PENDING -> "In review"; CompletionStatus.REJECTED -> "Ready to try again" }}", style = MaterialTheme.typography.bodySmall) }
                    }
                }
            }
        }, confirmButton = { TextButton({ activity = false }) { Text("Keep it up!") } })
        reward?.let { earned -> RewardDialog(earned, state.companion) { reward = null } }
        hatch?.let { result -> HatchReveal(result, { feedback() }, {
            vm.choose(result.creature.id); hatch = null; tab = Tab.CREATURES; feedback(true)
        }, { nameId = result.creature.id }) }
        nameId?.let { id ->
            val pet = state.creatures.first { it.id == id }
            NameDialog("A name of their own", "Buddy name", pet.nickname, { nameId = null }) { value -> vm.name(id, value); nameId = null; message("A lovely name for a lovely little friend.") }
        }
        if (addMember && authorized) NameDialog("Another little hero", "Adventurer's name", "", { addMember = false }) { value -> vm.addMember(value); addMember = false; feedback(true); message("Welcome, $value! A starter Sudsy is waiting.") }
        if (changePin && authorized) NameDialog("A new parent PIN", "Four digits", "", { changePin = false }, pin = true) { value -> vm.changePin(value); changePin = false; message("Parent PIN updated.") }
        if (reset && authorized) AlertDialog(onDismissRequest = { reset = false }, title = { Text("Start the demo fresh?") }, text = { Text("This replaces this device's household, buddies, and progress with the Chen family demo. The demo parent PIN will be 2468.") }, confirmButton = { TextButton({ vm.resetDemo(); reset = false; route = "main"; tab = Tab.HOME; authorized = false }) { Text("Reset demo", color = MaterialTheme.colorScheme.error) } }, dismissButton = { TextButton({ reset = false }) { Text("Keep our adventure") } })
        evolvedId?.let { id ->
            val pet = state.creatures.first { it.id == id }
            AlertDialog(onDismissRequest = { evolvedId = null }, title = { Text("Meet ${pet.species}!") }, text = {
                Box { Confetti(Modifier.matchParentSize()); Column(horizontalAlignment = Alignment.CenterHorizontally) { PetArt(pet, Modifier.size(250.dp)); Text("Every little quest brought you here. Here's to your next chapter together.", style = MaterialTheme.typography.bodyMedium) } }
            }, confirmButton = { TextButton({ evolvedId = null }) { Text("Look at you grow!") } })
        }
    }
}

@Composable private fun NameDialog(title: String, label: String, initial: String, onCancel: () -> Unit, pin: Boolean = false, onSave: (String) -> Unit) {
    var value by rememberSaveable { mutableStateOf(initial) }
    var error by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onCancel, title = { Text(title) }, text = {
        Column {
            OutlinedTextField(value, { value = if (pin) it.filter(Char::isDigit).take(4) else it.take(20); error = "" }, Modifier.fillMaxWidth(), label = { Text(label) }, singleLine = true, shape = RoundedCornerShape(16.dp), keyboardOptions = KeyboardOptions(keyboardType = if (pin) KeyboardType.NumberPassword else KeyboardType.Text), visualTransformation = if (pin) PasswordVisualTransformation() else VisualTransformation.None)
            if (error.isNotEmpty()) Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }, confirmButton = { TextButton({ runCatching { onSave(value) }.onFailure { error = it.message ?: "Try another name." } }, enabled = if (pin) value.length == 4 else value.trim().length >= 2) { Text("Save") } }, dismissButton = { TextButton(onCancel) { Text("Cancel") } })
}
