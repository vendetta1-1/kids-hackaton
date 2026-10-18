package com.sber.rofles.feature.demo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/** Служебный экран для демо: версия конфига, RSSI, обновить. UI сделаем позже. */
@Composable
fun DemoScreen(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Служебный экран (заглушка)")
    }
}