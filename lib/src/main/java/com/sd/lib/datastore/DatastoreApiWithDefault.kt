package com.sd.lib.datastore

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

interface DatastoreApiWithDefault<T> {
  /**
   * 数据流，如果数据不存在则返回默认值
   * @throws DatastoreReadDataException 当数据读取异常时
   */
  val flow: Flow<T>

  /** 数据流，如果数据不存在或数据读取异常，则返回默认值 */
  fun fallbackToDefaultFlow(): Flow<T>

  /**
   * 更新数据，如果数据不存在则把默认值传递给[transform]
   * @throws DatastoreWriteDataException 当数据写入异常时
   */
  @Throws(DatastoreWriteDataException::class)
  suspend fun update(transform: suspend (T) -> T?)
}

/** [DatastoreApiWithDefault.flow] */
@Throws(DatastoreReadDataException::class)
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
      .map { it ?: getDefault() }
      .distinctUntilChanged()

  override fun fallbackToDefaultFlow(): Flow<T> {
    return store.flow
      .catch { emit(null) }
      .map { it ?: getDefault() }
      .distinctUntilChanged()
  }

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