package com.sd.lib.datastore

import android.annotation.SuppressLint
import android.content.Context

@SuppressLint("StaticFieldLeak")
object FDatastore {
    private const val DEFAULT_GROUP = "com.sd.lib.datastore.group.default"

    private var _context: Context? = null
    private lateinit var _onError: (Throwable) -> Unit

    /** DefaultGroup */
    private val _defaultGroup by lazy { newGroup(DEFAULT_GROUP) }

    private val context: Context
        get() = _context ?: synchronized(this@FDatastore) {
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
    ) {
        synchronized(this@FDatastore) {
            if (_context == null) {
                _context = context.applicationContext
                _onError = onError
            } else {
                error("Initialized.")
            }
        }
    }

    /**
     * 默认分组
     */
    @JvmStatic
    fun defaultGroup(): DatastoreGroup = _defaultGroup

    /**
     * 默认分组下[clazz]对应的默认api
     */
    @JvmStatic
    fun <T> defaultGroupApi(clazz: Class<T>): DatastoreApi<T> {
        return defaultGroup().type(clazz).api()
    }

    private fun newGroup(group: String): DatastoreGroup {
        require(group.isNotEmpty()) { "group is empty" }
        val directory = context.filesDir
            .resolve("f_datastore")
            .resolve(fMd5(group))
        return DatastoreGroup(
            directory = directory,
            onError = _onError,
        )
    }
}