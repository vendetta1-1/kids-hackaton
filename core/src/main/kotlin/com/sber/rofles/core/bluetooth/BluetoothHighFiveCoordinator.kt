package com.sber.rofles.core.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import com.sber.rofles.domain.model.Element
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Bluetooth-рукопожатие через BLE: в окне ~3 с телефон рекламирует свой
 * пейлоад (8 байт) и сканирует соседа с тем же сервисом.
 *
 * Требуются права BLUETOOTH_SCAN / BLUETOOTH_ADVERTISE / BLUETOOTH_CONNECT
 * (Android 12+) и разрешение на BLE-локацию (Android < 31). Рантайм-запрос
 * прав — в шторке «Включи Bluetooth».
 */
@Singleton
class BluetoothHighFiveCoordinator @Inject constructor(
    @ApplicationContext private val context: Context,
) : HighFiveCoordinator {

    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

    override fun start() = Unit
    override fun stop() = Unit

    @SuppressLint("MissingPermission")
    override suspend fun shakeHands(myElement: Element?, myAvatar: Int): PeerHandshake? {
        val adapter = bluetoothAdapter ?: return null
        val advertiser = adapter.bluetoothLeAdvertiser ?: return null
        val scanner = adapter.bluetoothLeScanner ?: return null

        val myPayload = buildPayload(myElement, myAvatar)

        return withTimeoutOrNull(HANDSHAKE_TIMEOUT_MS) {
            suspendCancellableCoroutine { cont ->
                val advertiseCallback = object : AdvertiseCallback() {
                    override fun onStartSuccess(settings: AdvertiseSettings) = Unit
                    override fun onStartFailure(errorCode: Int) = Unit
                }

                val scanCallback = object : ScanCallback() {
                    override fun onScanResult(callbackType: Int, result: ScanResult) {
                        val peer = parsePeer(result) ?: return
                        if (peer.peerElement != myElement && cont.isActive) {
                            cont.resume(peer)
                        }
                    }

                    override fun onScanFailed(errorCode: Int) = Unit
                }

                val settings = AdvertiseSettings.Builder()
                    .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                    .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                    .setConnectable(false)
                    .build()
                val advertiseData = AdvertiseData.Builder()
                    .addServiceUuid(SERVICE_UUID)
                    .addServiceData(SERVICE_UUID, myPayload)
                    .build()

                advertiser.startAdvertising(settings, advertiseData, advertiseCallback)

                val filters = listOf(
                    ScanFilter.Builder().setServiceUuid(SERVICE_UUID).build(),
                )
                val scanSettings = ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build()
                scanner.startScan(filters, scanSettings, scanCallback)

                cont.invokeOnCancellation {
                    runCatching { advertiser.stopAdvertising(advertiseCallback) }
                    runCatching { scanner.stopScan(scanCallback) }
                }
            }
        }
    }

    /** 8 байт: [elementId, avatar, randomHi, randomLo, ...]. */
    private fun buildPayload(element: Element?, avatar: Int): ByteArray {
        val elementId = (element?.ordinal ?: 0xFF).toByte()
        val random = (System.currentTimeMillis() % 65_535).toInt()
        return byteArrayOf(
            elementId,
            avatar.toByte(),
            (random shr 8).toByte(),
            (random and 0xFF).toByte(),
        )
    }

    private fun parsePeer(result: ScanResult): PeerHandshake? {
        val data = result.scanRecord?.getServiceData(SERVICE_UUID) ?: return null
        if (data.size < 4) return null

        val elementId = data[0].toInt() and 0xFF
        val avatar = data[1].toInt() and 0xFF
        val random = ((data[2].toInt() and 0xFF) shl 8) or (data[3].toInt() and 0xFF)
        val element = Element.entries.getOrNull(elementId)
        return PeerHandshake(peerElement = element, peerAvatar = avatar, random = random)
    }

    private companion object {
        val SERVICE_UUID = ParcelUuid.fromString("0000FFF0-0000-1000-8000-00805F9B34FB")
        const val HANDSHAKE_TIMEOUT_MS = 3_000L
    }
}