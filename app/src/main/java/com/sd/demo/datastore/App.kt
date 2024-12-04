package com.sd.demo.datastore

import android.app.Application
import com.sd.lib.datastore.FDatastore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class App : Application() {
   override fun onCreate() {
      super.onCreate()
      GlobalScope.launch {
         FDatastore.errorFlow.collect {
            logMsg { "error:${it.stackTraceToString()}" }
         }
      }
   }
}