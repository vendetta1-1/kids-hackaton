package com.sber.rofles.core.prank

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object PrankPalette {
    val background = Color(0xFFF2F5F7)
    val surface = Color.White
    val text = Color(0xFF000000)
    val secondary = Color(0xFFA8B4BD)
    val tertiary = Color(0xFF7D868C)
    val iconMono = Color(0xFFA3C2DB)
    val green = Color(0xFF19D13B)
    val greenSoft = Color(0xFFE3F9E7)
    val outline = Color(0xFFE6ECF0)
    val disabled = Color(0xFFE6ECF0)
    val primaryButton = Brush.horizontalGradient(listOf(Color(0xFF3ED9FF), Color(0xFFA6FF2D)))
    val card = RoundedCornerShape(20.dp)
}

fun Long.toColor(): Color = Color(this)
