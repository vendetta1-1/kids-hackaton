package com.sber.rofles.domain.handshake

import java.util.UUID

enum class Phase(val code: Int) {
    HELLO(0),
    CONFIRM(1);

    companion object {
        fun fromCode(code: Int): Phase? = entries.firstOrNull { it.code == code }
    }
}

data class Payload(
    val version: Int,
    val phase: Phase,
    val element: Int,
    val avatar: Int,
    val nonce: Int,
    val peerNonce: Int,
) {
    fun confirm(peer: Int): Payload = copy(phase = Phase.CONFIRM, peerNonce = peer)

    companion object {
        const val VERSION = 1

        fun hello(element: Int, avatar: Int, nonce: Int): Payload =
            Payload(VERSION, Phase.HELLO, element, avatar, (nonce and 0xFFFF).takeIf { it != 0 } ?: 1, 0)
    }
}

object HandshakeCodec {
    const val PREFIX: Long = 0x6869_6635_6b69_6473L

    val prefixUuid: UUID = UUID(PREFIX, 0L)

    val prefixMask: UUID = UUID(-1L, 0L)

    fun encode(payload: Payload): UUID {
        val low = ((payload.version.toLong() and 0xFF) shl 56) or
            ((payload.phase.code.toLong() and 0xFF) shl 48) or
            ((payload.element.toLong() and 0xFF) shl 40) or
            ((payload.avatar.toLong() and 0xFF) shl 32) or
            ((payload.nonce.toLong() and 0xFFFF) shl 16) or
            (payload.peerNonce.toLong() and 0xFFFF)
        return UUID(PREFIX, low)
    }

    fun decode(uuid: UUID): Payload? {
        if (uuid.mostSignificantBits != PREFIX) return null
        val low = uuid.leastSignificantBits
        val version = ((low ushr 56) and 0xFF).toInt()
        if (version != Payload.VERSION) return null
        val phase = Phase.fromCode(((low ushr 48) and 0xFF).toInt()) ?: return null
        val nonce = ((low ushr 16) and 0xFFFF).toInt()
        if (nonce == 0) return null
        return Payload(
            version = version,
            phase = phase,
            element = ((low ushr 40) and 0xFF).toInt(),
            avatar = ((low ushr 32) and 0xFF).toInt(),
            nonce = nonce,
            peerNonce = (low and 0xFFFF).toInt(),
        )
    }
}
