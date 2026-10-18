package com.sber.rofles.feature.demo

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.sber.rofles.core.prank.BluetoothPermissions
import com.sber.rofles.core.prank.PrankArt
import com.sber.rofles.core.prank.PrankBackTopBar
import com.sber.rofles.core.prank.PrankPalette
import com.sber.rofles.core.prank.PrankPrimaryButton
import com.sber.rofles.core.prank.PrankType
import com.sber.rofles.domain.handshake.Phase
import com.sber.rofles.domain.model.BleCapability

private val Warning = Color(0xFFFF8A3D)

@Composable
fun DemoScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: DemoViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()

    DisposableEffect(Unit) {
        viewModel.startRadar()
        onDispose { viewModel.stopRadar() }
    }

    val enableBluetooth = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        viewModel.startRadar()
    }
    val requestPermissions = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        viewModel.startRadar()
    }

    Column(
        modifier
            .fillMaxSize()
            .background(PrankPalette.background),
    ) {
        PrankBackTopBar(title = "Служебный экран", onBack = onBack)
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            RadarCard(
                state = state,
                onFix = {
                    when (state.capability) {
                        BleCapability.NO_PERMISSION -> requestPermissions.launch(BluetoothPermissions.required)
                        BleCapability.OFF -> runCatching { enableBluetooth.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)) }
                        else -> viewModel.startRadar()
                    }
                },
            )
            ConfigCard(state = state, onRefresh = viewModel::refreshConfig)
            MeCard(state = state, onChangeElement = viewModel.controller::openElementPicker)
            Section(title = "Показать без второго телефона") {
                PrankPrimaryButton("Показать «Дай пять!»", onClick = viewModel.controller::previewHighFive)
                Spacer(Modifier.height(8.dp))
                SecondaryButton("Показать соло-эффект", onClick = viewModel.controller::previewSolo)
            }
        }
    }
}

@Composable
private fun RadarCard(state: DemoUiState, onFix: () -> Unit) {
    Section(
        title = "Кто рядом",
        trailing = { RadarBadge(on = state.radarOn) },
    ) {
        when {
            state.capability != BleCapability.FULL -> {
                Text(capabilityText(state.capability), style = PrankType.secondary)
                if (state.capability != BleCapability.UNSUPPORTED) {
                    Spacer(Modifier.height(12.dp))
                    PrankPrimaryButton(
                        if (state.capability == BleCapability.NO_PERMISSION) "Разрешить Bluetooth" else "Включить Bluetooth",
                        onClick = onFix,
                    )
                }
            }
            state.neighbors.isEmpty() -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(painterResource(PrankArt.bluetooth), contentDescription = null, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Пока никого. Открой этот экран на втором телефоне и поднеси его ближе",
                        style = PrankType.secondary,
                    )
                }
            }
            else -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.neighbors.forEach { NeighborRow(it, rssiMin = state.config?.rssiMin ?: -55) }
            }
        }
    }
}

@Composable
private fun NeighborRow(neighbor: Neighbor, rssiMin: Int) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(PrankPalette.background)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(48.dp)
                .background(PrankPalette.surface, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (neighbor.element != null) {
                Image(painterResource(PrankArt.element(neighbor.element)), contentDescription = null, modifier = Modifier.size(40.dp))
            } else {
                Text("?", style = PrankType.title.copy(color = PrankPalette.secondary))
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(neighbor.elementTitle, style = PrankType.body)
            Text(
                "${if (neighbor.phase == Phase.HELLO) "Привет" else "Подтверждает"} · " +
                    "#${neighbor.nonce.toString(16).uppercase()} · аватар ${neighbor.avatar}",
                style = PrankType.footnote.copy(color = PrankPalette.secondary),
            )
            Spacer(Modifier.height(6.dp))
            SignalBar(rssi = neighbor.medianRssi, rssiMin = rssiMin)
        }
        Spacer(Modifier.width(12.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text("${neighbor.medianRssi}", style = PrankType.title.copy(fontSize = 20.sp))
            Text("дБм", style = PrankType.footnote.copy(color = PrankPalette.secondary))
            Spacer(Modifier.height(4.dp))
            Text(
                if (neighbor.close) "рядом" else "далеко",
                style = PrankType.footnote.copy(
                    color = if (neighbor.close) PrankPalette.green else Warning,
                    fontWeight = FontWeight.Bold,
                ),
            )
        }
    }
}

@Composable
private fun SignalBar(rssi: Int, rssiMin: Int) {
    val fraction = ((rssi + 100) / 70f).coerceIn(0.05f, 1f)
    val threshold = ((rssiMin + 100) / 70f).coerceIn(0f, 1f)
    Box(
        Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(CircleShape)
            .background(PrankPalette.outline),
    ) {
        Box(
            Modifier
                .fillMaxWidth(fraction)
                .height(6.dp)
                .clip(CircleShape)
                .background(if (rssi >= rssiMin) PrankPalette.green else Warning),
        )
        Box(
            Modifier
                .fillMaxWidth(threshold)
                .height(6.dp),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Box(
                Modifier
                    .width(2.dp)
                    .height(6.dp)
                    .background(PrankPalette.text.copy(alpha = 0.5f)),
            )
        }
    }
}

