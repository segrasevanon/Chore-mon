@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
package com.choreemon.app.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.choreemon.app.R
import com.choreemon.app.data.*
import kotlin.math.*

val LocalMotion = staticCompositionLocalOf { true }

val Element.sprite get() = when (this) { Element.WATER -> R.drawable.sudsy; Element.EARTH -> R.drawable.sockmite; Element.AIR -> R.drawable.dustbunny; Element.POISON -> R.drawable.stinkle }
val Creature.sprite get() = when (element) {
    Element.WATER -> listOf(R.drawable.sudsy, R.drawable.bubblescale, R.drawable.dishdrake)[stage.coerceIn(0, 2)]
    Element.EARTH -> listOf(R.drawable.sockmite, R.drawable.sockgolem, R.drawable.socktitan)[stage.coerceIn(0, 2)]
    Element.AIR -> listOf(R.drawable.dustbunny, R.drawable.whirlpup, R.drawable.vacuumphant)[stage.coerceIn(0, 2)]
    Element.POISON -> listOf(R.drawable.stinkle, R.drawable.grubsong, R.drawable.trashorok)[stage.coerceIn(0, 2)]
}
val Element.icon get() = when (this) { Element.WATER -> Icons.Rounded.WaterDrop; Element.EARTH -> Icons.Rounded.Eco; Element.AIR -> Icons.Rounded.Air; Element.POISON -> Icons.Rounded.Recycling }
val Element.choreIcon get() = when (this) { Element.WATER -> Icons.Rounded.LocalDining; Element.EARTH -> Icons.Rounded.LocalLaundryService; Element.AIR -> Icons.Rounded.CleaningServices; Element.POISON -> Icons.Rounded.DeleteOutline }

@Composable fun GameCard(modifier: Modifier = Modifier, color: Color = Color.White, onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(24.dp)
    Column(modifier.shadow(5.dp, shape, ambientColor = Ink.copy(alpha = .07f), spotColor = Ink.copy(alpha = .07f))
        .clip(shape).background(color).border(1.5.dp, Color.White.copy(alpha = .9f), shape)
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier).padding(16.dp), content = content)
}

@Composable fun GameButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, color: Color = Blue, icon: ImageVector? = null, enabled: Boolean = true) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) .96f else 1f, spring(stiffness = Spring.StiffnessMedium), label = "button press")
    Button(onClick, modifier.graphicsLayer { scaleX = scale; scaleY = scale }.heightIn(min = 52.dp),
        enabled = enabled, shape = RoundedCornerShape(18.dp), interactionSource = source,
        colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = Color.White, disabledContainerColor = Color(0xFFE0E8F1), disabledContentColor = Muted),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp, pressedElevation = 0.dp), contentPadding = PaddingValues(horizontal = 19.dp, vertical = 13.dp)) {
        if (icon != null) { Icon(icon, null, Modifier.size(21.dp)); Spacer(Modifier.width(8.dp)) }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable fun Pill(text: String, color: Color = Blue, background: Color = color.copy(alpha = .11f), icon: ImageVector? = null, modifier: Modifier = Modifier) {
    Row(modifier.clip(CircleShape).background(background).padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        if (icon != null) Icon(icon, null, Modifier.size(15.dp), tint = color)
        Text(text, style = MaterialTheme.typography.labelMedium, color = color)
    }
}

@Composable fun Avatar(member: Member, modifier: Modifier = Modifier, size: Dp = 46.dp) {
    val color = listOf(Pink, Blue, Purple, Green)[member.avatar % 4]
    Box(modifier.size(size).clip(CircleShape).background(color.copy(alpha = .14f)).border(2.dp, Color.White, CircleShape), contentAlignment = Alignment.Center) {
        Icon(when (member.avatar % 4) { 0 -> Icons.Rounded.Face3; 1 -> Icons.Rounded.Face6; 2 -> Icons.Rounded.Face4; else -> Icons.Rounded.Face }, null, Modifier.size(size * .79f), tint = color)
    }
}

@Composable fun Brand(modifier: Modifier = Modifier, size: Int = 29) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Text("Choreémon", fontFamily = Nunito, fontWeight = FontWeight.Black, fontSize = size.sp, color = Color(0xFFFFC63B),
            style = androidx.compose.ui.text.TextStyle(shadow = Shadow(Color(0xFF174998), Offset(0f, 5f), 0f), letterSpacing = (-1.3).sp))
        Image(painterResource(R.drawable.egg), null, Modifier.size((size + 8).dp))
    }
}

@Composable fun SectionTitle(title: String, action: String? = null, onAction: () -> Unit = {}) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        if (action != null) TextButton(onAction, contentPadding = PaddingValues(horizontal = 4.dp)) { Text(action, style = MaterialTheme.typography.labelMedium); Icon(Icons.Rounded.ChevronRight, null, Modifier.size(16.dp)) }
    }
}

