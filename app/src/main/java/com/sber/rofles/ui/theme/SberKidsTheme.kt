package com.sber.rofles.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Цвета СберKids — сняты с макета Figma («Карта экранов»). */
@Immutable
data class SkColors(
    val background: Color = Color(0xFFF2F5F7),
    val surface: Color = Color.White,
    val textPrimary: Color = Color(0xFF000000),
    val textSecondary: Color = Color(0xFFA8B4BD),
    val textTertiary: Color = Color(0xFF7D868C),
    val iconMono: Color = Color(0xFFA3C2DB),
    val green: Color = Color(0xFF19D13B),
    val toggleOn: Color = Color(0xFF3AE6A1),
    val toggleOff: Color = Color(0xFFDDE5EB),
    val blue: Color = Color(0xFF4DB0FF),
    val lime: Color = Color(0xFF8CFF2E),
    val progressTrack: Color = Color(0xFFE6ECF0),
    val blobPink: Color = Color(0xFFFF5297),
    val blobYellow: Color = Color(0xFFF1DB48),
    val purple: Color = Color(0xFF8B5CF6),
)

/** Скругления макета: карточка 20, «склейка» ячеек 4, таббар 24. */
@Immutable
data class SkShapes(
    val card: RoundedCornerShape = RoundedCornerShape(20.dp),
    val button: RoundedCornerShape = RoundedCornerShape(20.dp),
    val tabBar: RoundedCornerShape = RoundedCornerShape(24.dp),
    val small: RoundedCornerShape = RoundedCornerShape(12.dp),
)

/** Типографика. Фирменный SB Sans не публичный — берём системный шрифт с теми же весами и кеглями. */
@Immutable
data class SkTypography(
    val balance: TextStyle = TextStyle(fontSize = 32.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold),
    val headline: TextStyle = TextStyle(fontSize = 22.sp, lineHeight = 26.sp, fontWeight = FontWeight.Bold),
    val title: TextStyle = TextStyle(fontSize = 18.sp, lineHeight = 22.sp, fontWeight = FontWeight.Bold),
    val navTitle: TextStyle = TextStyle(fontSize = 17.sp, lineHeight = 22.sp, fontWeight = FontWeight.Bold),
    val body: TextStyle = TextStyle(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
    val bodyRegular: TextStyle = TextStyle(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.Normal),
    val footnote: TextStyle = TextStyle(fontSize = 13.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium),
    val caption: TextStyle = TextStyle(fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.Medium),
    val button: TextStyle = TextStyle(fontSize = 17.sp, lineHeight = 22.sp, fontWeight = FontWeight.Bold),
)

/** Градиенты кнопок и баннеров из макета. */
@Immutable
data class SkGradients(
    /** «Создать копилку», «Круто!», «Посмотреть все» — голубой → салатовый. */
    val primaryButton: Brush = Brush.horizontalGradient(listOf(Color(0xFF3ED9FF), Color(0xFFA6FF2D))),
    /** Рекламный баннер на Главном. */
    val banner: Brush = Brush.horizontalGradient(listOf(Color(0xFFBB66F8), Color(0xFF4411A6))),
)

val LocalSkColors = staticCompositionLocalOf { SkColors() }
val LocalSkShapes = staticCompositionLocalOf { SkShapes() }
val LocalSkTypography = staticCompositionLocalOf { SkTypography() }
val LocalSkGradients = staticCompositionLocalOf { SkGradients() }

/** Доступ к токенам: `SberKids.colors.background` и т. д. */
object SberKids {
    val colors: SkColors @Composable get() = LocalSkColors.current
    val shapes: SkShapes @Composable get() = LocalSkShapes.current
    val typography: SkTypography @Composable get() = LocalSkTypography.current
    val gradients: SkGradients @Composable get() = LocalSkGradients.current
}

/** Тема оболочки СберKids. Приложение всегда светлое, как в макете. */
@Composable
fun SberKidsTheme(content: @Composable () -> Unit) {
    val colors = SkColors()
    val typography = SkTypography()
    val material = lightColorScheme(
        primary = colors.green,
        onPrimary = Color.White,
        background = colors.background,
        onBackground = colors.textPrimary,
        surface = colors.surface,
        onSurface = colors.textPrimary,
    )
    androidx.compose.runtime.CompositionLocalProvider(
        LocalSkColors provides colors,
        LocalSkShapes provides SkShapes(),
        LocalSkTypography provides typography,
        LocalSkGradients provides SkGradients(),
    ) {
        MaterialTheme(
            colorScheme = material,
            typography = Typography(bodyLarge = typography.bodyRegular.copy(fontFamily = FontFamily.Default)),
            content = content,
        )
    }
}
