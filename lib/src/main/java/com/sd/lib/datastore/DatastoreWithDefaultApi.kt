package com.sd.lib.datastore

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

fun <T> DatastoreApi<T>.withDefault(
   getDefault: suspend () -> T,
): DatastoreWithDefaultApi<T> {
   return DatastoreWithDefaultApiImpl(
      store = this,
      getDefault = getDefault,
   )
}

interface DatastoreWithDefaultApi<T> {
   /** 数据流 */
   val flow: Flow<T>

   /** 用[transform]的结果替换数据 */
   suspend fun update(transform: suspend (T) -> T): T
}

/** 获取数据 */
suspend fun <T> DatastoreWithDefaultApi<T>.get(): T {
   return flow.first()
}

private class DatastoreWithDefaultApiImpl<T>(
   private val store: DatastoreApi<T>,
   private val getDefault: suspend () -> T,
) : DatastoreWithDefaultApi<T> {

   override val flow: Flow<T>
      get() = store.flow
         .map { it ?: newData() }
         .distinctUntilChanged()

   override suspend fun update(transform: suspend (T) -> T): T {
      return store.replace {
         val data = it ?: newData()
         transform(data)
      } ?: newData()
   }

   private suspend fun newData(): T {
      return getDefault().also { data ->
         store.replace { it ?: data }
      }
   }
}