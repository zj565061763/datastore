package com.sd.lib.datastore

import kotlinx.coroutines.runBlocking

class DatastoreJavaApi<T>(
   private val api: DatastoreApi<T>,
) {
   /**
    * [DatastoreApi.get]
    */
   fun get(): T? {
      return runBlocking { api.get() }
   }

   /**
    * [DatastoreApi.replace]
    */
   fun replace(data: T?): T? {
      return runBlocking { api.replace(data) }
   }

   /**
    * [DatastoreApi.replace]
    */
   fun replace(transform: (T?) -> T?): T? {
      return runBlocking { api.replace { transform(it) } }
   }

   /**
    * [DatastoreApi.update]
    */
   fun update(transform: (T) -> T): T? {
      return runBlocking { api.update { transform(it) } }
   }
}
