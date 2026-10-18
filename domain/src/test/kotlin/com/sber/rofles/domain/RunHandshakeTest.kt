package com.sber.rofles.domain

import com.sber.rofles.domain.handshake.HandshakeCodec
import com.sber.rofles.domain.handshake.HandshakeResult
import com.sber.rofles.domain.handshake.Payload
import com.sber.rofles.domain.model.BleCapability
import com.sber.rofles.domain.repository.Clock
import com.sber.rofles.domain.repository.HandshakeTransport
import com.sber.rofles.domain.repository.RawAdvert
import com.sber.rofles.domain.usecase.RunHandshake
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class RunHandshakeTest {

    private val params = TestConfigs.handshake
    private val me = Payload.hello(element = 1, avatar = 1, nonce = 100)
    private val friend = Payload.hello(element = 2, avatar = 4, nonce = 200)

    private class FakeTransport : HandshakeTransport {
        val incoming = Channel<RawAdvert>(Channel.UNLIMITED)
        val advertised = mutableListOf<UUID>()

        override fun capability(): BleCapability = BleCapability.FULL

        override fun scan(): Flow<RawAdvert> = incoming.receiveAsFlow()

        override fun advertise(uuid: UUID) {
            advertised += uuid
        }

        override fun stop() = Unit
    }

    private class TestClock(private val scope: TestScope) : Clock {
        override fun now(): Long = scope.currentTime

        override fun today(): LocalDate = LocalDate.of(2026, 10, 18)
    }

    private fun TestScope.runHandshake(transport: FakeTransport): RunHandshake =
        RunHandshake(transport, TestClock(this))

    private suspend fun seen(vararg adverts: RawAdvert, times: Int, transport: FakeTransport) {
        repeat(times) { adverts.forEach { transport.incoming.send(it) } }
    }

    @Test
    fun `mutual confirm produces matched peer`() = runTest {
        val transport = FakeTransport()
        val handshake = runHandshake(transport)
        var result: HandshakeResult? = null
        val job = launch { result = handshake.invoke(me, params) }

        seen(RawAdvert(HandshakeCodec.encode(friend), -40), times = 3, transport = transport)
        seen(RawAdvert(HandshakeCodec.encode(friend.confirm(100)), -40), times = 3, transport = transport)
        advanceUntilIdle()
        job.join()

        val matched = result as HandshakeResult.Matched
        assertEquals(1, matched.peers.size)
        assertEquals(200, matched.peers.single().nonce)
        assertEquals(2, matched.peers.single().element)
        assertEquals(HandshakeCodec.encode(me.confirm(200)), transport.advertised.last())
    }

    @Test
    fun `friend that never confirms leads to timeout`() = runTest {
        val transport = FakeTransport()
        val handshake = runHandshake(transport)
        var result: HandshakeResult? = null
        val job = launch { result = handshake.invoke(me, params) }

        seen(RawAdvert(HandshakeCodec.encode(friend), -40), times = 3, transport = transport)
        advanceUntilIdle()
        job.join()

        assertEquals(HandshakeResult.Timeout, result)
    }

    @Test
    fun `no adverts at all leads to timeout`() = runTest {
        val transport = FakeTransport()
        val handshake = runHandshake(transport)
        var result: HandshakeResult? = null
        val job = launch { result = handshake.invoke(me, params) }
        advanceUntilIdle()
        job.join()
        assertEquals(HandshakeResult.Timeout, result)
        assertTrue(transport.advertised.first() == HandshakeCodec.encode(me))
    }

    @Test
    fun `weak signal never matches`() = runTest {
        val transport = FakeTransport()
        val handshake = runHandshake(transport)
        var result: HandshakeResult? = null
        val job = launch { result = handshake.invoke(me, params) }

        seen(RawAdvert(HandshakeCodec.encode(friend.confirm(100)), -90), times = 3, transport = transport)
        advanceUntilIdle()
        job.join()

        assertEquals(HandshakeResult.Timeout, result)
    }

    @Test
    fun `stop is always called in the end`() = runTest {
        val transport = FakeTransport()
        val handshake = runHandshake(transport)
        val job = launch { handshake.invoke(me, params) }
        advanceUntilIdle()
        job.join()
        assertTrue(transport.advertised.isNotEmpty())
    }
}