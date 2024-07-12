package com.sd.lib.datastore

class DatastoreJavaApi<T>(
    private val api: DatastoreApi<T>,
) {
    /**
     * [DatastoreApi.get]
     */
    fun get(): T? {
        return api.getBlocking()
    }

    /**
     * [DatastoreApi.initIfNull]
     */
    fun initIfNull(transform: () -> T): T? {
        return api.initIfNullBlocking {
            transform()
        }
    }

    /**
     * [DatastoreApi.replace]
     */
    fun replace(transform: (T?) -> T?): T? {
        return api.replaceBlocking {
            transform(it)
        }
    }

    /**
     * [DatastoreApi.update]
     */
    fun update(transform: (T) -> T): T? {
        return api.updateBlocking {
            transform(it)
        }
    }
}
