package com.choreemon.app.ui

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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.choreemon.app.R
import com.choreemon.app.data.*
import java.time.LocalDate

@Composable fun WelcomeScreen(onDemo: () -> Unit, onCreate: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFE0F4FF), Color(0xFFFAF9FF), Color(0xFFFFF6E5)))).statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Spacer(Modifier.height(12.dp)); Brand(size = 40)
        Pill("REAL CHORES. LITTLE WONDERS.", Purple, icon = Icons.Rounded.AutoAwesome)
        Text("Little chores.\nBig adventures.", style = MaterialTheme.typography.displaySmall, textAlign = TextAlign.Center)
        Text("Make home a little brighter.\nRaise a friend who grows with you.", style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, color = Muted)
        Box(Modifier.fillMaxWidth().height(222.dp).clip(RoundedCornerShape(30.dp)).border(2.dp, Color.White, RoundedCornerShape(30.dp))) {
            Image(painterResource(R.drawable.room), null, Modifier.matchParentSize(), contentScale = ContentScale.Crop)
            PetArt(Creature("welcome", "", Element.WATER, Rarity.COMMON, adopted = LocalDate.now().toString()), Modifier.align(Alignment.BottomStart).padding(start = 8.dp, bottom = 6.dp).size(205.dp))
            EggArt(Modifier.align(Alignment.BottomEnd).padding(end = 12.dp, bottom = 15.dp).size(138.dp))
            Confetti(Modifier.matchParentSize(), burst = false)
        }
        GameCard(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                listOf(Triple(Icons.Rounded.Checklist, "Do a chore", Blue), Triple(Icons.Rounded.Star, "Earn points", Gold), Triple(Icons.Rounded.Pets, "Grow a buddy", Purple)).forEachIndexed { index, (icon, label, color) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.size(42.dp).clip(CircleShape).background(color.copy(alpha = .12f)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = color) }
                        Spacer(Modifier.height(7.dp)); Text(label, style = MaterialTheme.typography.labelMedium, color = Ink)
                    }
                }
            }
        }
        GameButton("Start playing", onDemo, Modifier.fillMaxWidth(), Color(0xFFFFAC24), Icons.Rounded.AutoAwesome)
        OutlinedButton(onCreate, Modifier.fillMaxWidth().heightIn(min = 54.dp), shape = RoundedCornerShape(18.dp), border = BorderStroke(1.5.dp, Purple.copy(alpha = .4f))) { Icon(Icons.Rounded.Home, null, Modifier.size(21.dp)); Spacer(Modifier.width(8.dp)); Text("Create our household") }
        Text("Start playing explores the Chen family demo.\nDemo parent PIN: 2468 • Progress saves on this device.", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
        Spacer(Modifier.height(10.dp))
    }
}

@Composable fun CreateHouseholdScreen(onBack: () -> Unit, onCreate: (String, String, String) -> Unit) {
    var household by rememberSaveable { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }
    var pin by rememberSaveable { mutableStateOf("") }
    var repeat by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().background(CanvasColor).statusBarsPadding().navigationBarsPadding().imePadding()) {
        BackHeader("A home for your adventure", onBack)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            PageHeading("GROWN-UPS START HERE", "Welcome to the team.", "One household, lots of little heroes. You'll start with a free Sudsy and four everyday quests.")
            OutlinedTextField(household, { household = it.take(32) }, Modifier.fillMaxWidth(), label = { Text("Household name") }, placeholder = { Text("e.g. The Patel Family") }, shape = RoundedCornerShape(16.dp), singleLine = true)
            OutlinedTextField(name, { name = it.take(20) }, Modifier.fillMaxWidth(), label = { Text("First adventurer's name") }, shape = RoundedCornerShape(16.dp), singleLine = true)
            OutlinedTextField(pin, { pin = it.filter(Char::isDigit).take(4) }, Modifier.fillMaxWidth(), label = { Text("Choose a four-digit parent PIN") }, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), shape = RoundedCornerShape(16.dp), singleLine = true)
            OutlinedTextField(repeat, { repeat = it.filter(Char::isDigit).take(4) }, Modifier.fillMaxWidth(), label = { Text("Repeat parent PIN") }, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), shape = RoundedCornerShape(16.dp), singleLine = true)
            Text("Keep this PIN safe. It protects quest management on your shared device. You can add more adventurers in the grown-up corner.", style = MaterialTheme.typography.bodySmall)
            if (error.isNotEmpty()) Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            GameButton("Let our adventure begin", {
                if (pin != repeat) error = "Those PINs don't match yet."
                else runCatching { onCreate(household, name, pin) }.onFailure { error = it.message ?: "Check your details." }
            }, Modifier.fillMaxWidth(), Color(0xFFFFAD23), Icons.Rounded.Pets, household.trim().length >= 2 && name.trim().length >= 2 && pin.length == 4 && repeat.length == 4)
        }
    }
}

@Composable fun SettingsScreen(state: GameState, onBack: () -> Unit, onPreferences: (Preferences) -> Unit, onProfiles: () -> Unit, onParent: () -> Unit) {
    Column(Modifier.fillMaxSize().background(CanvasColor).statusBarsPadding().navigationBarsPadding()) {
        BackHeader("Make yourself at home", onBack)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            PageHeading("YOUR ADVENTURE, YOUR WAY", "The little details", "A cozy space for the whole household.")
            GameCard(Modifier.fillMaxWidth()) {
                listOf(Triple("Playful sounds", "Little chimes for your little wins", state.preferences.sound), Triple("Happy taps", "Gentle vibration when you help", state.preferences.haptics), Triple("Calmer animations", "Keep things still and comfortable", state.preferences.reducedMotion)).forEachIndexed { index, (title, body, enabled) ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) { Text(title, style = MaterialTheme.typography.titleSmall); Text(body, style = MaterialTheme.typography.bodySmall) }
                        Switch(enabled, { value -> onPreferences(when (index) { 0 -> state.preferences.copy(sound = value); 1 -> state.preferences.copy(haptics = value); else -> state.preferences.copy(reducedMotion = value) }) })
                    }
                    if (index < 2) HorizontalDivider(color = Color(0xFFEDF1F7))
                }
            }
            GameCard(Modifier.fillMaxWidth(), Sky) {
                Text(state.household, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(5.dp)); Text("${state.members.size} adventurers • Saved on this device", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(14.dp)); GameButton("Switch adventurer", onProfiles, Modifier.fillMaxWidth(), icon = Icons.Rounded.Face)
                TextButton(onParent, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Icon(Icons.Rounded.Lock, null, Modifier.size(18.dp)); Spacer(Modifier.width(7.dp)); Text("Grown-up corner") }
            }
            GameCard(Modifier.fillMaxWidth()) {
                Text("A little field guide", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(11.dp))
                Text("1. Finish each step in a quest to earn points.\n\n2. Your active buddy gains XP. Happy buddies earn a little extra.\n\n3. Spend 50 points on a classic egg. Every tenth quest unlocks a 25-point gift.\n\n4. Evolve at levels 10 and 25. All rarities can evolve.\n\n5. Help on consecutive days to build a streak. Your buddies always welcome you back.", style = MaterialTheme.typography.bodyMedium)
            }
            Text("Made for little helpers and the people cheering them on. No ads, purchases, or accounts. This version uses one shared device; it does not sync between phones.", style = MaterialTheme.typography.bodySmall)
            Text("Choreémon 1.0 • A little help. A little magic.", style = MaterialTheme.typography.bodySmall, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(15.dp))
        }
    }
}
