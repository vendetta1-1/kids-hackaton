package com.sber.rofles.feature.album

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sber.rofles.domain.model.AlbumEntry
import com.sber.rofles.domain.model.ComboKey
import com.sber.rofles.domain.model.Element
import com.sber.rofles.domain.model.ElementStyle
import com.sber.rofles.domain.model.PrankConfig
import com.sber.rofles.domain.model.Skin
import com.sber.rofles.domain.repository.AlbumRepository
import com.sber.rofles.domain.repository.Clock
import com.sber.rofles.domain.repository.PrankConfigRepository
import com.sber.rofles.domain.usecase.ResolveSkin
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class AlbumCard(
    val key: ComboKey,
    val title: String,
    val elementA: ElementStyle?,
    val elementB: ElementStyle?,
    val colorA: Long,
    val colorB: Long,
    val count: Int,
    val firstAt: Long,
) {
    val isOpen: Boolean get() = count > 0
}

data class AlbumTexts(
    val title: String,
    val shareText: String,
    val shareButton: String,
    val brand: String,
    val progress: String,
)

sealed interface AlbumUiState {
    val texts: AlbumTexts

    data class Loading(override val texts: AlbumTexts) : AlbumUiState

    data class Unavailable(override val texts: AlbumTexts) : AlbumUiState

    data class Ready(
        override val texts: AlbumTexts,
        val cards: List<AlbumCard>,
    ) : AlbumUiState {
        val opened: Int get() = cards.count { it.isOpen }
        val total: Int get() = cards.size
    }
}

@HiltViewModel
class AlbumViewModel @Inject constructor(
    albumRepository: AlbumRepository,
    configRepository: PrankConfigRepository,
    private val clock: Clock,
) : ViewModel() {

    val state: StateFlow<AlbumUiState> = combine(
        configRepository.config,
        albumRepository.observe(),
    ) { config, entries -> build(config, entries) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AlbumUiState.Loading(textsOf(configRepository.config.value)),
        )

    private fun build(config: PrankConfig?, entries: List<AlbumEntry>): AlbumUiState {
        val texts = textsOf(config)
        if (config == null) return AlbumUiState.Unavailable(texts)
        val skin = runCatching { ResolveSkin(config, clock.today()) }.getOrNull()
            ?: return AlbumUiState.Unavailable(texts)
        val byKey = entries.associateBy { it.key }
        val cards = ComboKey.ALL.map { key ->
            AlbumCard(
                key = key,
                title = skin.combos[key]?.title ?: config.fallbackTitle,
                elementA = skin.elements[key.a],
                elementB = skin.elements[key.b],
                colorA = skin.colorOf(key.a),
                colorB = skin.colorOf(key.b),
                count = byKey[key]?.count ?: 0,
                firstAt = byKey[key]?.firstAt ?: 0L,
            )
        }
        return AlbumUiState.Ready(texts, cards)
    }

    private fun Skin.colorOf(element: Element): Long = elements[element]?.color ?: FALLBACK_COLOR

    private fun textsOf(config: PrankConfig?): AlbumTexts = AlbumTexts(
        title = config?.text("albumTitle", DEFAULT_TITLE) ?: DEFAULT_TITLE,
        shareText = config?.text("shareText", DEFAULT_SHARE_TEXT) ?: DEFAULT_SHARE_TEXT,
        shareButton = config?.text("shareButton", DEFAULT_SHARE_BUTTON) ?: DEFAULT_SHARE_BUTTON,
        brand = config?.text("shareBrand", DEFAULT_BRAND) ?: DEFAULT_BRAND,
        progress = config?.text("albumProgress", DEFAULT_PROGRESS) ?: DEFAULT_PROGRESS,
    )

    private companion object {
        const val DEFAULT_TITLE = "Мой альбом"
        const val DEFAULT_SHARE_TEXT = "Мы создали"
        const val DEFAULT_SHARE_BUTTON = "Поделиться"
        const val DEFAULT_BRAND = "Дай пять!"
        const val DEFAULT_PROGRESS = "карточек собрано"
        const val FALLBACK_COLOR = 0xFFA8B4BDL
    }
}
