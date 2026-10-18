package com.sber.rofles.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sber.rofles.domain.model.Combination
import com.sber.rofles.domain.model.Element
import com.sber.rofles.domain.repository.HighFiveRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

private val Context.prikolDataStore by preferencesDataStore(name = "prikol")

/**
 * Реализация репозитория на DataStore (Preferences): выбранная стихия,
 * альбом карточек и переключатель «Дай пять» хранятся локально.
 */
@Singleton
class HighFiveRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : HighFiveRepository {

    private object Keys {
        val ELEMENT = stringPreferencesKey("element")
        val ALBUM = stringSetPreferencesKey("album")
        val ENABLED = booleanPreferencesKey("enabled")
        val LAST_CHANGE = longPreferencesKey("last_element_change")
    }

    override fun currentElement(): Flow<Element?> =
        context.prikolDataStore.data.map { prefs ->
            prefs[Keys.ELEMENT]?.let { name -> Element.entries.firstOrNull { it.name == name } }
        }

    override suspend fun setElement(element: Element): Result<Unit> = runCatching {
        val today = LocalDate.now().toEpochDay()
        val lastChange = context.prikolDataStore.data.first()[Keys.LAST_CHANGE] ?: 0L
        check(today != lastChange) { "Стихию можно менять не чаще раза в день" }

        context.prikolDataStore.edit { prefs ->
            prefs[Keys.ELEMENT] = element.name
            prefs[Keys.LAST_CHANGE] = today
        }
    }

    override fun album(): Flow<Set<Combination>> =
        context.prikolDataStore.data.map { prefs ->
            prefs[Keys.ALBUM]
                ?.mapNotNull { id -> Combination.ALL.firstOrNull { it.id.toString() == id } }
                ?.toSet()
                ?: emptySet()
        }

    override suspend fun addCard(combination: Combination): Result<Unit> = runCatching {
        context.prikolDataStore.edit { prefs ->
            val current = prefs[Keys.ALBUM] ?: emptySet()
            prefs[Keys.ALBUM] = current + combination.id.toString()
        }
    }

    override fun isEnabled(): Flow<Boolean> =
        context.prikolDataStore.data.map { prefs -> prefs[Keys.ENABLED] ?: true }
}