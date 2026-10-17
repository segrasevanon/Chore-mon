package com.choreemon.app.ui

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.choreemon.app.data.*
import java.time.LocalDate

@Composable fun CreaturesScreen(state: GameState, onSelect: (Creature) -> Unit, onHatch: () -> Unit) {
    var filter by rememberSaveable { mutableStateOf("All") }
    val visible = state.team.filter { filter == "All" || filter == it.element.label || filter == "Favorites" && it.favorite }
    val discovered = state.team.flatMap { pet -> (0..pet.stage).map { "${pet.element}:$it" } }.toSet().size
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(vertical = 20.dp), verticalArrangement = Arrangement.spacedBy(17.dp)) {
        Column(Modifier.padding(horizontal = 20.dp)) {
            PageHeading("YOUR VERY OWN LITTLE WORLD", "Meet your buddies", "Collect, care, and grow a little closer.")
            Spacer(Modifier.height(13.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Pill("${state.team.size} buddies", Purple, icon = Icons.Rounded.Pets); Pill("$discovered / 12 forms", Blue, icon = Icons.Rounded.AutoAwesome) }
        }
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("All", "Water", "Earth", "Air", "Poison", "Favorites").forEach { item ->
                FilterChip(filter == item, { filter = item }, { Text(item) }, leadingIcon = if (item == "Favorites") { { Icon(Icons.Rounded.Favorite, null, Modifier.size(16.dp)) } } else null)
            }
        }
        if (visible.isEmpty()) Box(Modifier.padding(horizontal = 20.dp)) { EmptyState("A new friend is out there", "Hatch eggs to discover this part of your collection.", Icons.Rounded.Pets, "Visit hatchery", onHatch) }
        visible.chunked(2).forEach { row ->
            Row(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { pet ->
                    GameCard(Modifier.weight(1f), pet.element.pale, { onSelect(pet) }) {
                        Box(Modifier.fillMaxWidth()) {
                            Pill("Lv. ${pet.level}", pet.element.color, Color.White.copy(alpha = .8f), modifier = Modifier.align(Alignment.TopStart))
                            if (pet.id == state.companion.id) Icon(Icons.Rounded.Stars, "Active companion", Modifier.align(Alignment.TopEnd).size(22.dp), tint = Gold)
                        }
                        PetArt(pet, Modifier.fillMaxWidth().height(130.dp), mood = GameRules.mood(state, pet, LocalDate.now()))
                        Text(pet.name, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                        Spacer(Modifier.height(3.dp))
                        Text("${pet.rarity.label} • ${pet.element.label}", style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(8.dp))
                        val mood = GameRules.mood(state, pet, LocalDate.now())
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(if (mood == Mood.HAPPY) Icons.Rounded.SentimentVerySatisfied else if (mood == Mood.CONTENT) Icons.Rounded.SentimentSatisfied else Icons.Rounded.Bedtime, null, Modifier.size(16.dp), tint = if (mood == Mood.HAPPY) Green else Gold)
                            Spacer(Modifier.width(5.dp)); Text(mood.label, style = MaterialTheme.typography.labelMedium, color = Ink)
                        }
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
        Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionTitle("The field guide")
            Element.entries.forEach { element ->
                val collected = state.team.filter { it.element == element }
                GameCard(Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(element.icon, null, Modifier.size(27.dp), tint = element.color)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text("${element.label} family", style = MaterialTheme.typography.titleSmall)
                            Text(element.names.joinToString("  ›  "), style = MaterialTheme.typography.bodySmall)
                        }
                        Icon(if (collected.isEmpty()) Icons.Rounded.Lock else Icons.Rounded.CheckCircle, if (collected.isEmpty()) "Undiscovered" else "Discovered", Modifier.size(21.dp), tint = if (collected.isEmpty()) Muted else Green)
                    }
                }
            }
        }
        Spacer(Modifier.height(15.dp))
    }
}

