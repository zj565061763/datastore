package com.sd.lib.datastore

open class DatastoreException internal constructor(
  message: String,
  cause: Throwable,
) : Exception(message, cause)

class DatastoreWriteDataException internal constructor(
  message: String,
  cause: Throwable,
) : DatastoreException(message, cause)

class DatastoreReadDataException internal constructor(
  message: String,
  cause: Throwable,
) : DatastoreException(message, cause)