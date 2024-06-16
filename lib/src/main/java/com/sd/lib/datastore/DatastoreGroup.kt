package com.sd.lib.datastore

import java.io.File

interface DatastoreGroup {
    fun <T> type(clazz: Class<T>): TypedDatastore<T>
    fun removeType(id: String)
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

    private val _holder: MutableMap<String, TypedDatastore<*>> = mutableMapOf()

    override fun <T> type(clazz: Class<T>): TypedDatastore<T> {
        val datastoreType = clazz.getAnnotation(DatastoreType::class.java)
            ?: error("Annotation DatastoreType was not found in $clazz")

        val id = datastoreType.id.ifEmpty { clazz.name }

        synchronized(this@DatastoreGroupImpl) {
            @Suppress("UNCHECKED_CAST")
            return _holder.getOrPut(id) {
                TypedDatastore(
                    directory = directoryOfID(id),
                    clazz = clazz,
                    onError = onError,
                )
            } as TypedDatastore<T>
        }
    }

    override fun removeType(id: String) {
        synchronized(this@DatastoreGroupImpl) {
            if (_holder.containsKey(id)) error("Can not remove active type:$id")
            directoryOfID(id).deleteRecursively()
        }
    }

    private fun directoryOfID(id: String): File = directory.resolve(fMd5(id))
}