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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.*
import com.choreemon.app.data.*
import java.time.LocalDate
import java.util.UUID

@Composable fun AdminScreen(state: GameState, onBack: () -> Unit, onCreate: () -> Unit, onEdit: (Chore) -> Unit, onArchive: (String) -> Unit, onApprove: (String, Boolean) -> Unit, onAddMember: () -> Unit, onPin: () -> Unit, onReset: () -> Unit) {
    var showArchived by remember { mutableStateOf(false) }
    val pending = state.completions.filter { it.status == CompletionStatus.PENDING }
    Column(Modifier.fillMaxSize().background(CanvasColor).statusBarsPadding().navigationBarsPadding()) {
        BackHeader("Grown-up corner", onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            PageHeading(state.household, "A little teamwork,\na lot of magic.", "Set the quests. Cheer on your household.")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                val counts = listOf(
                    Triple("This week's wins", state.completions.count { it.status == CompletionStatus.APPROVED && LocalDate.parse(it.date) >= weekOf(LocalDate.now()) }, Green),
                    Triple("Adventurers", state.members.size, Blue), Triple("To review", pending.size, Purple)
                )
                counts.forEach { (label, count, color) -> GameCard(Modifier.weight(1f), color.copy(alpha = .09f)) {
                    Text("$count", style = MaterialTheme.typography.headlineLarge, color = color)
                    Text(label, style = MaterialTheme.typography.bodySmall)
                } }
            }
            if (pending.isNotEmpty()) {
                SectionTitle("Ready for a thumbs up")
                pending.forEach { entry ->
                    GameCard(Modifier.fillMaxWidth(), Color(0xFFFFF9EB)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val member = state.members.first { it.id == entry.memberId }
                            Avatar(member, size = 42.dp); Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) { Text(entry.title, style = MaterialTheme.typography.titleMedium); Text("${member.name} • ${entry.date}", style = MaterialTheme.typography.bodySmall) }
                            Pill("${entry.points} pts", Color(0xFFAE7C16), icon = Icons.Rounded.Star)
                        }
                        if (entry.photo.isNotEmpty()) { Spacer(Modifier.height(12.dp)); PhotoProof(entry.photo, Modifier.fillMaxWidth().height(145.dp)) }
                        Spacer(Modifier.height(13.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                            OutlinedButton({ onApprove(entry.id, false) }, Modifier.weight(1f).heightIn(min = 48.dp), shape = RoundedCornerShape(16.dp)) { Text("Try again") }
                            GameButton("Approve", { onApprove(entry.id, true) }, Modifier.weight(1f), Green, Icons.Rounded.Check)
                        }
                    }
                }
            }
            SectionTitle("Household quests", "New quest", onCreate)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Include archived quests", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Switch(showArchived, { showArchived = it })
            }
            state.chores.filter { showArchived || !it.archived }.forEach { chore ->
                GameCard(Modifier.fillMaxWidth(), chore.element.pale) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(chore.element.choreIcon, null, Modifier.size(32.dp), tint = chore.element.color)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(chore.title, style = MaterialTheme.typography.titleMedium)
                            Text("${chore.points} pts • ${chore.schedule.label}${if (chore.archived) " • Archived" else ""}", style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton({ onEdit(chore) }) { Icon(Icons.Rounded.Edit, "Edit ${chore.title}", Modifier.size(21.dp), tint = Blue) }
                        IconButton({ onArchive(chore.id) }) { Icon(if (chore.archived) Icons.Rounded.Unarchive else Icons.Rounded.Archive, if (chore.archived) "Restore ${chore.title}" else "Archive ${chore.title}", Modifier.size(21.dp), tint = Muted) }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(chore.assigned.mapNotNull { id -> state.members.find { it.id == id }?.name }.joinToString("  •  ").ifEmpty { "No adventurers assigned" }, style = MaterialTheme.typography.labelMedium, color = Ink)
                    if (chore.approval) { Spacer(Modifier.height(6.dp)); Pill("Grown-up check required", Purple, icon = Icons.Rounded.VerifiedUser) }
                }
            }
            GameButton("Create a new quest", onCreate, Modifier.fillMaxWidth(), Blue, Icons.Rounded.Add)
            SectionTitle("Your adventurers", "Add member", onAddMember)
            state.members.forEach { member ->
                GameCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Avatar(member); Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) { Text(member.name, style = MaterialTheme.typography.titleMedium); Text("${state.creatures.count { it.owner == member.id }} buddies • ${GameRules.streak(state, member.id, LocalDate.now())} day streak", style = MaterialTheme.typography.bodySmall) }
                        Pill("${member.points}", Color(0xFFB77F0C), icon = Icons.Rounded.Star)
                    }
                }
            }
            GameCard(Modifier.fillMaxWidth(), Sky) {
                Text("${state.household} • ${state.inviteCode}", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(5.dp)); Text("A shared-device household. Switch adventurers from the profile menu. All progress and photos stay on this device.", style = MaterialTheme.typography.bodySmall)
            }
            OutlinedButton(onPin, Modifier.fillMaxWidth().heightIn(min = 50.dp), shape = RoundedCornerShape(16.dp)) { Icon(Icons.Rounded.Lock, null, Modifier.size(18.dp)); Spacer(Modifier.width(9.dp)); Text("Change parent PIN") }
            TextButton(onReset, Modifier.align(Alignment.CenterHorizontally)) { Text("Reset the presentation demo", color = Muted) }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable fun ChoreEditor(state: GameState, chore: Chore?, onBack: () -> Unit, onSave: (Chore) -> Unit) {
    var title by rememberSaveable(chore?.id) { mutableStateOf(chore?.title ?: "") }
    var description by rememberSaveable(chore?.id) { mutableStateOf(chore?.description ?: "") }
    var points by rememberSaveable(chore?.id) { mutableStateOf(chore?.points?.toString() ?: "10") }
    var element by rememberSaveable(chore?.id) { mutableStateOf(chore?.element ?: Element.WATER) }
    var assigned by rememberSaveable(chore?.id) { mutableStateOf(chore?.assigned ?: listOf(state.activeMember)) }
    var schedule by rememberSaveable(chore?.id) { mutableStateOf(chore?.schedule ?: Schedule.DAILY) }
    var approval by rememberSaveable(chore?.id) { mutableStateOf(chore?.approval ?: false) }
    var steps by rememberSaveable(chore?.id) { mutableStateOf(chore?.steps?.joinToString("\n") ?: defaultSteps(element).joinToString("\n")) }
    var error by remember { mutableStateOf("") }
    val fieldColors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
    Column(Modifier.fillMaxSize().background(CanvasColor).statusBarsPadding().navigationBarsPadding().imePadding()) {
        BackHeader(if (chore == null) "A brand-new quest" else "Edit quest", onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(17.dp)) {
            Text("Small tasks, big possibilities.", style = MaterialTheme.typography.headlineMedium)
            OutlinedTextField(title, { title = it.take(48) }, Modifier.fillMaxWidth(), label = { Text("Quest name") }, placeholder = { Text("e.g. Water the plants") }, singleLine = true, shape = RoundedCornerShape(16.dp), colors = fieldColors)
            OutlinedTextField(description, { description = it.take(180) }, Modifier.fillMaxWidth(), label = { Text("A little encouragement") }, minLines = 2, shape = RoundedCornerShape(16.dp), colors = fieldColors)
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(points, { points = it.filter(Char::isDigit).take(3) }, Modifier.width(135.dp), label = { Text("Points (1–100)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, shape = RoundedCornerShape(16.dp), colors = fieldColors)
                Spacer(Modifier.width(12.dp)); Text("An easy quest might earn 5–10 points. A bigger one, 15–25.", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
            }
            Text("Creature family", style = MaterialTheme.typography.titleSmall)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Element.entries.forEach { item -> FilterChip(element == item, { element = item; if (chore == null) steps = defaultSteps(item).joinToString("\n") }, { Text(item.label) }, leadingIcon = { Icon(item.icon, null, Modifier.size(16.dp), tint = item.color) }) }
            }
            Text("Assign to your adventurers", style = MaterialTheme.typography.titleSmall)
            state.members.chunked(3).forEach { row -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { member -> FilterChip(member.id in assigned, { assigned = if (member.id in assigned) assigned - member.id else assigned + member.id }, { Text(member.name) }, leadingIcon = { Avatar(member, size = 22.dp) }) }
            } }
            Text("Repeat schedule", style = MaterialTheme.typography.titleSmall)
            Schedule.entries.chunked(2).forEach { row -> Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) { row.forEach { item -> FilterChip(schedule == item, { schedule = item }, { Text(item.label) }) } } }
            OutlinedTextField(steps, { steps = it.take(600) }, Modifier.fillMaxWidth(), label = { Text("Helpful steps (one per line)") }, supportingText = { Text("Add 1–8 steps. Keep them simple and age appropriate.") }, minLines = 4, shape = RoundedCornerShape(16.dp), colors = fieldColors)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text("Grown-up thumbs up", style = MaterialTheme.typography.titleSmall); Text("Review before rewards are earned", style = MaterialTheme.typography.bodySmall) }
                Switch(approval, { approval = it })
            }
            if (error.isNotEmpty()) Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            GameButton(if (chore == null) "Create quest" else "Save changes", {
                val candidate = Chore(chore?.id ?: UUID.randomUUID().toString(), title.trim(), description.trim().ifBlank { "One little step toward a brighter home." }, points.toIntOrNull() ?: 0, element, assigned, steps.lines().map(String::trim).filter(String::isNotEmpty), schedule, approval, chore?.archived ?: false)
                runCatching { GameRules.validateChore(candidate, state); onSave(candidate) }.onFailure { error = it.message ?: "Check your quest details." }
            }, Modifier.fillMaxWidth(), Color(0xFFFFAD23), Icons.Rounded.AutoAwesome)
            Spacer(Modifier.height(15.dp))
        }
    }
}

