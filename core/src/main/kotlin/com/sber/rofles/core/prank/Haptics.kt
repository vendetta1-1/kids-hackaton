package com.sber.rofles.core.prank

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Haptics @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            context.getSystemService(Vibrator::class.java)
        }
    }

    fun highFive() = play(longArrayOf(0, 60, 80, 120, 60, 220), intArrayOf(0, 180, 0, 255, 0, 140))

    fun tick() = play(longArrayOf(0, 30), intArrayOf(0, 120))

    private fun play(timings: LongArray, amplitudes: IntArray) {
        val vibrator = vibrator?.takeIf { it.hasVibrator() } ?: return
        runCatching {
            val effect = if (vibrator.hasAmplitudeControl()) {
                VibrationEffect.createWaveform(timings, amplitudes, -1)
            } else {
                VibrationEffect.createWaveform(timings, -1)
            }
            vibrator.vibrate(effect)
        }
    }
}
