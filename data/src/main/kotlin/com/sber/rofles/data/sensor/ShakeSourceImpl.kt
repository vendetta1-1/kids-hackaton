package com.sber.rofles.data.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.sber.rofles.domain.model.ShakeParams
import com.sber.rofles.domain.repository.ShakeSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

@Singleton
class ShakeSourceImpl @Inject constructor(
    @ApplicationContext context: Context,
) : ShakeSource {

    private val sensorManager = context.getSystemService(SensorManager::class.java)

    override fun shakes(params: ShakeParams): Flow<Long> {
        val manager = sensorManager ?: return emptyFlow()
        val accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) ?: return emptyFlow()
        return callbackFlow {
            val peaks = ArrayDeque<Long>()
            var above = false
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val (x, y, z) = event.values
                    val g = sqrt(x * x + y * y + z * z) / SensorManager.GRAVITY_EARTH
                    val now = System.currentTimeMillis()
                    if (g > params.thresholdG && !above) {
                        above = true
                        peaks.addLast(now)
                        while (peaks.isNotEmpty() && now - peaks.first() > params.windowMs) peaks.removeFirst()
                        if (peaks.size >= params.peaks) {
                            peaks.clear()
                            trySend(now)
                        }
                    } else if (g < params.thresholdG * HYSTERESIS) {
                        above = false
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }
            manager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
            awaitClose { manager.unregisterListener(listener) }
        }
    }

    private companion object {
        const val HYSTERESIS = 0.8f
    }
}
