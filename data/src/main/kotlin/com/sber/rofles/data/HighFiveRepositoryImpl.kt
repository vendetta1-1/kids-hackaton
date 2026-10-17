package com.sber.rofles.data

import com.sber.rofles.domain.model.Combination
import com.sber.rofles.domain.model.Element
import com.sber.rofles.domain.repository.HighFiveRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * In-memory реализация репозитория. Позже заменяется на DataStore (альбом,
 * выбранная стихия, переключатель) и Bluetooth/конфиг с CMS.
 */
class HighFiveRepositoryImpl : HighFiveRepository {

    private val _element = MutableStateFlow<Element?>(null)
    private val _album = MutableStateFlow<Set<Combination>>(emptySet())
    private val _enabled = MutableStateFlow(true)

    override fun currentElement(): Flow<Element?> = _element

    override suspend fun setElement(element: Element): Result<Unit> =
        runCatching { _element.value = element }

    override fun album(): Flow<Set<Combination>> = _album

    override suspend fun addCard(combination: Combination): Result<Unit> =
        runCatching { _album.value += combination }

    override fun isEnabled(): Flow<Boolean> = _enabled
}