@Composable
private fun RadarBadge(on: Boolean) {
    val pulse by rememberInfiniteTransition(label = "radar").animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "pulse",
    )
    Row(
        Modifier
            .background(if (on) PrankPalette.greenSoft else PrankPalette.background, CircleShape)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(8.dp)
                .graphicsLayer { alpha = if (on) pulse else 1f }
                .background(if (on) PrankPalette.green else PrankPalette.secondary, CircleShape),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            if (on) "Сканирую" else "Выключен",
            style = PrankType.footnote.copy(color = if (on) PrankPalette.green else PrankPalette.tertiary),
        )
    }
}

@Composable
private fun ConfigCard(state: DemoUiState, onRefresh: () -> Unit) {
    val config = state.config
    Section(title = "Конфиг") {
        if (config == null) {
            Text("Конфиг не загружен, прикол выключен", style = PrankType.secondary)
        } else {
            InfoRow("Версия", "${config.version}")
            InfoRow("Прикол", if (config.enabled) "Включён" else "Выключен")
            InfoRow("Сезон", seasonTitle(config.skinId))
            InfoRow("Порог тряски", "${config.thresholdG} g")
            InfoRow("Пауза между тряской", "${config.cooldownMs / 1000} с")
            InfoRow("Окно рукопожатия", "${config.durationMs} мс")
            InfoRow("Порог близости", "${config.rssiMin} дБм")
            InfoRow("Замеров сигнала", "${config.rssiSamples}")
        }
        Spacer(Modifier.height(12.dp))
        PrankPrimaryButton(
            text = if (state.refresh == RefreshStatus.Loading) "Загружаю…" else "Обновить конфиг",
            enabled = state.refresh != RefreshStatus.Loading,
            onClick = onRefresh,
        )
        refreshText(state.refresh)?.let { (text, success) ->
            Spacer(Modifier.height(8.dp))
            Text(
                text,
                style = PrankType.footnote.copy(color = if (success) PrankPalette.green else PrankPalette.tertiary),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun MeCard(state: DemoUiState, onChangeElement: () -> Unit) {
    val me = state.me
    Section(title = "Мой телефон") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val element = me?.element
            if (element != null) {
                Image(painterResource(PrankArt.element(element.element)), contentDescription = null, modifier = Modifier.size(56.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(element?.title ?: "Стихия не выбрана", style = PrankType.title)
                Text(
                    "Альбом: ${me?.albumOpened ?: 0} из ${me?.albumTotal ?: 21}",
                    style = PrankType.footnote.copy(color = PrankPalette.secondary),
                )
            }
            Text(
                "Сменить",
                style = PrankType.footnote.copy(color = PrankPalette.green, fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .clip(CircleShape)
                    .background(PrankPalette.greenSoft)
                    .clickable(onClick = onChangeElement)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }
        Spacer(Modifier.height(12.dp))
        InfoRow("«Дай пять» в настройках", if (me?.highFiveEnabled != false) "Включён" else "Выключен")
        InfoRow("Bluetooth", capabilityShort(state.capability))
    }
}

@Composable
private fun Section(
    title: String,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(PrankPalette.card)
            .background(PrankPalette.surface)
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = PrankType.title, modifier = Modifier.weight(1f))
            trailing?.invoke()
        }
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = PrankType.secondary, modifier = Modifier.weight(1f))
        Text(value, style = PrankType.body)
    }
}

@Composable
private fun SecondaryButton(text: String, onClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(PrankPalette.background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = PrankType.title)
    }
}

private fun seasonTitle(skinId: String): String = when (skinId) {
    "base" -> "Обычный"
    "newyear" -> "Новогодний"
    else -> skinId
}

private fun capabilityText(capability: BleCapability): String = when (capability) {
    BleCapability.FULL -> "Bluetooth готов"
    BleCapability.NO_PERMISSION -> "Нет разрешения на Bluetooth. Без него телефоны не найдут друг друга"
    BleCapability.OFF -> "Bluetooth или геолокация выключены"
    BleCapability.UNSUPPORTED -> "Этот телефон не умеет вещать по Bluetooth LE, работает только соло"
}

private fun capabilityShort(capability: BleCapability): String = when (capability) {
    BleCapability.FULL -> "Готов"
    BleCapability.NO_PERMISSION -> "Нет разрешения"
    BleCapability.OFF -> "Выключен"
    BleCapability.UNSUPPORTED -> "Не поддерживается"
}

private fun refreshText(status: RefreshStatus): Pair<String, Boolean>? = when (status) {
    RefreshStatus.Idle, RefreshStatus.Loading -> null
    is RefreshStatus.Updated -> "Загружена новая версия ${status.version}" to true
    is RefreshStatus.Same -> "Новой версии нет, сейчас версия ${status.version}" to false
}
