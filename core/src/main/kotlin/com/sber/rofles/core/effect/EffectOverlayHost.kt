package com.sber.rofles.core.effect

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.delay

/**
 * Глобальный оверлей-хост эффекта. Рисуется поверх любого экрана,
 * касания проходят насквозь (Box без обработки кликов).
 */
@Composable
fun EffectOverlayHost(
    animationJson: String?,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (animationJson == null) return

    val composition by rememberLottieComposition(
        LottieCompositionSpec.JsonString(animationJson)
    )

    LaunchedEffect(composition) {
        // Эффект живёт ~3 секунды, потом закрываемся.
        delay(3000L)
        onDone()
    }

    Box(modifier.fillMaxSize()) {
        LottieAnimation(
            composition = composition,
            iterations = 1,
            contentScale = ContentScale.Crop,
        )
    }
}