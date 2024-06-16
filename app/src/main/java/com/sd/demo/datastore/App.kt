package com.sd.demo.datastore

import android.app.Application
import android.util.Log
import com.sd.demo.datastore.model.version1.UserInfo
import com.sd.lib.datastore.FDatastore
import com.sd.lib.datastore.replaceBlocking

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

val userInfoDatastoreApi = FDatastore.defaultGroupApi(UserInfo::class.java)
    .apply {
        replaceBlocking {
            it ?: UserInfo(age = 0)
        }
    }