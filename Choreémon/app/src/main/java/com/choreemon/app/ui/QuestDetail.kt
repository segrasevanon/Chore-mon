package com.choreemon.app.ui

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.choreemon.app.data.*

@Composable fun PhotoProof(uri: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap by produceState<ImageBitmap?>(null, uri) {
        value = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            runCatching { context.contentResolver.openInputStream(Uri.parse(uri))?.use { BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = 4 })?.asImageBitmap() } }.getOrNull()
        }
    }
    if (bitmap != null) Image(bitmap!!, "Attached chore photo", modifier.clip(RoundedCornerShape(15.dp)), contentScale = ContentScale.Crop)
    else Box(modifier.clip(RoundedCornerShape(15.dp)).background(Sky), contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Photo, "Photo unavailable", tint = Blue) }
}

@Composable fun QuestDetail(chore: Chore, state: GameState, onBack: () -> Unit, onComplete: (String) -> Unit) {
    var checked by rememberSaveable(chore.id) { mutableStateOf(emptyList<Int>()) }
    var photo by rememberSaveable(chore.id) { mutableStateOf("") }
    var submitting by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            runCatching { context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            photo = uri.toString()
        }
    }
    Column(Modifier.fillMaxSize().background(CanvasColor).statusBarsPadding().navigationBarsPadding()) {
        BackHeader("Your quest", onBack) { Pill("${chore.points} pts", Color(0xFFAA760C), Color(0xFFFFF0C6), Icons.Rounded.Star) }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            GameCard(Modifier.fillMaxWidth(), chore.element.pale) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(70.dp).clip(RoundedCornerShape(22.dp)).background(Color.White.copy(alpha = .8f)), contentAlignment = Alignment.Center) { Icon(chore.element.choreIcon, null, Modifier.size(42.dp), tint = chore.element.color) }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(chore.title, style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.height(5.dp)); Pill(chore.element.label, chore.element.color, icon = chore.element.icon)
                    }
                }
                Spacer(Modifier.height(14.dp)); Text(chore.description, style = MaterialTheme.typography.bodyMedium, color = Ink)
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("One little step at a time", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text("${checked.size}/${chore.steps.size}", style = MaterialTheme.typography.labelLarge, color = Blue)
            }
            XpBar(checked.size.toFloat() / chore.steps.size, color = Green)
            chore.steps.forEachIndexed { index, step ->
                val done = index in checked
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(if (done) Color(0xFFE9F9F0) else Color.White)
                    .border(1.dp, if (done) Green.copy(alpha = .3f) else Color(0xFFE4ECF6), RoundedCornerShape(18.dp))
                    .toggleable(value = done, role = Role.Checkbox, onValueChange = { checked = if (done) checked - index else checked + index })
                    .padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(32.dp).clip(CircleShape).background(if (done) Green else Sky), contentAlignment = Alignment.Center) {
                        if (done) Icon(Icons.Rounded.Check, null, Modifier.size(21.dp), tint = Color.White) else Text("${index + 1}", style = MaterialTheme.typography.labelLarge, color = Blue)
                    }
                    Spacer(Modifier.width(12.dp)); Text(step, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                }
            }
            GameCard(Modifier.fillMaxWidth(), onClick = { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.AddAPhoto, null, Modifier.size(30.dp), tint = Blue)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) { Text(if (photo.isEmpty()) "Add a photo (optional)" else "Your sparkle, captured!", style = MaterialTheme.typography.titleSmall); Text("Stays on this device", style = MaterialTheme.typography.bodySmall) }
                    if (photo.isNotEmpty()) PhotoProof(photo, Modifier.size(52.dp)) else Icon(Icons.Rounded.Add, "Choose photo", tint = Blue)
                }
                if (photo.isNotEmpty()) TextButton({ photo = "" }, contentPadding = PaddingValues(0.dp)) { Text("Remove photo") }
            }
            if (chore.approval) Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(Color(0xFFF0EAFC)).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.SupervisorAccount, null, tint = Purple); Spacer(Modifier.width(10.dp))
                Text("A grown-up will check this quest before your rewards arrive.", style = MaterialTheme.typography.bodySmall, color = Ink)
            }
            Spacer(Modifier.height(8.dp))
        }
        Surface(shadowElevation = 10.dp) {
            Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                GameButton(if (chore.approval) "Send for a thumbs up" else "Mark complete", { submitting = true; onComplete(photo) }, Modifier.fillMaxWidth(), Green, Icons.Rounded.Check, checked.size == chore.steps.size && !submitting)
                Spacer(Modifier.height(7.dp)); Text(if (checked.size == chore.steps.size) "${state.companion.name} is cheering you on!" else "Check each step when you're done.", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable fun RewardDialog(reward: ChoreReward, creature: Creature, onDone: () -> Unit) {
    Dialog(onDone, DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxWidth().padding(22.dp).clip(RoundedCornerShape(32.dp)).background(Brush.verticalGradient(listOf(Color(0xFFF0FCFF), Color.White, Color(0xFFFFF7DE))))) {
            if (!reward.pending) Confetti(Modifier.matchParentSize())
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Pill(if (reward.pending) "A GROWN-UP'S TURN" else "A LITTLE WIN. A BIG HIGH-FIVE.", if (reward.pending) Purple else Green, icon = if (reward.pending) Icons.Rounded.Schedule else Icons.Rounded.Verified)
                Spacer(Modifier.height(15.dp))
                Text(if (reward.pending) "Off for a thumbs up!" else "You did the thing!", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                Text(if (reward.pending) "Your quest is safely in the review queue." else "A brighter home. A happier buddy.", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                PetArt(creature, Modifier.size(210.dp).padding(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GameCard(Modifier.weight(1f), Color(0xFFFFF0C9)) {
                        Icon(Icons.Rounded.Star, null, tint = Gold)
                        Text("+${reward.points}", style = MaterialTheme.typography.headlineMedium)
                        Text(if (reward.pending) "after approval" else "points earned", style = MaterialTheme.typography.bodySmall)
                    }
                    GameCard(Modifier.weight(1f), Color(0xFFE9EDFF)) {
                        Icon(Icons.Rounded.Bolt, null, tint = Purple)
                        Text("+${reward.xp} XP", style = MaterialTheme.typography.headlineMedium)
                        Text("for ${reward.creatureName}", style = MaterialTheme.typography.bodySmall, maxLines = 1)
                    }
                }
                Spacer(Modifier.height(20.dp))
                GameButton(if (reward.pending) "Got it!" else "Awesome!", onDone, Modifier.fillMaxWidth(), if (reward.pending) Purple else Color(0xFFFFAE20), Icons.Rounded.AutoAwesome)
            }
        }
    }
}
