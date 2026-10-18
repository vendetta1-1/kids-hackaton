package com.sber.rofles.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.sber.rofles.domain.model.AlbumEntry
import com.sber.rofles.domain.model.ComboKey
import com.sber.rofles.domain.model.Element
import com.sber.rofles.domain.model.MyElement
import com.sber.rofles.domain.model.PrankSettings
import com.sber.rofles.domain.repository.AlbumRepository
import com.sber.rofles.domain.repository.MyElementRepository
import com.sber.rofles.domain.repository.PrankSettingsRepository
import com.sber.rofles.domain.usecase.AlbumBook
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrankPreferences @Inject constructor(
    @ApplicationContext context: Context,
) {
    val prefs: SharedPreferences = context.getSharedPreferences("prank", Context.MODE_PRIVATE)

    fun changes(): Flow<SharedPreferences> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { p, _ -> trySend(p) }
        trySend(prefs)
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }.conflate()
}

@Singleton
class MyElementRepositoryImpl @Inject constructor(
    private val store: PrankPreferences,
) : MyElementRepository {

    override fun observe(): Flow<MyElement?> = store.changes().map { prefs ->
        val element = Element.fromId(prefs.getInt(KEY_ELEMENT, 0)) ?: return@map null
        MyElement(element, LocalDate.ofEpochDay(prefs.getLong(KEY_CHANGED_ON, 0)))
    }.distinctUntilChanged()

    override suspend fun save(element: MyElement) = withContext(Dispatchers.IO) {
        store.prefs.edit(commit = true) {
            putInt(KEY_ELEMENT, element.element.id)
            putLong(KEY_CHANGED_ON, element.changedOn.toEpochDay())
        }
    }

    private companion object {
        const val KEY_ELEMENT = "my_element"
        const val KEY_CHANGED_ON = "my_element_at"
    }
}

@Singleton
class AlbumRepositoryImpl @Inject constructor(
    private val store: PrankPreferences,
) : AlbumRepository {

    private val mutex = Mutex()

    override fun observe(): Flow<List<AlbumEntry>> =
        store.changes().map { read(it).values.sortedBy { entry -> entry.firstAt } }.distinctUntilChanged()

    override suspend fun record(key: ComboKey, now: Long): AlbumEntry = mutex.withLock {
        withContext(Dispatchers.IO) {
            val updated = AlbumBook.record(read(store.prefs), key, now)
            store.prefs.edit(commit = true) { putString(KEY_ALBUM, write(updated)) }
            updated.getValue(key)
        }
    }

    private fun read(prefs: SharedPreferences): Map<ComboKey, AlbumEntry> {
        val json = runCatching { JSONObject(prefs.getString(KEY_ALBUM, null) ?: "{}") }.getOrElse { JSONObject() }
        return json.keys().asSequence().mapNotNull { id ->
            val key = ComboKey.parse(id) ?: return@mapNotNull null
            val item = json.optJSONObject(id) ?: return@mapNotNull null
            key to AlbumEntry(key, item.optLong("f"), item.optInt("c", 1))
        }.toMap()
    }

    private fun write(entries: Map<ComboKey, AlbumEntry>): String {
        val json = JSONObject()
        entries.values.forEach { entry ->
            json.put(entry.key.id, JSONObject().put("f", entry.firstAt).put("c", entry.count))
        }
        return json.toString()
    }

    private companion object {
        const val KEY_ALBUM = "album_v1"
    }
}

@Singleton
class PrankSettingsRepositoryImpl @Inject constructor(
    private val store: PrankPreferences,
) : PrankSettingsRepository {

    override val settings: Flow<PrankSettings> = store.changes().map { prefs ->
        PrankSettings(
            highFiveEnabled = prefs.getBoolean(KEY_HIGH_FIVE, true),
            vibrationEnabled = prefs.getBoolean(KEY_VIBRATION, true),
            soundEnabled = prefs.getBoolean(KEY_SOUND, false),
            bluetoothOnboardingShown = prefs.getBoolean(KEY_BT_ONBOARDING, false),
        )
    }.distinctUntilChanged()

    override suspend fun setHighFiveEnabled(enabled: Boolean) = put(KEY_HIGH_FIVE, enabled)

    override suspend fun setVibrationEnabled(enabled: Boolean) = put(KEY_VIBRATION, enabled)

    override suspend fun setSoundEnabled(enabled: Boolean) = put(KEY_SOUND, enabled)

    override suspend fun setBluetoothOnboardingShown() = put(KEY_BT_ONBOARDING, true)

    private suspend fun put(key: String, value: Boolean) = withContext(Dispatchers.IO) {
        store.prefs.edit(commit = true) { putBoolean(key, value) }
    }

    private companion object {
        const val KEY_HIGH_FIVE = "high_five_enabled"
        const val KEY_VIBRATION = "vibration_enabled"
        const val KEY_SOUND = "sound_enabled"
        const val KEY_BT_ONBOARDING = "bt_onboarding_shown"
    }
}
