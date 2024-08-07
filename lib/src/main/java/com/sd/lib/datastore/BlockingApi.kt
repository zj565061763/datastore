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
 * [DatastoreApi.initIfNull]
 */
fun <T> DatastoreApi<T>.initIfNullBlocking(transform: suspend () -> T): T? {
    return runBlocking {
        initIfNull(transform)
    }
}

/**
 * [DatastoreApi.replace]
 */
fun <T> DatastoreApi<T>.replaceBlocking(transform: suspend (T?) -> T?): T? {
    return runBlocking {
        replace(transform)
    }
}

/**
 * [DatastoreApi.update]
 */
fun <T> DatastoreApi<T>.updateBlocking(transform: suspend (T) -> T): T? {
    return runBlocking {
        update(transform)
    }
}