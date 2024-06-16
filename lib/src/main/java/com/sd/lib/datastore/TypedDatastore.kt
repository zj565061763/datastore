package com.sd.lib.datastore

import android.util.Base64
import java.io.File

interface TypedDatastore<T> {
    /**
     * 获取默认文件对应的Api
     */
    fun api(): DatastoreApi<T>

    /**
     * 获取[filename]文件对应的Api
     */
    fun api(filename: String): DatastoreApi<T>

    /**
     * 获取所有文件对应的Api
     */
    fun apis(): List<DatastoreApi<T>>
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
    private var _hasLoadApis = false

    override fun api(): DatastoreApi<T> {
        return api("default")
    }

    override fun api(filename: String): DatastoreApi<T> {
        require(filename.isNotEmpty()) { "filename is empty" }
        synchronized(this@TypedDatastoreImpl) {
            return _holder.getOrPut(filename) {
                DatastoreApi(
                    file = directory.resolve(filename.encodeBase64()),
                    clazz = clazz,
                    onError = onError,
                )
            }
        }
    }

    override fun apis(): List<DatastoreApi<T>> {
        synchronized(this@TypedDatastoreImpl) {
            if (_hasLoadApis) {
                return _holder.values.toList()
            }

            val list = directory.list()
            if (!list.isNullOrEmpty()) {
                list.asSequence()
                    .filter { !it.contains(".") }
                    .map { runCatching { it.decodeBase64() }.getOrNull() }
                    .filterNotNull()
                    .forEach { api(it) }
            }

            _hasLoadApis = true
            return _holder.values.toList()
        }
    }
}

private fun String.encodeBase64(): String {
    val input = this.toByteArray()
    val flag = Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
    return Base64.encode(input, flag).decodeToString()
}

@Throws(IllegalArgumentException::class)
private fun String.decodeBase64(): String {
    val input = this.toByteArray()
    val flag = Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
    return Base64.decode(input, flag).decodeToString()
}