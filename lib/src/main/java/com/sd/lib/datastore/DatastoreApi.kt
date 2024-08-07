package com.sd.lib.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.core.MultiProcessDataStoreFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.io.File

interface DatastoreApi<T> {

    /** 数据流 */
    val dataFlow: Flow<T?>

    /**
     * 获取数据
     */
    suspend fun get(): T?

    /**
     * 已保存数据为null，才会调用[transform]更新数据
     */
    suspend fun initIfNull(transform: suspend () -> T): T?

    /**
     * 替换数据
     */
    suspend fun replace(transform: suspend (T?) -> T?): T?

    /**
     * 已保存数据不为null，才会调用[transform]更新数据
     */
    suspend fun update(transform: suspend (T) -> T): T?
}

internal fun <T> DatastoreApi(
    file: File,
    clazz: Class<T>,
    onError: (Throwable) -> Unit,
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
    private val onError: (Throwable) -> Unit,
) : DatastoreApi<T> {

    private val _datastore: DataStore<Model<T>> = MultiProcessDataStoreFactory.create(
        serializer = ModelSerializer(clazz),
        produceFile = { file }
    )

    override val dataFlow: Flow<T?>
        get() = _datastore.data
            .catch { notifyError(it) }
            .map { it.data }

    override suspend fun get(): T? {
        return dataFlow.firstOrNull()
    }

    override suspend fun initIfNull(transform: suspend () -> T): T? {
        return updateData { it ?: transform() }
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
            is java.io.IOException -> onError(error)
            else -> throw error
        }
    }
}