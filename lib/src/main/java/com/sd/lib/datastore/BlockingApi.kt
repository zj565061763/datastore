package com.sd.lib.datastore

import kotlinx.coroutines.runBlocking

/**
 * [DatastoreApi.get]
 */
fun <T> DatastoreApi<T>.getBlocking(): T? {
    return runBlocking {
        get()
    }
}

/**
 * [DatastoreApi.replace]
 */
fun <T> DatastoreApi<T>.replaceBlocking(transform: suspend (T?) -> T?) {
    runBlocking {
        replace(transform)
    }
}

/**
 * [DatastoreApi.update]
 */
fun <T> DatastoreApi<T>.updateBlocking(transform: suspend (T) -> T) {
    runBlocking {
        update(transform)
    }
}