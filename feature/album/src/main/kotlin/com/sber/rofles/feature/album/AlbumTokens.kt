package com.sber.rofles.feature.album

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp

internal object AlbumTokens {
    val Background = Color(0xFFF2F5F7)
    val Surface = Color.White
    val TextPrimary = Color.Black
    val TextSecondary = Color(0xFFA8B4BD)
    val Accent = Color(0xFF19D13B)
    val Track = Color(0xFFE1E7EB)
    val Silhouette = Color(0xFFE4E9ED)
    val SilhouetteMark = Color(0xFFC3CDD4)
    val ShareStart = Color(0xFF3ED9FF)
    val ShareEnd = Color(0xFFA6FF2D)

    val CardShape = RoundedCornerShape(20.dp)
    val ButtonHeight = 56.dp
    val ScreenPadding = 16.dp

    val ShareBrush: Brush get() = Brush.horizontalGradient(listOf(ShareStart, ShareEnd))
}

internal fun argbColor(value: Long): Color = Color(value.toInt())

internal fun Color.soft(amount: Float = 0.45f): Color = lerp(this, Color.White, amount)
