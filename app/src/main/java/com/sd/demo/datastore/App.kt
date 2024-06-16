package com.sd.demo.datastore

import android.app.Application
import android.util.Log
import com.sd.lib.datastore.FDatastore

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // 初始化
        FDatastore.init(
            context = this,
            onError = {
                logMsg { Log.getStackTraceString(it) }
            }
        )
    }
}