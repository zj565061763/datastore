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
     * [DatastoreApi.replace]
     */
    fun replace(transform: (T?) -> T?) {
        api.replaceBlocking {
            transform(it)
        }
    }

    /**
     * [DatastoreApi.update]
     */
    fun update(transform: (T) -> T) {
        api.updateBlocking {
            transform(it)
        }
    }
}
