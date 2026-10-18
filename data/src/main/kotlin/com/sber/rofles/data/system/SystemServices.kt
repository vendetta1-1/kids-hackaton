package com.sber.rofles.data.system

import com.sber.rofles.domain.repository.Clock
import com.sber.rofles.domain.repository.Random16
import java.security.SecureRandom
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemClock @Inject constructor() : Clock {
    override fun now(): Long = System.currentTimeMillis()

    override fun today(): LocalDate = LocalDate.now()
}

@Singleton
class SecureRandom16 @Inject constructor() : Random16 {
    private val random = SecureRandom()

    override fun next(): Int = random.nextInt(0xFFFF) + 1
}
