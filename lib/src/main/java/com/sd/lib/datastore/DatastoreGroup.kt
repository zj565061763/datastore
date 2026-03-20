package com.sd.lib.datastore

import java.io.File
import java.security.MessageDigest

internal class DatastoreGroup(
  private val directory: File,
) {
  private val _holder: MutableMap<String, ApiInfo<*>> = mutableMapOf()

  fun <T> get(clazz: Class<T>): DatastoreApi<T> {
    val datastoreType = requireNotNull(clazz.getAnnotation(DatastoreType::class.java)) {
      "Annotation ${DatastoreType::class.java.simpleName} was not found in ${clazz.name}"
    }

    val id = datastoreType.id.ifEmpty {
      throw IllegalArgumentException("DatastoreType.id is empty in ${clazz.name}")
    }

    _holder[id]?.also { info ->
      if (info.clazz == clazz) {
        @Suppress("UNCHECKED_CAST")
        return info.api as DatastoreApi<T>
      } else {
        throw IllegalArgumentException("id:${id} has bound to ${info.clazz.name} when bind ${clazz.name}")
      }
    }

    return DatastoreApi(
      file = directoryOf(id).resolve("default"),
      clazz = clazz,
    ).also { api ->
      _holder[id] = ApiInfo(clazz, api)
    }
  }

  private fun directoryOf(id: String): File {
    val dir = runCatching { md5(id) }.getOrElse { id }
    return directory.resolve(dir)
  }

  private data class ApiInfo<T>(
    val clazz: Class<T>,
    val api: DatastoreApi<T>,
  )
}

private fun md5(input: String): String {
  val md5Bytes = MessageDigest.getInstance("MD5").digest(input.toByteArray())
  return buildString {
    for (byte in md5Bytes) {
      val hex = (0xff and byte.toInt()).toString(16)
      if (hex.length == 1) append("0")
      append(hex)
    }
  }
}