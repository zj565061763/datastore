package com.sd.lib.datastore

import java.io.File

interface TypedDatastore<T> {
    /**
     * 获取默认key对应的api，默认key为全类名
     */
    fun api(): DatastoreApi<T>

    /**
     * 获取[key]对应的api
     */
    fun api(key: String): DatastoreApi<T>
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

    override fun api(key: String): DatastoreApi<T> {
        require(key.isNotEmpty()) { "key is empty" }
        synchronized(this@TypedDatastoreImpl) {
            return _holder.getOrPut(key) {
                DatastoreApi(
                    file = directory.resolve(fMd5(key)),
                    clazz = clazz,
                    onError = onError,
                )
            }
        }
    }
}
