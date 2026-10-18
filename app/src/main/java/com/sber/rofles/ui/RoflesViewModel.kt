package com.sber.rofles.ui

import androidx.lifecycle.ViewModel
import com.sber.rofles.core.effect.EffectState
import com.sber.rofles.core.effect.PrikolCoordinator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/** ViewModel приложения: держит координатор прикола и его состояние. */
@HiltViewModel
class RoflesViewModel @Inject constructor(
    private val coordinator: PrikolCoordinator,
) : ViewModel() {

    val effect: StateFlow<EffectState?> = coordinator.effect

    init {
        coordinator.start()
    }

    fun dismissEffect() {
        coordinator.dismiss()
    }

    override fun onCleared() {
        coordinator.stop()
    }
}