package com.sber.rofles.feature.demo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sber.rofles.core.prank.PrankController
import com.sber.rofles.domain.handshake.HandshakeCodec
import com.sber.rofles.domain.handshake.Payload
import com.sber.rofles.domain.handshake.Phase
import com.sber.rofles.domain.model.BleCapability
import com.sber.rofles.domain.model.Element
import com.sber.rofles.domain.model.ElementStyle
import com.sber.rofles.domain.model.PrankConfig
import com.sber.rofles.domain.repository.AlbumRepository
import com.sber.rofles.domain.repository.AvatarProvider
import com.sber.rofles.domain.repository.Clock
import com.sber.rofles.domain.repository.HandshakeTransport
import com.sber.rofles.domain.repository.MyElementRepository
import com.sber.rofles.domain.repository.PrankConfigRepository
import com.sber.rofles.domain.repository.PrankSettingsRepository
import com.sber.rofles.domain.repository.Random16
import com.sber.rofles.domain.usecase.ResolveSkin
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConfigInfo(
    val version: Int,
    val enabled: Boolean,
    val skinId: String,
    val thresholdG: Float,
    val cooldownMs: Long,
    val durationMs: Long,
    val settleMs: Long,
    val rssiMin: Int,
    val rssiSamples: Int,
)

data class MyInfo(
    val element: ElementStyle?,
    val albumOpened: Int,
    val albumTotal: Int,
    val highFiveEnabled: Boolean,
)

data class Neighbor(
    val nonce: Int,
    val element: Element?,
    val elementTitle: String,
    val avatar: Int,
    val phase: Phase,
    val lastRssi: Int,
    val medianRssi: Int,
    val close: Boolean,
    val seenAgoMs: Long,
)

sealed interface RefreshStatus {
    data object Idle : RefreshStatus
    data object Loading : RefreshStatus
    data class Updated(val version: Int) : RefreshStatus
    data class Same(val version: Int) : RefreshStatus
}

data class DemoUiState(
    val config: ConfigInfo? = null,
    val me: MyInfo? = null,
    val capability: BleCapability = BleCapability.UNSUPPORTED,
    val radarOn: Boolean = false,
    val neighbors: List<Neighbor> = emptyList(),
    val refresh: RefreshStatus = RefreshStatus.Idle,
)

@HiltViewModel
class DemoViewModel @Inject constructor(
    private val configRepository: PrankConfigRepository,
    private val transport: HandshakeTransport,
    private val clock: Clock,
    private val random: Random16,
    private val avatar: AvatarProvider,
    myElementRepository: MyElementRepository,
    albumRepository: AlbumRepository,
    settingsRepository: PrankSettingsRepository,
    val controller: PrankController,
) : ViewModel() {

    private val myElement = myElementRepository.observe().stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val radar = MutableStateFlow(RadarState())
    private val refresh = MutableStateFlow<RefreshStatus>(RefreshStatus.Idle)
    private val capability = MutableStateFlow(transport.capability())
    private var radarJob: Job? = null

    val state: StateFlow<DemoUiState> = combine(
        configRepository.config,
        combine(myElement, albumRepository.observe(), settingsRepository.settings, ::Triple),
        radar,
        refresh,
        capability,
    ) { config, (mine, album, settings), radarState, refreshStatus, cap ->
        val skin = config?.let { runCatching { ResolveSkin(it, clock.today()) }.getOrNull() }
        DemoUiState(
            config = config?.let { info(it, skin?.id ?: it.defaultSkin) },
            me = MyInfo(
                element = mine?.element?.let { skin?.elements?.get(it) },
                albumOpened = album.size,
                albumTotal = 21,
                highFiveEnabled = settings.highFiveEnabled,
            ),
            capability = cap,
            radarOn = radarState.on,
            neighbors = radarState.neighbors(config, skin?.elements, clock.now()),
            refresh = refreshStatus,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DemoUiState())

    fun refreshConfig() {
        if (refresh.value == RefreshStatus.Loading) return
        viewModelScope.launch {
            val before = configRepository.config.value?.version ?: 0
            refresh.value = RefreshStatus.Loading
            configRepository.refresh()
            val after = configRepository.config.value?.version ?: 0
            refresh.value = if (after > before) RefreshStatus.Updated(after) else RefreshStatus.Same(after)
        }
    }

    fun checkCapability() {
        capability.value = transport.capability()
    }

    fun startRadar() {
        checkCapability()
        if (radarJob?.isActive == true || capability.value != BleCapability.FULL) return
        radarJob = viewModelScope.launch {
            val element = myElement.value?.element ?: myElementFromStore()
            val hello = Payload.hello(element?.id ?: 0, avatar.index(), random.next())
            transport.advertise(HandshakeCodec.encode(hello))
            radar.update { it.copy(on = true, samples = emptyMap()) }
            launch {
                while (isActive) {
                    delay(500)
                    radar.update { it.copy(tick = it.tick + 1) }
                }
            }
            transport.scan().collect { advert ->
                val payload = HandshakeCodec.decode(advert.uuid) ?: return@collect
                if (payload.nonce == hello.nonce) return@collect
                val now = clock.now()
                radar.update { it.record(payload, advert.rssi, now) }
            }
        }
        radarJob?.invokeOnCompletion {
            transport.stop()
            radar.update { it.copy(on = false) }
        }
    }

    fun stopRadar() {
        radarJob?.cancel()
        radarJob = null
    }

    override fun onCleared() {
        stopRadar()
    }

    private suspend fun myElementFromStore(): Element? = myElement.first()?.element

    private fun info(config: PrankConfig, skinId: String) = ConfigInfo(
        version = config.version,
        enabled = config.enabled,
        skinId = skinId,
        thresholdG = config.shake.thresholdG,
        cooldownMs = config.shake.cooldownMs,
        durationMs = config.handshake.durationMs,
        settleMs = config.handshake.settleMs,
        rssiMin = config.handshake.rssiMin,
        rssiSamples = config.handshake.rssiSamples,
    )
}

private data class Sample(val payload: Payload, val rssi: List<Int>, val seenAt: Long)

private data class RadarState(
    val on: Boolean = false,
    val samples: Map<Int, Sample> = emptyMap(),
    val tick: Long = 0,
) {
    fun record(payload: Payload, rssi: Int, now: Long): RadarState {
        val previous = samples[payload.nonce]?.rssi ?: emptyList()
        val sample = Sample(payload, (previous + rssi).takeLast(WINDOW), now)
        return copy(samples = samples + (payload.nonce to sample))
    }

    fun neighbors(config: PrankConfig?, styles: Map<Element, ElementStyle>?, now: Long): List<Neighbor> {
        val rssiMin = config?.handshake?.rssiMin ?: DEFAULT_RSSI_MIN
        return samples.values
            .filter { now - it.seenAt < STALE_MS }
            .sortedByDescending { it.rssi.last() }
            .map { sample ->
                val element = Element.fromId(sample.payload.element)
                val median = sample.rssi.sorted()[sample.rssi.size / 2]
                Neighbor(
                    nonce = sample.payload.nonce,
                    element = element,
                    elementTitle = element?.let { styles?.get(it)?.title } ?: "Стихия не выбрана",
                    avatar = sample.payload.avatar,
                    phase = sample.payload.phase,
                    lastRssi = sample.rssi.last(),
                    medianRssi = median,
                    close = median >= rssiMin,
                    seenAgoMs = now - sample.seenAt,
                )
            }
    }

    private companion object {
        const val WINDOW = 5
        const val STALE_MS = 5_000L
        const val DEFAULT_RSSI_MIN = -55
    }
}
