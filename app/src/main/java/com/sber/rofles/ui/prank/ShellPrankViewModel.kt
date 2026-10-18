package com.sber.rofles.ui.prank

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sber.rofles.domain.model.Element
import com.sber.rofles.domain.model.ElementStyle
import com.sber.rofles.domain.repository.AlbumRepository
import com.sber.rofles.domain.repository.Clock
import com.sber.rofles.domain.repository.MyElementRepository
import com.sber.rofles.domain.repository.PrankConfigRepository
import com.sber.rofles.domain.repository.PrankProgressRepository
import com.sber.rofles.domain.usecase.AvatarFrames
import com.sber.rofles.domain.usecase.ResolveSkin
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AvatarFrameUi(val style: ElementStyle)

data class ShellPrankUi(
    val showShakeHint: Boolean = false,
    val frame: AvatarFrameUi? = null,
    val newFrame: ElementStyle? = null,
)

@HiltViewModel
class ShellPrankViewModel @Inject constructor(
    private val progressRepository: PrankProgressRepository,
    albumRepository: AlbumRepository,
    myElementRepository: MyElementRepository,
    configRepository: PrankConfigRepository,
    clock: Clock,
) : ViewModel() {

    val state: StateFlow<ShellPrankUi> = combine(
        progressRepository.progress,
        albumRepository.observe(),
        myElementRepository.observe(),
        configRepository.config,
    ) { progress, album, mine, config ->
        val styles = config?.let { runCatching { ResolveSkin(it, clock.today()).elements }.getOrNull() }.orEmpty()
        val completed = AvatarFrames.completed(album.map { it.key })
        val frame = AvatarFrames.pick(completed, mine?.element)
        val fresh = Element.entries.firstOrNull { it in completed && it !in progress.celebratedFrames }
        ShellPrankUi(
            showShakeHint = config?.enabled == true && !progress.shakeHintShown,
            frame = frame?.let(styles::get)?.let(::AvatarFrameUi),
            newFrame = fresh?.let(styles::get),
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ShellPrankUi())

    fun onShake() {
        if (!state.value.showShakeHint) return
        viewModelScope.launch { progressRepository.markShakeHintShown() }
    }

    fun dismissShakeHint() {
        viewModelScope.launch { progressRepository.markShakeHintShown() }
    }

    fun frameCelebrated(element: Element) {
        viewModelScope.launch { progressRepository.markFrameCelebrated(element) }
    }
}
