package com.sd.lib.datastore

import java.io.File

interface DatastoreGroup {
    fun <T> type(clazz: Class<T>): DatastoreApi<T>
}

internal fun DatastoreGroup(
    directory: File,
    onError: (Throwable) -> Unit,
): DatastoreGroup {
    return DatastoreGroupImpl(
        directory = directory,
        onError = onError,
    )
}

private class DatastoreGroupImpl(
    private val directory: File,
    private val onError: (Throwable) -> Unit,
) : DatastoreGroup {

    private val _holder: MutableMap<String, ApiInfo<*>> = mutableMapOf()

    override fun <T> type(clazz: Class<T>): DatastoreApi<T> {
        val datastoreType = clazz.getAnnotation(DatastoreType::class.java)
            ?: error("Annotation DatastoreType was not found in $clazz")

        val id = datastoreType.id.ifEmpty { clazz.name }

        synchronized(this@DatastoreGroupImpl) {
            _holder[id]?.let { info ->
                if (info.clazz != clazz) error("id:${id} has bound to ${info.clazz}")
                @Suppress("UNCHECKED_CAST")
                return info.api as DatastoreApi<T>
            }
            return newDatastoreApi(
                file = directoryOfID(id).resolve("default"),
                clazz = clazz,
            ).also { api ->
                _holder[id] = ApiInfo(api, clazz)
            }
        }
    }

    private fun <T> newDatastoreApi(
        file: File,
        clazz: Class<T>,
    ): DatastoreApi<T> {
        return DatastoreApi(
            file = file,
            clazz = clazz,
            onError = onError,
        )
    }

    private fun directoryOfID(id: String): File {
        require(id.isNotEmpty()) { "id is empty" }
        return directory.resolve(fMd5(id))
    }

    private data class ApiInfo<T>(
        val api: DatastoreApi<T>,
        val clazz: Class<T>,
    )
}