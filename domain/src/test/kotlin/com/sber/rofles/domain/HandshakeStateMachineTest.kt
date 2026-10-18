package com.sber.rofles.domain

import com.sber.rofles.domain.handshake.HandshakeResult
import com.sber.rofles.domain.handshake.HandshakeStateMachine
import com.sber.rofles.domain.handshake.HsCmd
import com.sber.rofles.domain.handshake.HsEvent
import com.sber.rofles.domain.handshake.HsState
import com.sber.rofles.domain.handshake.Payload
import com.sber.rofles.domain.handshake.Phase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HandshakeStateMachineTest {

    private val params = TestConfigs.handshake
    private val me = Payload.hello(element = 1, avatar = 1, nonce = 100)
    private val friend = Payload.hello(element = 2, avatar = 4, nonce = 200)
    private val third = Payload.hello(element = 6, avatar = 2, nonce = 300)

    private var state: HsState = HsState.Idle
    private val commands = mutableListOf<HsCmd>()

    private fun send(event: HsEvent) {
        val (next, cmds) = HandshakeStateMachine.reduce(state, event, params)
        state = next
        commands += cmds
    }

    private fun seen(payload: Payload, rssi: Int, now: Long, times: Int = 3) {
        repeat(times) { send(HsEvent.Seen(payload, rssi, now)) }
    }

    private fun lastAdvertised(): Payload = commands.filterIsInstance<HsCmd.Advertise>().last().payload

    @Test
    fun `start advertises hello`() {
        send(HsEvent.Start(0, me))
        assertEquals(me, lastAdvertised())
    }

    @Test
    fun `mutual confirm matches after settle`() {
        send(HsEvent.Start(0, me))
        seen(friend, rssi = -40, now = 200)
        assertEquals(Payload.hello(1, 1, 100).confirm(200), lastAdvertised())

        send(HsEvent.Seen(friend.confirm(100), -40, 400))
        send(HsEvent.Tick(800))
        assertTrue(state is HsState.Active)

        send(HsEvent.Tick(1_000))
        val result = (state as HsState.Done).result as HandshakeResult.Matched
        assertEquals(listOf(200), result.peers.map { it.nonce })
        assertEquals(2, result.peers.single().element)
        assertEquals(HsCmd.Stop, commands.last())
    }

    @Test
    fun `friend that only says hello leads to timeout`() {
        send(HsEvent.Start(0, me))
        seen(friend, rssi = -40, now = 200)
        send(HsEvent.Tick(2_900))
        assertTrue(state is HsState.Active)
        send(HsEvent.Tick(3_000))
        assertEquals(HandshakeResult.Timeout, (state as HsState.Done).result)
    }

    @Test
    fun `weak signal is not a friend`() {
        send(HsEvent.Start(0, me))
        seen(friend.confirm(100), rssi = -80, now = 200)
        assertEquals(me, lastAdvertised())
        send(HsEvent.Tick(3_000))
        assertEquals(HandshakeResult.Timeout, (state as HsState.Done).result)
    }

    @Test
    fun `needs enough rssi samples`() {
        send(HsEvent.Start(0, me))
        seen(friend.confirm(100), rssi = -40, now = 200, times = 2)
        assertEquals(me, lastAdvertised())
        send(HsEvent.Seen(friend.confirm(100), -40, 300))
        assertEquals(me.confirm(200), lastAdvertised())
    }

    @Test
    fun `median smooths a single spike`() {
        send(HsEvent.Start(0, me))
        send(HsEvent.Seen(friend, -90, 100))
        send(HsEvent.Seen(friend, -40, 150))
        send(HsEvent.Seen(friend, -45, 200))
        assertEquals(me.confirm(200), lastAdvertised())
    }

    @Test
    fun `other protocol version is ignored`() {
        send(HsEvent.Start(0, me))
        seen(friend.copy(version = 2).confirm(100), rssi = -40, now = 200)
        send(HsEvent.Tick(3_000))
        assertEquals(HandshakeResult.Timeout, (state as HsState.Done).result)
    }

    @Test
    fun `own nonce is ignored`() {
        send(HsEvent.Start(0, me))
        seen(me.copy(phase = Phase.CONFIRM, peerNonce = 100), rssi = -30, now = 200)
        assertEquals(me, lastAdvertised())
    }

    @Test
    fun `settle keeps advertising past the window`() {
        send(HsEvent.Start(0, me))
        seen(friend.confirm(100), rssi = -40, now = 2_900)
        send(HsEvent.Tick(3_000))
        assertTrue(state is HsState.Active)
        send(HsEvent.Tick(3_500))
        assertTrue(state is HsState.Done)
    }

    @Test
    fun `three phones match two peers and rotate echo`() {
        send(HsEvent.Start(0, me))
        seen(friend, rssi = -40, now = 100)
        seen(third, rssi = -42, now = 100)
        assertEquals(me.confirm(200), lastAdvertised())

        send(HsEvent.Tick(400))
        assertEquals(me.confirm(300), lastAdvertised())

        send(HsEvent.Seen(friend.confirm(100), -40, 500))
        send(HsEvent.Seen(third.confirm(100), -40, 600))
        send(HsEvent.Tick(1_200))
        val result = (state as HsState.Done).result as HandshakeResult.Matched
        assertEquals(listOf(200, 300), result.peers.map { it.nonce })
    }

    @Test
    fun `events after done are ignored`() {
        send(HsEvent.Start(0, me))
        send(HsEvent.Tick(3_000))
        val done = state
        send(HsEvent.Seen(friend.confirm(100), -40, 3_100))
        assertEquals(done, state)
    }
}
