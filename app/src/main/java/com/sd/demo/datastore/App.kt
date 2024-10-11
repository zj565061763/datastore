package com.sd.demo.datastore

import android.app.Application
import android.util.Log
import com.sd.lib.datastore.FDatastore
import kotlinx.coroutines.runBlocking

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

      // 如果值为null，则设置一个默认值
      runBlocking {
         userInfoApi.replace { it ?: UserInfo() }
      }
   }
}