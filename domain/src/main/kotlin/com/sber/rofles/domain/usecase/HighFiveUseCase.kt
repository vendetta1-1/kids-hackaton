package com.sber.rofles.domain.usecase

import com.sber.rofles.domain.handshake.HandshakeCodec
import com.sber.rofles.domain.handshake.HandshakeResult
import com.sber.rofles.domain.handshake.HandshakeStateMachine
import com.sber.rofles.domain.handshake.HsCmd
import com.sber.rofles.domain.handshake.HsEvent
import com.sber.rofles.domain.handshake.HsState
import com.sber.rofles.domain.handshake.Payload
import com.sber.rofles.domain.model.BleCapability
import com.sber.rofles.domain.model.HandshakeParams
import com.sber.rofles.domain.model.PrankEvent
import com.sber.rofles.domain.repository.AlbumRepository
import com.sber.rofles.domain.repository.AvatarProvider
import com.sber.rofles.domain.repository.Clock
import com.sber.rofles.domain.repository.HandshakeTransport
import com.sber.rofles.domain.repository.MyElementRepository
import com.sber.rofles.domain.repository.PrankConfigRepository
import com.sber.rofles.domain.repository.PrankSettingsRepository
import com.sber.rofles.domain.repository.Random16
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class RunHandshake(
    private val transport: HandshakeTransport,
    private val clock: Clock,
    private val tickMs: Long = 100,
) {
    suspend operator fun invoke(my: Payload, params: HandshakeParams): HandshakeResult = coroutineScope {
        var state: HsState = HsState.Idle
        val events = Channel<HsEvent>(Channel.UNLIMITED)

        fun apply(event: HsEvent) {
            val (next, commands) = HandshakeStateMachine.reduce(state, event, params)
            state = next
            commands.forEach { command ->
                when (command) {
                    is HsCmd.Advertise -> transport.advertise(HandshakeCodec.encode(command.payload))
                    HsCmd.Stop -> transport.stop()
                }
            }
        }

        try {
            apply(HsEvent.Start(clock.now(), my))
            val scan = launch {
                transport.scan().collect { advert ->
                    HandshakeCodec.decode(advert.uuid)?.let { events.send(HsEvent.Seen(it, advert.rssi, clock.now())) }
                }
            }
            val ticks = launch {
                while (isActive) {
                    delay(tickMs)
                    events.send(HsEvent.Tick(clock.now()))
                }
            }
            for (event in events) {
                apply(event)
                if (state is HsState.Done) break
            }
            scan.cancel()
            ticks.cancel()
            (state as HsState.Done).result
        } finally {
            transport.stop()
        }
    }
}

class HighFiveUseCase(
    private val configRepository: PrankConfigRepository,
    private val settingsRepository: PrankSettingsRepository,
    private val myElementRepository: MyElementRepository,
    private val albumRepository: AlbumRepository,
    private val transport: HandshakeTransport,
    private val runHandshake: RunHandshake,
    private val avatar: AvatarProvider,
    private val clock: Clock,
    private val random: Random16,
    private val cooldown: Cooldown,
) {
    operator fun invoke(shakeAt: Long): Flow<PrankEvent> = flow {
        val config = configRepository.config.value ?: return@flow
        if (!config.enabled) return@flow
        if (!settingsRepository.settings.first().highFiveEnabled) return@flow
        if (!cooldown.tryPass(shakeAt, config.shake.cooldownMs)) return@flow

        val me = myElementRepository.observe().first()
        if (me == null) {
            emit(PrankEvent.NeedElement)
            return@flow
        }

        val skin = ResolveSkin(config, clock.today())
        val mine = skin.element(me.element)
        emit(PrankEvent.Solo(mine))

        val capability = transport.capability()
        if (capability != BleCapability.FULL) {
            emit(PrankEvent.BluetoothUnavailable(capability))
            return@flow
        }

        val hello = Payload.hello(me.element.id, avatar.index(), random.next())
        val result = runHandshake(hello, config.handshake)
        if (result !is HandshakeResult.Matched || result.peers.isEmpty()) return@flow

        when (val resolved = ResolveCombo(me.element, result.peers, skin, config.fallbackTitle)) {
            is Resolved.Known -> {
                val entry = albumRepository.record(resolved.combo.key, clock.now())
                emit(PrankEvent.HighFive(resolved.combo, mine, resolved.friend, entry.count == 1, entry.count))
            }
            is Resolved.Unknown -> emit(PrankEvent.Friendship(resolved.title, mine))
        }
    }
}
