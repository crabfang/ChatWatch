package com.cabe.app.watch

import android.app.Application
import com.blankj.utilcode.util.Utils

class MyApp: Application() {
    companion object {
        lateinit var instances: Application
    }
    override fun onCreate() {
        super.onCreate()
        instances = this
        Utils.init(this)
    }
}