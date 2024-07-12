package com.sd.demo.datastore

import android.app.Application
import android.util.Log
import com.sd.demo.datastore.model.UserInfo
import com.sd.lib.datastore.FDatastore
import com.sd.lib.datastore.initIfNullBlocking

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

val userInfoDatastoreApi = FDatastore.api(UserInfo::class.java)
    .apply {
        initIfNullBlocking { UserInfo(age = 0) }
    }