@Composable fun PageHeading(eyebrow: String, title: String, subtitle: String) {
    Column(Modifier.fillMaxWidth()) {
        Text(eyebrow.uppercase(), style = MaterialTheme.typography.labelSmall, color = Blue)
        Spacer(Modifier.height(4.dp))
        Text(title, style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(4.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable fun PetArt(creature: Creature, modifier: Modifier = Modifier, animate: Boolean = true, onPet: (() -> Unit)? = null, mood: Mood = Mood.HAPPY) {
    val motion = LocalMotion.current && animate
    val transition = rememberInfiniteTransition(label = "buddy breathing")
    val bob by transition.animateFloat(-3f, 4f, infiniteRepeatable(tween(if (mood == Mood.SAD) 3200 else 1900, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "buddy float")
    val breathe by transition.animateFloat(1f, 1.025f, infiniteRepeatable(tween(1900), RepeatMode.Reverse), label = "buddy breathe")
    Box(modifier, contentAlignment = Alignment.Center) {
        if (creature.rarity == Rarity.LEGENDARY) Box(Modifier.matchParentSize().background(Brush.radialGradient(listOf(Gold.copy(alpha = .45f), Color.Transparent)), CircleShape))
        val resource = if (mood == Mood.SAD && creature.stage == 0) when (creature.element) {
            Element.WATER -> R.drawable.sudsy_sad; Element.EARTH -> R.drawable.sockmite_sad
            Element.AIR -> R.drawable.dustbunny_sad; Element.POISON -> R.drawable.stinkle_sad
        } else creature.sprite
        Image(painterResource(resource), creature.name, Modifier.fillMaxSize()
            .graphicsLayer { if (motion) { translationY = bob; scaleX = breathe; scaleY = breathe }; rotationZ = if (mood == Mood.SAD) -4f else if (creature.stage == 2) -3f else 0f }
            .then(if (onPet != null) Modifier.clickable(onClickLabel = "Pet ${creature.name}", onClick = onPet) else Modifier), contentScale = ContentScale.Fit)
        if (creature.stage > 0) Icon(if (creature.stage == 2) Icons.Rounded.WorkspacePremium else Icons.Rounded.AutoAwesome,
            "Evolution stage ${creature.stage + 1}", Modifier.align(Alignment.TopEnd).size(34.dp), tint = if (creature.stage == 2) Gold else creature.element.color)
    }
}

@Composable fun EggArt(modifier: Modifier = Modifier, wobble: Boolean = false, kind: EggKind = EggKind.CLASSIC) {
    val motion = LocalMotion.current
    val t = rememberInfiniteTransition(label = "egg wobble")
    val angle by t.animateFloat(-7f, 7f, infiniteRepeatable(tween(if (wobble) 110 else 1700, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "egg rotation")
    Image(painterResource(R.drawable.egg), "${kind.label} egg", modifier.graphicsLayer {
        rotationZ = if (motion) angle else 0f
    }, colorFilter = if (kind == EggKind.AURORA) ColorFilter.tint(Purple.copy(alpha = .22f), BlendMode.SrcAtop) else null)
}

@Composable fun XpBar(progress: Float, modifier: Modifier = Modifier, color: Color = Blue, track: Color = Color(0xFFE3EBF5)) {
    val value by animateFloatAsState(progress.coerceIn(0f, 1f), tween(if (LocalMotion.current) 800 else 0), label = "XP progress")
    LinearProgressIndicator(progress = { value }, modifier.fillMaxWidth().height(9.dp).clip(CircleShape), color = color, trackColor = track, gapSize = 0.dp, drawStopIndicator = {})
}

@Composable fun EmptyState(title: String, body: String, icon: ImageVector = Icons.Rounded.AutoAwesome, action: String? = null, onAction: () -> Unit = {}) {
    GameCard(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(vertical = 18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, Modifier.size(45.dp), tint = Blue)
            Spacer(Modifier.height(12.dp)); Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(5.dp)); Text(body, style = MaterialTheme.typography.bodyMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            if (action != null) { Spacer(Modifier.height(16.dp)); GameButton(action, onAction) }
        }
    }
}

@Composable fun Confetti(modifier: Modifier = Modifier, burst: Boolean = true) {
    if (!LocalMotion.current) return
    val t = rememberInfiniteTransition(label = "celebration")
    val time by t.animateFloat(0f, 1f, infiniteRepeatable(tween(if (burst) 3000 else 6500, easing = LinearEasing)), label = "particles")
    val colors = listOf(Gold, Pink, Blue, Green, Purple)
    Canvas(modifier.clearAndSetSemantics { }) {
        repeat(if (burst) 36 else 13) { i ->
            val x = ((i * .618f) % 1f) * size.width
            val f = (time + i * .137f) % 1f
            val y = f * size.height
            val sway = sin(f * 9f + i) * 25f
            val alpha = if (burst) .9f else .55f
            rotate(degrees = f * 300f + i * 15f, pivot = Offset(x + sway, y)) {
                drawRoundRect(colors[i % colors.size].copy(alpha = alpha), Offset(x + sway, y), androidx.compose.ui.geometry.Size(5.dp.toPx(), 9.dp.toPx()), androidx.compose.ui.geometry.CornerRadius(2f))
            }
        }
    }
}

@Composable fun BackHeader(title: String, onBack: () -> Unit, trailing: @Composable (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onBack) { Icon(Icons.Rounded.ArrowBack, "Back") }
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        trailing?.invoke()
    }
}
