package com.cabe.app.watch

import android.app.Application
import cn.bmob.v3.Bmob
import com.blankj.utilcode.util.Utils

class MyApp: Application() {
    companion object {
        lateinit var instances: Application
    }
    override fun onCreate() {
        super.onCreate()
        instances = this
        Utils.init(this)
        Bmob.initialize(this, "48d8ad06974b26698e3805997347fa85")
    }
}