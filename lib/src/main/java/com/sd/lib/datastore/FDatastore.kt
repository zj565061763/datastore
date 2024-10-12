package com.sd.lib.datastore

import android.annotation.SuppressLint
import android.content.Context
import java.io.File

@SuppressLint("StaticFieldLeak")
object FDatastore {
   private var _context: Context? = null
   private lateinit var _onError: (Throwable) -> Unit

   private var _defaultGroup: DatastoreGroup? = null

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
      synchronized(FDatastore) {
         _defaultGroup?.let { return it.api(clazz) }
         DatastoreGroup(
            directory = getDirectory().resolve("default"),
            onError = _onError,
         ).also {
            _defaultGroup = it
            return it.api(clazz)
         }
      }
   }

   @SuppressLint("SdCardPath")
   private fun getDirectory(): File {
      val filesDir = context.filesDir ?: File("/data/data/${context.packageName}/files")
      return filesDir.resolve("sd.lib.datastore")
   }

   private val context: Context
      get() = _context ?: synchronized(FDatastore) {
         checkNotNull(_context) { "FDatastore.init() should be called before this." }
      }
}