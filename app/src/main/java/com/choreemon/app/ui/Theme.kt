package com.choreemon.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.choreemon.app.R
import com.choreemon.app.data.Element
import com.choreemon.app.data.Rarity

val Ink = Color(0xFF18345D)
val Muted = Color(0xFF73849E)
val Blue = Color(0xFF2089F5)
val Sky = Color(0xFFE9F6FF)
val Gold = Color(0xFFFFB829)
val Green = Color(0xFF27BE89)
val Purple = Color(0xFF9470EC)
val Pink = Color(0xFFF273A8)
val CanvasColor = Color(0xFFF5F9FD)
val Nunito = FontFamily(
    Font(R.font.nunito_regular, FontWeight.Normal), Font(R.font.nunito_medium, FontWeight.Medium),
    Font(R.font.nunito_semibold, FontWeight.SemiBold), Font(R.font.nunito_bold, FontWeight.Bold),
    Font(R.font.nunito_extrabold, FontWeight.ExtraBold), Font(R.font.nunito_black, FontWeight.Black)
)
private val type = Typography(
    displaySmall = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Black, fontSize = 34.sp, lineHeight = 39.sp, color = Ink),
    headlineLarge = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Black, fontSize = 30.sp, lineHeight = 36.sp, color = Ink),
    headlineMedium = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, lineHeight = 32.sp, color = Ink),
    titleLarge = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 21.sp, lineHeight = 27.sp, color = Ink),
    titleMedium = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, lineHeight = 21.sp, color = Ink),
    titleSmall = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 19.sp, color = Ink),
    bodyLarge = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 23.sp, color = Ink),
    bodyMedium = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp, color = Muted),
    bodySmall = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 17.sp, color = Muted),
    labelLarge = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, lineHeight = 14.sp, letterSpacing = .6.sp)
)
@Composable fun ChoreemonTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = lightColorScheme(primary = Blue, secondary = Purple, tertiary = Gold, background = CanvasColor, surface = Color.White, onSurface = Ink, onBackground = Ink, outline = Color(0xFFDDE6F2)), typography = type, content = content)
}
val Element.color get() = when (this) { Element.WATER -> Blue; Element.EARTH -> Color(0xFFDEA148); Element.AIR -> Purple; Element.POISON -> Color(0xFF54AA7B) }
val Element.pale get() = when (this) { Element.WATER -> Color(0xFFE4F8FF); Element.EARTH -> Color(0xFFFFF2DD); Element.AIR -> Color(0xFFF0EAFE); Element.POISON -> Color(0xFFE9F7EB) }
val Rarity.color get() = when (this) { Rarity.COMMON -> Blue; Rarity.RARE -> Purple; Rarity.LEGENDARY -> Color(0xFFBD7A07) }
