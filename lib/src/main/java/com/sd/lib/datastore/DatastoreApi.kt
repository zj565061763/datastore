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
  /**
   * 数据流
   * @throws DatastoreReadDataException 当数据读取异常时
   */
  val flow: Flow<T?>

  /**
   * 更新数据，当数据写入异常时抛出[DatastoreWriteDataException]，
   * 如果是[transform]的异常则直接抛出
   */
  @Throws(DatastoreWriteDataException::class)
  suspend fun update(transform: suspend (T?) -> T?)
}

/** [DatastoreApi.flow] */
@Throws(DatastoreReadDataException::class)
suspend fun <T> DatastoreApi<T>.get(): T? = flow.first()

internal fun <T> DatastoreApi(
  file: File,
  clazz: Class<T>,
): DatastoreApi<T> {
  return DatastoreApiImpl(file = file, clazz = clazz)
}

private class DatastoreApiImpl<T>(
  file: File,
  private val clazz: Class<T>,
) : DatastoreApi<T> {
  private val _serializer = ModelSerializer(clazz)
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
      when (e) {
        is androidx.datastore.core.IOException,
        is com.squareup.moshi.JsonDataException,
          -> throw DatastoreWriteDataException(message = "Write data error ${clazz.name}", cause = e)
        else -> {
          // 其他异常不包装，直接抛出
          throw e
        }
      }
    }
  }
}

private class ModelSerializer<T>(
  private val clazz: Class<T>,
) : Serializer<Model<T>> {
  private val _jsonAdapter: JsonAdapter<Model<T>> = fMoshi.adapter(
    Types.newParameterizedType(Model::class.java, clazz)
  )

  override val defaultValue: Model<T> = Model(data = null)

  override suspend fun readFrom(input: InputStream): Model<T> {
    return runCatching {
      val source = input.source().buffer()
      _jsonAdapter.fromJson(source) ?: defaultValue
    }.getOrElse { e ->
      when (e) {
        is kotlinx.coroutines.CancellationException -> throw e
        is com.squareup.moshi.JsonDataException,
        is java.io.EOFException,
          -> throw androidx.datastore.core.CorruptionException("Read data error")
        else -> throw DatastoreReadDataException(message = "Read data error ${clazz.name}", cause = e)
      }
    }
  }

  override suspend fun writeTo(t: Model<T>, output: OutputStream) {
    val sink = output.sink().buffer()
    _jsonAdapter.toJson(sink, t)
    sink.flush()
  }
}

internal data class Model<T>(
  val data: T?,
)