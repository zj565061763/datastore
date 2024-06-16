package com.sd.lib.datastore

import java.io.File

interface TypedDatastore<T> {
    /**
     * 获取默认文件对应的Api
     */
    fun api(): DatastoreApi<T>

    /**
     * 获取[file]文件对应的api
     */
    fun api(file: String): DatastoreApi<T>
}

internal fun <T> TypedDatastore(
    directory: File,
    clazz: Class<T>,
    onError: (Throwable) -> Unit,
): TypedDatastore<T> {
    return TypedDatastoreImpl(
        directory = directory,
        clazz = clazz,
        onError = onError,
    )
}

private class TypedDatastoreImpl<T>(
    private val directory: File,
    private val clazz: Class<T>,
    private val onError: (Throwable) -> Unit,
) : TypedDatastore<T> {

    private val _holder: MutableMap<String, DatastoreApi<T>> = mutableMapOf()

    override fun api(): DatastoreApi<T> {
        return api(clazz.name)
    }

    override fun api(file: String): DatastoreApi<T> {
        require(file.isNotEmpty()) { "file is empty" }
        synchronized(this@TypedDatastoreImpl) {
            return _holder.getOrPut(file) {
                DatastoreApi(
                    file = directory.resolve(fMd5(file)),
                    clazz = clazz,
                    onError = onError,
                )
            }
        }
    }
}
