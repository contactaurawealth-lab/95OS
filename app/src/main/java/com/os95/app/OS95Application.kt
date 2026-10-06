package com.os95.app

import android.app.Application
import com.os95.app.core.di.OS95AppContainer

class OS95Application : Application() {
    lateinit var container: OS95AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = OS95AppContainer(this)
    }
}
