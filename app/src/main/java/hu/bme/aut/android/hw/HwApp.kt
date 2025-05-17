package hu.bme.aut.android.hw


import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp                 // 🔸 this boots Hilt at app start-up
class HwApp : Application()