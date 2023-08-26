package com.example.runningapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp // Note that when we add this dagger hilt  then we get some dagger hilt generated files that we should not touch
// This will tell dagger that this is our application
// Then we have to go the manifest file to make change the name of our application to ".BaseApplication"
class BaseApplication : Application() { // Note that wen need to create this class to represent our application
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}