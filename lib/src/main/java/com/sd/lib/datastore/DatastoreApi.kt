package com.sd.lib.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.core.MultiProcessDataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File

interface DatastoreApi<T> {

   /** 数据流 */
   val dataFlow: Flow<T?>

   /**
    * 数据流，如果数据为空，则调用[factory]创建数据，并根据[save]决定是否保存创建的数据
    */
   fun dataFlow(
      save: Boolean = false,
      factory: () -> T,
   ): Flow<T>

   /** 获取数据 */
   suspend fun get(): T?

   /** 用[data]替换数据 */
   suspend fun replace(data: T?): T?

   /** 用[transform]替换数据 */
   suspend fun replace(transform: suspend (T?) -> T?): T?

   /** 已保存数据不为null，才会调用[transform]更新数据 */
   suspend fun update(transform: suspend (T) -> T): T?
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
   clazz: Class<T>,
   private val onError: (DatastoreException) -> Unit,
) : DatastoreApi<T> {
   private val _serializer = ModelSerializer(
      clazz = clazz,
      onError = { notifyError(it) }
   )

   private val _datastore: DataStore<Model<T>> = MultiProcessDataStoreFactory.create(
      serializer = _serializer,
      corruptionHandler = ReplaceFileCorruptionHandler { _serializer.defaultValue },
      produceFile = { file },
   )

   override val dataFlow: Flow<T?> = _datastore.data
      .catch { notifyError(it) }
      .map { it.data }

   override fun dataFlow(
      save: Boolean,
      factory: () -> T,
   ): Flow<T> {
      return dataFlow.map { data ->
         data ?: factory().also { newData ->
            if (save) {
               replace { it ?: newData }
            }
         }
      }
   }

   override suspend fun get(): T? {
      return dataFlow.first()
   }

   override suspend fun replace(data: T?): T? {
      return replace { data }
   }

   override suspend fun replace(transform: suspend (T?) -> T?): T? {
      return updateData(transform)
   }

   override suspend fun update(transform: suspend (T) -> T): T? {
      return updateData { data ->
         if (data == null) {
            null
         } else {
            transform(data)
         }
      }
   }

   private suspend fun updateData(transform: suspend (T?) -> T?): T? {
      return updateModel { model ->
         val newData = transform(model.data)
         model.copy(data = newData)
      }?.data
   }

   private suspend fun updateModel(transform: suspend (Model<T>) -> Model<T>): Model<T>? {
      return try {
         _datastore.updateData(transform)
      } catch (e: Throwable) {
         notifyError(e)
         null
      }
   }

   private fun notifyError(error: Throwable) {
      when (error) {
         is DatastoreException -> onError(error)
         is java.io.IOException -> onError(DatastoreIOException(cause = error))
         else -> throw error
      }
   }
}