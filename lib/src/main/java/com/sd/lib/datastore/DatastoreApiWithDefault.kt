package com.sd.lib.datastore

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

fun <T> DatastoreApi<T>.withDefault(
  getDefault: suspend () -> T,
): DatastoreApiWithDefault<T> {
  return DatastoreApiWithDefaultImpl(
    store = this,
    getDefault = getDefault,
  )
}

interface DatastoreApiWithDefault<T> {
  /** 数据流 */
  val flow: Flow<T>

  /** 用[transform]的结果替换数据 */
  suspend fun update(transform: suspend (T) -> T): T
}

/** 获取数据 */
suspend fun <T> DatastoreApiWithDefault<T>.get(): T {
  return flow.first()
}

private class DatastoreApiWithDefaultImpl<T>(
  private val store: DatastoreApi<T>,
  private val getDefault: suspend () -> T,
) : DatastoreApiWithDefault<T> {

  override val flow: Flow<T>
    get() = store.flow
      .map { it ?: newData(save = true) }
      .distinctUntilChanged()

  override suspend fun update(transform: suspend (T) -> T): T {
    return store.replace {
      val data = it ?: newData()
      transform(data)
    } ?: newData()
  }

  private suspend fun newData(save: Boolean = false): T {
    return getDefault().also { data ->
      if (save) {
        store.replace { it ?: data }
      }
    }
  }
}