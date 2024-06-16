package com.sd.lib.datastore

import java.io.File

interface TypedDatastore<T> {
    /**
     * 获取默认文件对应的Api
     */
    fun api(): DatastoreApi<T>

    /**
     * 获取[filename]文件对应的api
     */
    fun api(filename: String): DatastoreApi<T>
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

    override fun api(filename: String): DatastoreApi<T> {
        require(filename.isNotEmpty()) { "filename is empty" }
        synchronized(this@TypedDatastoreImpl) {
            return _holder.getOrPut(filename) {
                DatastoreApi(
                    file = directory.resolve(fMd5(filename)),
                    clazz = clazz,
                    onError = onError,
                )
            }
        }
    }
}
