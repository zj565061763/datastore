package com.sd.demo.datastore

import android.app.Application
import com.sd.lib.datastore.FDatastore

class App : Application() {
  override fun onCreate() {
    super.onCreate()
    // 默认在主进程自动初始化，其他进程需要手动初始化，初始化方法可以重复调用。
    FDatastore.init(this)
  }
}