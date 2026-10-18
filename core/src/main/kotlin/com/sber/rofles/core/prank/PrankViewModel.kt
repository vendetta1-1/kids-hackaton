package com.sber.rofles.core.prank

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PrankViewModel @Inject constructor(
    val controller: PrankController,
) : ViewModel()
