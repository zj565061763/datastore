package com.sd.lib.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.core.MultiProcessDataStoreFactory
import androidx.datastore.core.Serializer
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import com.sd.lib.moshi.fMoshi
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import okio.buffer
import okio.sink
import okio.source
import java.io.File
import java.io.InputStream
import java.io.OutputStream

interface DatastoreApi<T> {
  /** 数据流 */
  val flow: Flow<T?>

  /** 更新数据 */
  @Throws(DatastoreWriteDataException::class)
  suspend fun update(transform: suspend (T?) -> T?)
}

/** 获取数据 */
suspend fun <T> DatastoreApi<T>.get(): T? = flow.first()

internal fun <T> DatastoreApi(
  file: File,
  clazz: Class<T>,
  onError: (DatastoreException) -> Unit,
): DatastoreApi<T> {
  return DatastoreApiImpl(
    file = file,
    clazz = clazz,
    onError = onError,
  )
}

private class DatastoreApiImpl<T>(
  file: File,
  private val clazz: Class<T>,
  private val onError: (DatastoreException) -> Unit,
) : DatastoreApi<T> {
  private val _serializer = ModelSerializer(clazz, onError)
  private val _datastore: DataStore<Model<T>> = MultiProcessDataStoreFactory.create(
    serializer = _serializer,
    corruptionHandler = ReplaceFileCorruptionHandler { _serializer.defaultValue },
    produceFile = { file },
  )

  override val flow: Flow<T?>
    get() = _datastore.data.map { it.data }

  override suspend fun update(transform: suspend (T?) -> T?) {
    updateData { model ->
      val newData = transform(model.data)
      model.copy(data = newData)
    }
  }

  @Throws(DatastoreWriteDataException::class)
  private suspend fun updateData(transform: suspend (Model<T>) -> Model<T>) {
    runCatching {
      _datastore.updateData { data ->
        transform(data)
      }
    }.onFailure { e ->
      if (e is androidx.datastore.core.IOException) {
        throw DatastoreWriteDataException(message = "Write data error ${clazz.name}", cause = e)
      } else {
        throw e
      }
    }
  }
}

private class ModelSerializer<T>(
  private val clazz: Class<T>,
  private val onError: (DatastoreException) -> Unit,
) : Serializer<Model<T>> {
  private val _jsonAdapter: JsonAdapter<Model<T>> = fMoshi.adapter(
    Types.newParameterizedType(Model::class.java, clazz)
  )

  override val defaultValue: Model<T> = Model(data = null)

  override suspend fun writeTo(t: Model<T>, output: OutputStream) {
    val sink = output.sink().buffer()
    _jsonAdapter.toJson(sink, t)
    sink.flush()
  }

  override suspend fun readFrom(input: InputStream): Model<T> {
    return runCatching {
      val source = input.source().buffer()
      _jsonAdapter.fromJson(source) ?: defaultValue
    }.getOrElse { e ->
      onError(DatastoreReadDataException(message = "Read data error ${clazz.name}", cause = e))
      defaultValue
    }
  }
}

internal data class Model<T>(
  val data: T?,
)