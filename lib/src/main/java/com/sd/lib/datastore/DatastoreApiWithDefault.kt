package com.sd.lib.datastore

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

interface DatastoreApiWithDefault<T> {
  /** 数据流 */
  val flow: Flow<T>

  /** 更新数据 */
  suspend fun update(transform: suspend (T) -> T?)
}

/** 获取数据 */
suspend fun <T> DatastoreApiWithDefault<T>.get(): T = flow.first()

fun <T> DatastoreApi<T>.withDefault(
  getDefault: suspend () -> T,
): DatastoreApiWithDefault<T> {
  return DatastoreApiWithDefaultImpl(
    store = this,
    getDefault = getDefault,
  )
}

private class DatastoreApiWithDefaultImpl<T>(
  private val store: DatastoreApi<T>,
  private val getDefault: suspend () -> T,
) : DatastoreApiWithDefault<T> {

  override val flow: Flow<T>
    get() = store.flow
      .map { it ?: newData(save = true) }
      .distinctUntilChanged()

  override suspend fun update(transform: suspend (T) -> T?) {
    store.update {
      val data = it ?: newData()
      transform(data)
    }
  }

  private suspend fun newData(save: Boolean = false): T {
    return getDefault().also { data ->
      if (save) {
        store.update { it ?: data }
      }
    }
  }
}