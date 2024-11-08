package com.sd.demo.datastore

import android.app.Application
import com.sd.lib.datastore.FDatastore

class App : Application() {
   override fun onCreate() {
      super.onCreate()
      FDatastore.init(
         context = this,
         onError = {
            logMsg { it.stackTraceToString() }
         }
      )
   }
}