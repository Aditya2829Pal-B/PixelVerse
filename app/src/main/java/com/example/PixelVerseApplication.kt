package com.example

import android.app.Application
import com.example.di.AppContainer
import com.example.di.DefaultAppContainer

import com.example.data.LocalDataBootstrapper // updated import

class PixelVerseApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
        LocalDataBootstrapper(container).bootstrapDataIfNeeded()
    }
}
