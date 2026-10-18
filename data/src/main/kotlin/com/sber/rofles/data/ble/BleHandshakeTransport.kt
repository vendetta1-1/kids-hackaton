package com.sber.rofles.data.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertisingSet
import android.bluetooth.le.AdvertisingSetCallback
import android.bluetooth.le.AdvertisingSetParameters
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.ParcelUuid
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.sber.rofles.domain.model.BleCapability
import com.sber.rofles.domain.repository.HandshakeTransport
import com.sber.rofles.domain.repository.RawAdvert
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BleHandshakeTransport @Inject constructor(
    @ApplicationContext private val context: Context,
) : HandshakeTransport {

    private val adapter get() = context.getSystemService(BluetoothManager::class.java)?.adapter

    private val lock = Any()
    private var advertisingSet: AdvertisingSet? = null
    private var advertisingCallback: AdvertisingSetCallback? = null
    private var pendingData: AdvertiseData? = null
    private val scanStarts = ArrayDeque<Long>()

    override fun capability(): BleCapability {
        val adapter = adapter ?: return BleCapability.UNSUPPORTED
        if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) return BleCapability.UNSUPPORTED
        if (!hasPermissions(context)) return BleCapability.NO_PERMISSION
        if (!adapter.isEnabled) return BleCapability.OFF
        if (!adapter.isMultipleAdvertisementSupported) return BleCapability.UNSUPPORTED
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S && !isLocationEnabled()) return BleCapability.OFF
        return BleCapability.FULL
    }

    @SuppressLint("MissingPermission")
    override fun scan(): Flow<RawAdvert> {
        val scanner = adapter?.bluetoothLeScanner ?: return emptyFlow()
        if (!reserveScanStart()) return emptyFlow()
        return callbackFlow {
            val callback = object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult) {
                    result.scanRecord?.serviceUuids?.forEach { trySend(RawAdvert(it.uuid, result.rssi)) }
                }

                override fun onBatchScanResults(results: MutableList<ScanResult>) {
                    results.forEach { onScanResult(ScanSettings.CALLBACK_TYPE_ALL_MATCHES, it) }
                }

                override fun onScanFailed(errorCode: Int) {
                    close()
                }
            }
            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .setReportDelay(0)
                .build()
            runCatching { scanner.startScan(null, settings, callback) }.onFailure { close() }
            awaitClose { runCatching { scanner.stopScan(callback) } }
        }
    }

    @SuppressLint("MissingPermission")
    override fun advertise(uuid: UUID) {
        val advertiser = adapter?.bluetoothLeAdvertiser ?: return
        val data = AdvertiseData.Builder()
            .addServiceUuid(ParcelUuid(uuid))
            .setIncludeDeviceName(false)
            .setIncludeTxPowerLevel(false)
            .build()
        synchronized(lock) {
            pendingData = data
            val set = advertisingSet
            if (set != null) {
                runCatching { set.setAdvertisingData(data) }
                return
            }
            if (advertisingCallback != null) return
            val callback = object : AdvertisingSetCallback() {
                override fun onAdvertisingSetStarted(advertisingSet: AdvertisingSet?, txPower: Int, status: Int) {
                    synchronized(lock) {
                        if (advertisingCallback !== this) return
                        if (status != ADVERTISE_SUCCESS || advertisingSet == null) {
                            advertisingCallback = null
                            return
                        }
                        this@BleHandshakeTransport.advertisingSet = advertisingSet
                        pendingData?.takeIf { it != data }?.let { runCatching { advertisingSet.setAdvertisingData(it) } }
                    }
                }
            }
            val parameters = AdvertisingSetParameters.Builder()
                .setLegacyMode(true)
                .setConnectable(false)
                .setScannable(false)
                .setInterval(AdvertisingSetParameters.INTERVAL_MIN)
                .setTxPowerLevel(AdvertisingSetParameters.TX_POWER_HIGH)
                .build()
            advertisingCallback = callback
            runCatching { advertiser.startAdvertisingSet(parameters, data, null, null, null, callback) }
                .onFailure { advertisingCallback = null }
        }
    }

    @SuppressLint("MissingPermission")
    override fun stop() {
        synchronized(lock) {
            val callback = advertisingCallback ?: return
            runCatching { adapter?.bluetoothLeAdvertiser?.stopAdvertisingSet(callback) }
            advertisingCallback = null
            advertisingSet = null
            pendingData = null
        }
    }

    private fun reserveScanStart(): Boolean = synchronized(scanStarts) {
        val now = System.currentTimeMillis()
        while (scanStarts.isNotEmpty() && now - scanStarts.first() > SCAN_WINDOW_MS) scanStarts.removeFirst()
        if (scanStarts.size >= MAX_SCAN_STARTS) return false
        scanStarts.addLast(now)
        true
    }

    private fun isLocationEnabled(): Boolean =
        context.getSystemService(LocationManager::class.java)?.let(LocationManagerCompat::isLocationEnabled) ?: false

    companion object {
        private const val SCAN_WINDOW_MS = 30_000L
        private const val MAX_SCAN_STARTS = 5

        fun requiredPermissions(): Array<String> =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.BLUETOOTH_CONNECT,
                )
            } else {
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            }

        fun hasPermissions(context: Context): Boolean = requiredPermissions().all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}