@Composable fun ParentPinDialog(state: GameState, onCancel: () -> Unit, onUnlock: () -> Unit) {
    var pin by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf(false) }
    var attempts by rememberSaveable { mutableIntStateOf(0) }
    var lockUntil by rememberSaveable { mutableLongStateOf(0L) }
    var seconds by remember { mutableIntStateOf(0) }
    LaunchedEffect(lockUntil) { while (System.currentTimeMillis() < lockUntil) { seconds = ((lockUntil - System.currentTimeMillis()) / 1000).toInt() + 1; kotlinx.coroutines.delay(500) }; seconds = 0 }
    AlertDialog(onCancel, icon = { Icon(Icons.Rounded.Lock, null, tint = Purple) }, title = { Text("Just for grown-ups") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Enter the four-digit parent PIN to manage your household.", style = MaterialTheme.typography.bodyMedium)
            OutlinedTextField(pin, { pin = it.filter(Char::isDigit).take(4); error = false }, Modifier.fillMaxWidth(), label = { Text("Parent PIN") }, visualTransformation = PasswordVisualTransformation(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), shape = RoundedCornerShape(15.dp), isError = error, enabled = seconds == 0)
            if (error) Text("That PIN didn't match. Try again.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            if (seconds > 0) Text("Take a little break. Try again in ${seconds}s.", style = MaterialTheme.typography.bodySmall)
        }
    }, confirmButton = { TextButton({
        if (ParentPin.verify(pin, state)) onUnlock() else { error = true; pin = ""; attempts++; if (attempts >= 5) { lockUntil = System.currentTimeMillis() + 30000; attempts = 0 } }
    }, enabled = pin.length == 4 && seconds == 0) { Text("Unlock") } }, dismissButton = { TextButton(onCancel) { Text("Back to play") } })
}
