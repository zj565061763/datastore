package com.sd.lib.datastore

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class DatastoreType(
  val id: String,
)