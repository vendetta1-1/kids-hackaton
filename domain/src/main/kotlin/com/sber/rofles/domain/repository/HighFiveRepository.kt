package com.sber.rofles.domain.repository

import com.sber.rofles.domain.model.Combination
import com.sber.rofles.domain.model.Element
import kotlinx.coroutines.flow.Flow

/**
 * Порт (интерфейс) для данных прикола «Дай пять!».
 * Реализуется в модуле :data. Домен не знает про Android, сеть и Bluetooth.
 */
interface HighFiveRepository {

    /** Выбранная стихия пользователя (null, пока не выбрана). */
    fun currentElement(): Flow<Element?>

    /** Поменять стихию (не чаще раза в день — проверка в домене). */
    suspend fun setElement(element: Element): Result<Unit>

    /** Список открытых карточек альбома (комбинаций). */
    fun album(): Flow<Set<Combination>>

    /** Сохранить новую карточку после «дай пять». */
    suspend fun addCard(combination: Combination): Result<Unit>

    /** Включён ли прикол «Дай пять» в настройках. */
    fun isEnabled(): Flow<Boolean>
}