package com.sd.lib.datastore

import android.annotation.SuppressLint
import android.content.Context
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.File

@SuppressLint("StaticFieldLeak")
object FDatastore {
  @Volatile
  private var _context: Context? = null
  private var _defaultGroup: DatastoreGroup? = null

  private val _scope = MainScope()
  private val _errorFlow = MutableSharedFlow<DatastoreException>()

  /** 错误信息流 */
  val errorFlow: Flow<DatastoreException>
    get() = _errorFlow.asSharedFlow()

  /**
   * 默认在主进程自动初始化，其他进程需要手动初始化，初始化方法可以重复调用。
   */
  @JvmStatic
  fun init(context: Context) {
    context.applicationContext?.also { appContext ->
      _context = appContext
    }
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
    return _defaultGroup ?: DatastoreGroup(
      directory = getDirectory().resolve("default"),
      onError = ::notifyError,
    ).also {
      _defaultGroup = it
    }
  }

  private fun notifyError(error: DatastoreException) {
    _scope.launch {
      _errorFlow.emit(error)
    }
  }

  @SuppressLint("SdCardPath")
  private fun getDirectory(): File {
    val context = _context ?: error("FDatastore.init() should be called before this.")
    val filesDir = context.filesDir ?: File("/data/data/${context.packageName}/files")
    return filesDir.resolve("sd.lib.datastore")
  }
}