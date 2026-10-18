package com.sber.rofles.ui.shell.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sber.rofles.domain.model.PrankSettings
import com.sber.rofles.domain.repository.PrankSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SoundsViewModel @Inject constructor(
    private val repository: PrankSettingsRepository,
) : ViewModel() {

    val settings: StateFlow<PrankSettings> =
        repository.settings.stateIn(viewModelScope, SharingStarted.Eagerly, PrankSettings())

    fun setVibration(enabled: Boolean) {
        viewModelScope.launch { repository.setVibrationEnabled(enabled) }
    }

    fun setHighFive(enabled: Boolean) {
        viewModelScope.launch { repository.setHighFiveEnabled(enabled) }
    }
}