@Composable fun CreatureDetail(state: GameState, creature: Creature, onBack: () -> Unit, onName: () -> Unit, onLove: () -> Unit, onFavorite: () -> Unit, onChoose: () -> Unit, onEvolve: () -> Unit) {
    val pet = state.creatures.firstOrNull { it.id == creature.id } ?: creature
    val mood = GameRules.mood(state, pet, LocalDate.now())
    val loved = pet.lovedOn == LocalDate.now().toString()
    Column(Modifier.fillMaxSize().background(CanvasColor).statusBarsPadding().navigationBarsPadding()) {
        BackHeader("Buddy journal", onBack) {
            IconButton(onFavorite) { Icon(if (pet.favorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, if (pet.favorite) "Remove favorite" else "Add favorite", tint = Pink) }
            IconButton(onName) { Icon(Icons.Rounded.Edit, "Rename buddy", tint = Blue) }
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            GameCard(Modifier.fillMaxWidth(), pet.element.pale) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Pill(pet.rarity.label, pet.rarity.color, icon = Icons.Rounded.WorkspacePremium); Pill("Lv. ${pet.level}", Blue, icon = Icons.Rounded.Bolt) }
                PetArt(pet, Modifier.align(Alignment.CenterHorizontally).size(238.dp), onPet = onLove, mood = mood)
                Text(pet.name, style = MaterialTheme.typography.headlineLarge, modifier = Modifier.align(Alignment.CenterHorizontally))
                Text(if (pet.nickname.isNotBlank()) pet.species else "${pet.element.label} • ${if (pet.stage == 0) "Little buddy" else if (pet.stage == 1) "Growing adventurer" else "Household legend"}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.align(Alignment.CenterHorizontally))
                Spacer(Modifier.height(12.dp))
                Pill("Mood: ${mood.label}", if (mood == Mood.HAPPY) Green else Color(0xFFBC8C31), icon = Icons.Rounded.SentimentSatisfiedAlt, modifier = Modifier.align(Alignment.CenterHorizontally))
                Spacer(Modifier.height(12.dp)); Text(pet.element.description, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
            GameCard(Modifier.fillMaxWidth()) {
                Text("A little love goes a long way", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp)); Text(when (mood) { Mood.HAPPY -> "Feeling loved! Happy buddies earn a little extra XP."; Mood.CONTENT -> "A small quest can bring that sparkle back."; Mood.SAD -> "Your buddy is resting. Start small, whenever you're ready." }, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(13.dp))
                GameButton(if (loved) "Feeling the love!" else "Send a little love", onLove, Modifier.fillMaxWidth(), Pink, Icons.Rounded.Favorite, !loved)
                Spacer(Modifier.height(8.dp)); Text("Daily love lifts mood by one step. Completing chores keeps the whole team happy.", style = MaterialTheme.typography.bodySmall)
            }
            GameCard(Modifier.fillMaxWidth()) {
                Text("Growing into something great", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(15.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    pet.element.names.forEachIndexed { index, name ->
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            PetArt(pet.copy(stage = index, nickname = ""), Modifier.fillMaxWidth().height(86.dp).alpha(if (pet.stage >= index) 1f else .38f), animate = false)
                            Text(name, style = MaterialTheme.typography.labelSmall, color = if (pet.stage >= index) Ink else Muted, maxLines = 1, fontSize = 10.sp)
                            Text(listOf("Lv. 1", "Lv. 10", "Lv. 25")[index], style = MaterialTheme.typography.bodySmall)
                            if (pet.stage == index) Icon(Icons.Rounded.ArrowDropUp, "Current form", tint = Blue)
                        }
                    }
                }
                Spacer(Modifier.height(15.dp))
                val target = pet.nextEvolutionXp
                if (target != null) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Evolution progress", style = MaterialTheme.typography.labelMedium); Text("${pet.xp} / $target XP", style = MaterialTheme.typography.bodySmall) }
                    Spacer(Modifier.height(9.dp)); XpBar(pet.xp.toFloat() / target, color = Green)
                    Spacer(Modifier.height(12.dp)); Text(if (pet.canEvolve) "Your buddy is ready for a big moment!" else "${target - pet.xp} more XP to ${pet.element.names[pet.stage + 1]}.", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(12.dp)); GameButton(if (pet.canEvolve) "Evolve ${pet.name}!" else "Growing with every quest", onEvolve, Modifier.fillMaxWidth(), Purple, Icons.Rounded.AutoAwesome, pet.canEvolve)
                } else Pill("Final form! You're a household legend.", Color(0xFFB9820C), icon = Icons.Rounded.WorkspacePremium)
            }
            GameButton(if (state.companion.id == pet.id) "Your adventure buddy" else "Make my adventure buddy", onChoose, Modifier.fillMaxWidth(), Blue, Icons.Rounded.Pets, state.companion.id != pet.id)
            Text("Together since ${LocalDate.parse(pet.adopted).format(java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy"))}", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(15.dp))
        }
    }
}
