package com.sd.lib.datastore

import android.annotation.SuppressLint
import android.content.Context
import java.io.File

@SuppressLint("StaticFieldLeak")
object FDatastore {
   private var _context: Context? = null
   private lateinit var _onError: (DatastoreException) -> Unit

   private var _defaultGroup: DatastoreGroup? = null

   /**
    * 初始化
    */
   @JvmOverloads
   @JvmStatic
   fun init(
      context: Context,
      onError: (DatastoreException) -> Unit = { it.printStackTrace() },
   ): Boolean {
      synchronized(FDatastore) {
         return if (_context == null) {
            _context = context.applicationContext
            _onError = onError
            _context != null
         } else {
            false
         }
      }
   }

   /**
    * 获取[clazz]对应的[DatastoreApi]
    */
   @JvmStatic
   fun <T> get(clazz: Class<T>): DatastoreApi<T> {
      synchronized(FDatastore) {
         val group = _defaultGroup ?: DatastoreGroup(
            directory = getDirectory().resolve("default"),
            onError = _onError,
         ).also { _defaultGroup = it }
         return group.api(clazz)
      }
   }

   @SuppressLint("SdCardPath")
   private fun getDirectory(): File {
      val context = _context ?: error("FDatastore.init() should be called before this.")
      val filesDir = context.filesDir ?: File("/data/data/${context.packageName}/files")
      return filesDir.resolve("sd.lib.datastore")
   }
}