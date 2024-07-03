package com.sd.lib.datastore

import android.annotation.SuppressLint
import android.content.Context

@SuppressLint("StaticFieldLeak")
object FDatastore {
    private var _context: Context? = null
    private lateinit var _onError: (Throwable) -> Unit

    /** 保存文件夹 */
    private val _directory by lazy {
        context.filesDir.resolve("sd.lib.datastore")
    }

    /** DefaultGroup */
    private val _defaultGroup by lazy {
        DatastoreGroup(
            directory = _directory.resolve("default"),
            onError = _onError,
        )
    }

    private val context: Context
        get() = _context ?: synchronized(FDatastore) {
            checkNotNull(_context) { "FDatastore.init() should be called before this." }
        }

    /**
     * 初始化
     */
    @JvmOverloads
    @JvmStatic
    fun init(
        context: Context,
        onError: (Throwable) -> Unit = { it.printStackTrace() },
    ): Boolean {
        synchronized(FDatastore) {
            return if (_context == null) {
                _context = context.applicationContext
                _onError = onError
                true
            } else {
                false
            }
        }
    }

    /**
     * 获取[clazz]的Api
     */
    @JvmStatic
    fun <T> api(clazz: Class<T>): DatastoreApi<T> {
        return _defaultGroup.api(clazz)
    }
}