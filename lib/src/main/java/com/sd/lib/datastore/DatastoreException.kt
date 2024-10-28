package com.sd.lib.datastore

open class DatastoreException internal constructor(
   message: String,
   cause: Throwable,
) : Exception(message, cause)

class DatastoreReadJsonException internal constructor(
   message: String,
   cause: Throwable,
) : DatastoreException(message, cause)