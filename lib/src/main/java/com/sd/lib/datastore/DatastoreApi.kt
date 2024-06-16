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
     * 替换数据
     */
    suspend fun replace(transform: suspend (T?) -> T?)

    /**
     * 更新数据
     */
    suspend fun update(transform: suspend (T) -> T)
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

    override val dataFlow: Flow<T?> = _datastore.data
        .catch { notifyError(it) }
        .map { it.data }

    override suspend fun get(): T? {
        return dataFlow.firstOrNull()
    }

    override suspend fun replace(transform: suspend (T?) -> T?) {
        updateData(transform)
    }

    override suspend fun update(transform: suspend (T) -> T) {
        updateData { data ->
            if (data == null) {
                null
            } else {
                transform(data)
            }
        }
    }

    private suspend fun updateData(transform: suspend (T?) -> T?) {
        updateModel { model ->
            val newData = transform(model.data)
            model.copy(data = newData)
        }
    }

    private suspend fun updateModel(transform: suspend (Model<T>) -> Model<T>) {
        try {
            _datastore.updateData(transform)
        } catch (e: Throwable) {
            notifyError(e)
        }
    }

    private fun notifyError(error: Throwable) {
        when (error) {
            is java.io.IOException -> onError(error)
            else -> throw error
        }
    }

    /*private fun <T> checkLegacyIds(
        clazz: Class<T>,
        type: DatastoreType,
        id: String,
    ) {
        val legacyIds = type.legacyIds
        if (legacyIds.isEmpty()) return

        val ids = legacyIds.asSequence()
            .map {
                it.also {
                    if (it.isEmpty()) error("legacy id is empty")
                    if (it == id) error("legacy id equals current id:$id")
                }
            }
            .filter { !_holder.containsKey(it) }
            .distinct()
            .toMutableList()

        if (ids.isEmpty()) {
            return
        }

        val currentDirectory = directoryOfID(id)
        if (!currentDirectory.exists()) {
            // 当前id关联的目录已存在
            return
        }

        if (!currentDirectory.mkdirs()) {
            // 创建目录失败
            return
        }

        val lastTypedDatastore = newTypedDatastore(
            directory = directoryOfID(ids.last()),
            clazz = clazz,
        )

        ids.forEach {
            directoryOfID(it).deleteRecursively()
        }
    }*/
}