package com.sd.lib.datastore

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

interface DatastoreApiWithDefault<T> {
  /** 数据流，如果数据不存在或数据读取异常，则返回默认值 */
  val flow: Flow<T>

  /**
   * 更新数据，如果数据不存在则把默认值传递给[transform]，
   * 当数据写入异常时抛出[DatastoreWriteDataException]，
   * [transform]的异常直接抛出。
   */
  @Throws(DatastoreWriteDataException::class)
  suspend fun update(transform: suspend (T) -> T?)
}

/** [DatastoreApiWithDefault.flow] */
suspend fun <T> DatastoreApiWithDefault<T>.get(): T = flow.first()

fun <T> DatastoreApi<T>.withDefault(
  onError: (Throwable) -> Unit = {},
  getDefault: suspend () -> T,
): DatastoreApiWithDefault<T> {
  return DatastoreApiWithDefaultImpl(
    store = this,
    onError = onError,
    getDefault = getDefault,
  )
}

private class DatastoreApiWithDefaultImpl<T>(
  private val store: DatastoreApi<T>,
  private val onError: (Throwable) -> Unit,
  private val getDefault: suspend () -> T,
) : DatastoreApiWithDefault<T> {

  override val flow: Flow<T>
    get() = store.flow
      .catch { e ->
        when (e) {
          is kotlinx.coroutines.CancellationException -> throw e
          else -> emit(null).also { onError(e) }
        }
      }
      .map { it ?: getDefault() }
      .distinctUntilChanged()

  override suspend fun update(transform: suspend (T) -> T?) {
    store.update {
      val data = it ?: getDefault()
      transform(data)
    }
  }
}