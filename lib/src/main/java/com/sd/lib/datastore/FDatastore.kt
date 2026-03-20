package com.sd.lib.datastore

import android.content.Context
import java.io.File
import java.util.concurrent.atomic.AtomicReference

object FDatastore {
  private val _context = AtomicReference<Context?>(null)
  private var _defaultGroup: DatastoreGroup? = null

  /**
   * 默认在主进程自动初始化，其他进程需要手动初始化。
   */
  @JvmStatic
  fun init(context: Context?) {
    _context.compareAndSet(null, context?.applicationContext)
  }

  /**
   * 获取[clazz]对应的[DatastoreApi]
   */
  @JvmStatic
  fun <T> get(clazz: Class<T>): DatastoreApi<T> {
    synchronized(FDatastore) {
      return getDefaultGroup().get(clazz)
    }
  }

  private fun getDefaultGroup(): DatastoreGroup {
    return _defaultGroup ?: kotlin.run {
      DatastoreGroup(directory = getDirectory().resolve("default"))
        .also { _defaultGroup = it }
    }
  }

  private fun getDirectory(): File {
    val context = _context.get() ?: error("FDatastore.init() should be called before this.")
    return context.filesDir.resolve("sd.lib.datastore")
  }
}