package com.choreemon.app.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import androidx.compose.ui.semantics.*
import com.choreemon.app.R
import com.choreemon.app.data.*
import kotlinx.coroutines.delay

@Composable fun HatcheryScreen(state: GameState, onHatch: (EggKind) -> Unit, onQuests: () -> Unit) {
    var selected by rememberSaveable { mutableStateOf(EggKind.CLASSIC) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("A LITTLE MAGIC AWAITS", style = MaterialTheme.typography.labelSmall, color = Purple)
                Text("The hatchery", style = MaterialTheme.typography.headlineLarge)
            }
            Pill("${state.member.points}", Color(0xFFAD770D), Color(0xFFFFF1CB), Icons.Rounded.Star)
        }
        Text("Who will your next little friend be?", style = MaterialTheme.typography.bodyMedium)
        Box(Modifier.fillMaxWidth().height(265.dp).clip(RoundedCornerShape(28.dp)).background(Color(0xFFF6EDFF))) {
            Image(painterResource(R.drawable.room), null, Modifier.matchParentSize(), contentScale = ContentScale.Crop, alpha = .23f)
            Box(Modifier.align(Alignment.Center).size(240.dp).background(Brush.radialGradient(listOf(Color.White, Color(0xB3FFF0B4), Color.Transparent)), CircleShape))
            Confetti(Modifier.matchParentSize(), burst = false)
            EggArt(Modifier.align(Alignment.Center).size(235.dp), kind = selected)
            Pill("Every egg is a new friendship", Purple, Color.White.copy(alpha = .93f), Icons.Rounded.FavoriteBorder, Modifier.align(Alignment.BottomCenter).padding(bottom = 13.dp))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            EggKind.entries.forEach { kind ->
                val color = when (kind) { EggKind.CLASSIC -> Blue; EggKind.ENCHANTED -> Purple; EggKind.AURORA -> Color(0xFFB7830C) }
                Column(Modifier.weight(1f).clip(RoundedCornerShape(20.dp)).background(if (selected == kind) color.copy(alpha = .11f) else Color.White)
                    .border(if (selected == kind) 2.dp else 1.dp, if (selected == kind) color else Color(0xFFE2E8F3), RoundedCornerShape(20.dp))
                    .clickable(role = androidx.compose.ui.semantics.Role.RadioButton, onClick = { selected = kind }).padding(vertical = 13.dp, horizontal = 3.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(if (kind == EggKind.AURORA) Icons.Rounded.WorkspacePremium else Icons.Rounded.AutoAwesome, null, Modifier.size(22.dp), tint = color)
                    Spacer(Modifier.height(6.dp)); Text(kind.label, style = MaterialTheme.typography.labelMedium, color = Ink)
                    Spacer(Modifier.height(4.dp)); Text("${kind.price} points", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        GameButton("Hatch for ${selected.price} points", { onHatch(selected) }, Modifier.fillMaxWidth(), Color(0xFFFFAD23), Icons.Rounded.AutoAwesome, state.member.points >= selected.price)
        if (state.member.points < selected.price) {
            TextButton(onQuests, Modifier.align(Alignment.CenterHorizontally)) { Text("Earn ${selected.price - state.member.points} more points with quests"); Icon(Icons.Rounded.ChevronRight, null) }
        }
        GameCard(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Info, null, Modifier.size(19.dp), tint = Blue)
                Spacer(Modifier.width(7.dp)); Text("A peek inside the magic", style = MaterialTheme.typography.titleSmall)
            }
            Spacer(Modifier.height(13.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf(Triple("Common", selected.common, Blue), Triple("Rare", selected.rare, Purple), Triple("Legendary", selected.legendary, Color(0xFFB7830C))).forEach { (label, odds, color) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$odds%", style = MaterialTheme.typography.titleLarge, color = color)
                        Text(label, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Spacer(Modifier.height(12.dp)); Text("All four creature families can hatch. Rarity is a special look; every buddy can reach its final evolution.", style = MaterialTheme.typography.bodySmall)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.VolunteerActivism, null, Modifier.size(17.dp), tint = Pink)
            Spacer(Modifier.width(7.dp)); Text("Earned through helping. Always.", style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable fun HatchReveal(result: HatchResult, onPetTap: () -> Unit, onDone: () -> Unit, onName: () -> Unit) {
    var taps by rememberSaveable(result.creature.id) { mutableIntStateOf(0) }
    var revealed by rememberSaveable(result.creature.id) { mutableStateOf(false) }
    val motion = LocalMotion.current
    LaunchedEffect(taps) { if (taps >= 3) { delay(if (motion) 750 else 0); revealed = true } }
    Dialog(onDismissRequest = { if (revealed) onDone() }, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFE3F5FF), Color(0xFFF3EBFF), Color(0xFFFFF3D7)))).statusBarsPadding().navigationBarsPadding()) {
            if (revealed) Confetti(Modifier.matchParentSize())
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Pill(if (revealed) "A NEW FRIEND!" else "${result.kind.price} POINTS WELL SPENT", Purple, icon = Icons.Rounded.AutoAwesome)
                Spacer(Modifier.height(16.dp))
                Text(if (revealed) "Hello, ${result.creature.species}!" else "Something magical\nis waking up…", style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center)
                Spacer(Modifier.height(9.dp))
                Text(if (revealed) "Your next adventure just got a little cuter." else "Give your egg three gentle taps.", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                Spacer(Modifier.height(22.dp))
                AnimatedContent(revealed, transitionSpec = { if (motion) (fadeIn(tween(800)) + scaleIn(initialScale = .3f, animationSpec = spring(dampingRatio = .55f))).togetherWith(fadeOut(tween(200))) else EnterTransition.None.togetherWith(ExitTransition.None) }, label = "hatching reveal") { open ->
                    if (open) {
                        Box(Modifier.size(285.dp), contentAlignment = Alignment.Center) {
                            Box(Modifier.size(275.dp).background(Brush.radialGradient(listOf(Color.White, Gold.copy(alpha = .3f), Color.Transparent)), CircleShape))
                            PetArt(result.creature, Modifier.size(275.dp))
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(Modifier.size(285.dp).semantics { contentDescription = "Hatch egg" }.clip(CircleShape).background(Brush.radialGradient(listOf(Color.White, Gold.copy(alpha = .28f), Color.Transparent)))
                                .clickable(enabled = taps < 3, onClickLabel = "Tap egg ${taps + 1} of 3", onClick = { taps++; onPetTap() }), contentAlignment = Alignment.Center) {
                                EggArt(Modifier.size(260.dp), wobble = taps > 0, kind = result.kind)
                            }
                            Spacer(Modifier.height(18.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { repeat(3) { i -> Box(Modifier.size(12.dp).clip(CircleShape).background(if (i < taps) Purple else Purple.copy(alpha = .2f))) } }
                            Spacer(Modifier.height(15.dp)); Text(if (taps >= 3) "Here comes your buddy!" else "Tap your egg • $taps / 3", style = MaterialTheme.typography.labelLarge, color = Purple)
                        }
                    }
                }
                if (revealed) {
                    Spacer(Modifier.height(18.dp))
                    Pill(result.creature.rarity.label, result.creature.rarity.color, icon = Icons.Rounded.WorkspacePremium)
                    Spacer(Modifier.height(15.dp))
                    Text(result.creature.element.description, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(15.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) { Pill(result.creature.element.label, result.creature.element.color, icon = result.creature.element.icon); Pill("Level 1", Blue, icon = Icons.Rounded.Bolt) }
                    Spacer(Modifier.height(25.dp))
                    GameButton("Adventure together", onDone, Modifier.fillMaxWidth(), Color(0xFFFFAD23), Icons.Rounded.Favorite)
                    TextButton(onName, Modifier.heightIn(min = 48.dp)) { Icon(Icons.Rounded.Edit, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Give your buddy a name") }
                }
            }
        }
    }
}
