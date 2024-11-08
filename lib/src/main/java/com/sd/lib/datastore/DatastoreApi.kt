package com.sd.lib.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.core.MultiProcessDataStoreFactory
import androidx.datastore.core.Serializer
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import com.sd.lib.moshi.fMoshi
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File
import java.io.InputStream
import java.io.OutputStream

interface DatastoreApi<T> {
   /** 数据流 */
   val flow: Flow<T?>

   /** 用[transform]的结果替换数据 */
   suspend fun replace(transform: suspend (T?) -> T?): T?
}

/** 获取数据 */
suspend fun <T> DatastoreApi<T>.get(): T? {
   return flow.first()
}

/** 数据不为null，才会调用[transform]更新数据 */
suspend fun <T> DatastoreApi<T>.update(transform: suspend (T) -> T): T? {
   return replace { data ->
      if (data == null) {
         null
      } else {
         transform(data)
      }
   }
}

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
   private val _serializer = ModelSerializer<T>()
   private val _datastore: DataStore<Model<T>> = MultiProcessDataStoreFactory.create(
      serializer = _serializer,
      corruptionHandler = ReplaceFileCorruptionHandler { _serializer.defaultValue },
      produceFile = { file },
   )

   override val flow: Flow<T?>
      get() = _datastore.data.map { it.data }

   override suspend fun replace(transform: suspend (T?) -> T?): T? {
      return updateData { model ->
         val newData = transform(model.data)
         model.copy(data = newData)
      }?.data
   }

   private suspend fun updateData(transform: suspend (Model<T>) -> Model<T>): Model<T>? {
      return runCatching {
         _datastore.updateData { data ->
            try {
               transform(data)
            } catch (e: Throwable) {
               throw TransformException(e)
            }
         }
      }.getOrElse { e ->
         if (e is TransformException) throw e
         if (e is CancellationException) throw e
         onError(
            DatastoreWriteDataException(
               message = "Write data error ${clazz.name}",
               cause = e,
            )
         )
         null
      }
   }

   private inner class ModelSerializer<T> : Serializer<Model<T>> {
      private val _jsonAdapter: JsonAdapter<Model<T>> = fMoshi.adapter(
         Types.newParameterizedType(Model::class.java, clazz)
      )

      override val defaultValue: Model<T> = Model(data = null)

      @Suppress("BlockingMethodInNonBlockingContext")
      override suspend fun writeTo(t: Model<T>, output: OutputStream) {
         val json = _jsonAdapter.toJson(t)
         output.write(json.toByteArray())
      }

      override suspend fun readFrom(input: InputStream): Model<T> {
         return runCatching {
            val json = input.readBytes().decodeToString()
            checkNotNull(_jsonAdapter.fromJson(json))
         }.getOrElse { e ->
            onError(
               DatastoreReadDataException(
                  message = "Read data error ${clazz.name}",
                  cause = e,
               )
            )
            defaultValue
         }
      }
   }
}

internal data class Model<T>(
   val data: T?,
)

private class TransformException(cause: Throwable) : Exception(cause)