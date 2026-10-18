package com.sber.rofles.domain.handshake

import com.sber.rofles.domain.model.HandshakeParams

data class Peer(val nonce: Int, val element: Int, val avatar: Int)

sealed interface HandshakeResult {
    data class Matched(val peers: List<Peer>) : HandshakeResult

    data object Timeout : HandshakeResult
}

data class Candidate(val element: Int, val avatar: Int, val rssi: List<Int>)

sealed interface HsState {
    data object Idle : HsState

    data class Active(
        val my: Payload,
        val startedAt: Long,
        val candidates: Map<Int, Candidate>,
        val confirmedBy: Set<Int>,
        val echoIndex: Int,
        val lastEchoAt: Long,
        val matchedAt: Long?,
        val advertised: Payload,
    ) : HsState

    data class Done(val result: HandshakeResult) : HsState
}

sealed interface HsEvent {
    data class Start(val now: Long, val my: Payload) : HsEvent

    data class Seen(val payload: Payload, val rssi: Int, val now: Long) : HsEvent

    data class Tick(val now: Long) : HsEvent
}

sealed interface HsCmd {
    data class Advertise(val payload: Payload) : HsCmd

    data object Stop : HsCmd
}

object HandshakeStateMachine {

    fun reduce(state: HsState, event: HsEvent, params: HandshakeParams): Pair<HsState, List<HsCmd>> =
        when {
            event is HsEvent.Start && state !is HsState.Active -> start(event)
            state is HsState.Active && event is HsEvent.Seen -> seen(state, event, params)
            state is HsState.Active && event is HsEvent.Tick -> tick(state, event, params)
            else -> state to emptyList()
        }

    private fun start(event: HsEvent.Start): Pair<HsState, List<HsCmd>> {
        val active = HsState.Active(
            my = event.my,
            startedAt = event.now,
            candidates = emptyMap(),
            confirmedBy = emptySet(),
            echoIndex = 0,
            lastEchoAt = event.now,
            matchedAt = null,
            advertised = event.my,
        )
        return active to listOf(HsCmd.Advertise(event.my))
    }

    private fun seen(state: HsState.Active, event: HsEvent.Seen, params: HandshakeParams): Pair<HsState, List<HsCmd>> {
        val p = event.payload
        if (p.version != Payload.VERSION || p.nonce == state.my.nonce) return state to emptyList()

        val previous = state.candidates[p.nonce]
        val samples = ((previous?.rssi ?: emptyList()) + event.rssi).takeLast(params.rssiSamples.coerceAtLeast(1))
        val candidates = state.candidates + (p.nonce to Candidate(p.element, p.avatar, samples))
        val confirmedBy = if (p.phase == Phase.CONFIRM && p.peerNonce == state.my.nonce) {
            state.confirmedBy + p.nonce
        } else {
            state.confirmedBy
        }

        val next = state.copy(candidates = candidates, confirmedBy = confirmedBy)
        val matchedAt = next.matchedAt ?: event.now.takeIf { matched(next, params).isNotEmpty() }
        return advertise(next.copy(matchedAt = matchedAt), params)
    }

    private fun tick(state: HsState.Active, event: HsEvent.Tick, params: HandshakeParams): Pair<HsState, List<HsCmd>> {
        val matchedAt = state.matchedAt
        if (matchedAt != null && event.now - matchedAt >= params.settleMs) {
            return HsState.Done(HandshakeResult.Matched(matched(state, params))) to listOf(HsCmd.Stop)
        }
        if (matchedAt == null && event.now - state.startedAt >= params.durationMs) {
            return HsState.Done(HandshakeResult.Timeout) to listOf(HsCmd.Stop)
        }
        val close = closeNonces(state, params)
        if (close.size > 1 && event.now - state.lastEchoAt >= params.echoRotateMs) {
            return advertise(state.copy(echoIndex = state.echoIndex + 1, lastEchoAt = event.now), params)
        }
        return state to emptyList()
    }

    private fun advertise(state: HsState.Active, params: HandshakeParams): Pair<HsState, List<HsCmd>> {
        val close = closeNonces(state, params)
        val desired = if (close.isEmpty()) state.my else state.my.confirm(close[state.echoIndex % close.size])
        if (desired == state.advertised) return state to emptyList()
        return state.copy(advertised = desired) to listOf(HsCmd.Advertise(desired))
    }

    private fun closeNonces(state: HsState.Active, params: HandshakeParams): List<Int> =
        state.candidates
            .filterValues { isClose(it, params) }
            .keys
            .sorted()

    private fun matched(state: HsState.Active, params: HandshakeParams): List<Peer> =
        closeNonces(state, params)
            .filter { it in state.confirmedBy }
            .map { nonce ->
                val c = state.candidates.getValue(nonce)
                Peer(nonce, c.element, c.avatar)
            }

    private fun isClose(candidate: Candidate, params: HandshakeParams): Boolean {
        if (candidate.rssi.size < params.rssiSamples) return false
        val sorted = candidate.rssi.sorted()
        val median = if (sorted.size % 2 == 1) {
            sorted[sorted.size / 2].toDouble()
        } else {
            (sorted[sorted.size / 2 - 1] + sorted[sorted.size / 2]) / 2.0
        }
        return median >= params.rssiMin
    }
}
