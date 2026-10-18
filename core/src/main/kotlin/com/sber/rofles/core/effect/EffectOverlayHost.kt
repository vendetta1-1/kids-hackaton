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
 *
 * @param animation URL (LottieCompositionSpec.Url) или JSON-строка анимации.
 */
@Composable
fun EffectOverlayHost(
    animation: String?,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (animation == null) return

    val spec = if (animation.startsWith("http")) {
        LottieCompositionSpec.Url(animation)
    } else {
        LottieCompositionSpec.JsonString(animation)
    }
    val composition by rememberLottieComposition(spec)

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