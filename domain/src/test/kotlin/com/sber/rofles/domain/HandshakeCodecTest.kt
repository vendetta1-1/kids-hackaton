package com.sber.rofles.domain

import com.sber.rofles.domain.handshake.HandshakeCodec
import com.sber.rofles.domain.handshake.Payload
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.UUID

class HandshakeCodecTest {

    @Test
    fun `hello round trip`() {
        val payload = Payload.hello(element = 3, avatar = 7, nonce = 0xBEEF)
        assertEquals(payload, HandshakeCodec.decode(HandshakeCodec.encode(payload)))
    }

    @Test
    fun `confirm round trip keeps peer nonce`() {
        val payload = Payload.hello(element = 6, avatar = 255, nonce = 1).confirm(0xFFFF)
        assertEquals(payload, HandshakeCodec.decode(HandshakeCodec.encode(payload)))
    }

    @Test
    fun `foreign prefix is ignored`() {
        val uuid = UUID(0x1234_5678_9abc_def0L, HandshakeCodec.encode(Payload.hello(1, 1, 5)).leastSignificantBits)
        assertNull(HandshakeCodec.decode(uuid))
    }

    @Test
    fun `garbage is ignored`() {
        assertNull(HandshakeCodec.decode(UUID(HandshakeCodec.PREFIX, -1L)))
        assertNull(HandshakeCodec.decode(UUID(HandshakeCodec.PREFIX, 0L)))
        assertNull(HandshakeCodec.decode(UUID.randomUUID()))
    }

    @Test
    fun `other protocol version is ignored`() {
        val encoded = HandshakeCodec.encode(Payload.hello(1, 1, 5).copy(version = 2))
        assertNull(HandshakeCodec.decode(encoded))
    }

    @Test
    fun `zero nonce is never produced`() {
        assertEquals(1, Payload.hello(1, 1, 0x10000).nonce)
    }
}
