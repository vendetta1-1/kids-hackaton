package com.sber.rofles

import android.app.Application
import com.sber.rofles.prank.Prank
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RoflesApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Prank.configure { avatarIndex = { SHELL_AVATAR_INDEX } }
    }

    private companion object {
        const val SHELL_AVATAR_INDEX = 1
    }
}
