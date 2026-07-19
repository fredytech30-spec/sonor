package com.example.sonor

import android.app.Application
import com.example.sonor.di.androidModule
import com.example.sonor.di.appModule
import com.example.sonor.di.sharedModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class SonorApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@SonorApplication)
            modules(sharedModule, androidModule, appModule)
        }
    }
}
