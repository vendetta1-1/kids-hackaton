package com.sber.rofles.prank

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.sber.rofles.core.effect.PrikolScreen
import com.sber.rofles.core.effect.ScreenGuard
import com.sber.rofles.core.prank.BluetoothPermissions
import com.sber.rofles.core.prank.BluetoothTexts
import com.sber.rofles.core.prank.PrankController
import com.sber.rofles.core.prank.PrankViewModel
import com.sber.rofles.core.prank.SheetUi
import com.sber.rofles.core.prank.ToastKind
import com.sber.rofles.domain.model.BleCapability
import com.sber.rofles.domain.model.ComboKey
import com.sber.rofles.domain.model.PrankEvent
import com.sber.rofles.feature.album.AlbumScreen
import com.sber.rofles.feature.demo.DemoScreen
import com.sber.rofles.feature.elementpicker.ElementPickerSheet
import com.sber.rofles.feature.highfive.BluetoothSheet
import com.sber.rofles.feature.highfive.HighFiveEffect
import com.sber.rofles.feature.highfive.PrankToast
import com.sber.rofles.feature.solo.SoloEffect
import kotlinx.coroutines.delay

private const val EFFECT_MS = 3_000

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PrankOverlayHost(
    currentScreen: PrikolScreen,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val controller = hiltViewModel<PrankViewModel>().controller
    val rewardsViewModel: PrankRewardsViewModel = hiltViewModel()
    val rewards by rewardsViewModel.state.collectAsState()
    val prankScreen by Prank.openScreen.collectAsState()
    val pickerRequest by Prank.elementPickerRequests.collectAsState()
    LaunchedEffect(pickerRequest) {
        if (pickerRequest > 0) controller.openElementPicker()
    }
    val effect by controller.effect.collectAsState()
    val toast by controller.toast.collectAsState()
    val sheet by controller.sheet.collectAsState()
    val picker by controller.picker.collectAsState()
    val bluetoothTexts by controller.bluetoothTexts.collectAsState()
    val openCard: (ComboKey?) -> Unit = { key ->
        effect?.let { controller.dismissEffect(it.id) }
        toast?.let { controller.dismissToast(it.id) }
        Prank.openAlbum(key?.id)
    }

    val allowed = prankScreen != PrankScreen.Service && ScreenGuard.allows(currentScreen) && !WindowInsets.isImeVisible
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(allowed, lifecycleOwner) {
        if (!allowed) return@LaunchedEffect
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            controller.shakes().collect { at ->
                rewardsViewModel.onShake()
                controller.onShake(at)
            }
        }
    }
    LaunchedEffect(Unit) {
        delay(1_500)
        controller.refreshConfigOnce()
    }

    Box(modifier.fillMaxSize()) {
        content()
        PrankScreens(prankScreen)
        effect?.let { current ->
            key(current.id) {
                LaunchedEffect(current.id) {
                    delay(EFFECT_MS.toLong())
                    controller.dismissEffect(current.id)
                }
                when (val event = current.event) {
                    is PrankEvent.Solo -> SoloEffect(event.style, current.id, durationMs = EFFECT_MS)
                    is PrankEvent.HighFive -> HighFiveEffect(
                        mine = event.mine,
                        friend = event.friend,
                        title = event.combo.title,
                        combo = event.combo.key,
                        isNew = event.isNew,
                        effectId = current.id,
                        count = event.count,
                        durationMs = EFFECT_MS,
                        onCardClick = { openCard(event.combo.key) },
                    )
                    is PrankEvent.Friendship -> HighFiveEffect(
                        event.mine,
                        null,
                        event.title,
                        null,
                        false,
                        current.id,
                        durationMs = EFFECT_MS,
                    )
                    else -> Unit
                }
            }
        }
        toast?.takeIf { effect == null }?.let { current ->
            key(current.id) {
                PrankToast(
                    toast = current,
                    onDismiss = { controller.dismissToast(current.id) },
                    onClick = {
                        val capability = current.capability
                        if (current.kind == ToastKind.BLUETOOTH && capability != null) {
                            controller.openBluetoothSheet(capability)
                        } else {
                            openCard(current.combo)
                        }
                    },
                    modifier = Modifier.align(Alignment.TopCenter),
                )
            }
        }
    }

    when (val current = sheet) {
        is SheetUi.ElementPicker -> picker?.let { state ->
            ElementPickerSheet(
                state = state,
                onSelect = controller::selectElement,
                onDismiss = controller::dismissSheet,
            )
        }
        is SheetUi.Bluetooth -> BluetoothOnboarding(controller, current.capability, bluetoothTexts)
        null -> Unit
    }
    val newFrame = rewards.newFrame
    if (newFrame != null && effect == null && sheet == null) {
        NewFrameSheet(newFrame, isMine = rewards.frame?.style?.element == newFrame.element) {
            rewardsViewModel.frameCelebrated(newFrame.element)
        }
    }
}

@Composable
private fun BluetoothOnboarding(
    controller: PrankController,
    capability: BleCapability,
    texts: BluetoothTexts,
) {
    val context = LocalContext.current
    val adapter = context.getSystemService(BluetoothManager::class.java)?.adapter
    val enableBluetooth = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        controller.onBluetoothFlowFinished()
    }
    val requestPermissions = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        if (result.values.all { it } && adapter?.isEnabled == false) {
            runCatching { enableBluetooth.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)) }
                .onFailure { controller.onBluetoothFlowFinished() }
        } else {
            controller.onBluetoothFlowFinished()
        }
    }
    BluetoothSheet(
        texts = texts,
        onEnable = {
            when {
                capability == BleCapability.NO_PERMISSION -> requestPermissions.launch(BluetoothPermissions.required)
                adapter?.isEnabled == false -> runCatching { enableBluetooth.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)) }
                    .onFailure { controller.onBluetoothFlowFinished() }
                else -> {
                    runCatching { context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
                    controller.onBluetoothFlowFinished()
                }
            }
        },
        onLater = controller::dismissSheet,
    )
}

@Composable
private fun PrankScreens(screen: PrankScreen?) {
    BackHandler(enabled = screen != null) { Prank.close() }
    AnimatedContent(
        targetState = screen,
        contentKey = { it?.let { value -> value::class } },
        transitionSpec = {
            (slideInHorizontally { it } + fadeIn()) togetherWith (slideOutHorizontally { it } + fadeOut())
        },
        label = "prank-screens",
    ) { current ->
        when (current) {
            is PrankScreen.Album -> AlbumScreen(
                onBack = Prank::close,
                focusCardId = current.focusCardId,
                onFocusConsumed = Prank::consumeAlbumFocus,
            )
            PrankScreen.Service -> DemoScreen(onBack = Prank::close)
            null -> Box(Modifier)
        }
    }
}
