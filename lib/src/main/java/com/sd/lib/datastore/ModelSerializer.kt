package com.sd.lib.datastore

import androidx.datastore.core.Serializer
import com.sd.lib.moshi.fMoshi
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Types
import java.io.InputStream
import java.io.OutputStream

internal class ModelSerializer<T>(
   private val clazz: Class<T>,
   private val onError: (DatastoreException) -> Unit,
) : Serializer<Model<T>> {

   private val _jsonAdapter: JsonAdapter<Model<T>> = fMoshi.adapter(
      Types.newParameterizedType(Model::class.java, clazz)
   )

   override val defaultValue: Model<T> = Model()

   @Suppress("BlockingMethodInNonBlockingContext")
   override suspend fun writeTo(t: Model<T>, output: OutputStream) {
      val json = _jsonAdapter.toJson(t)
      output.write(json.toByteArray())
   }

   override suspend fun readFrom(input: InputStream): Model<T> {
      val bytes = input.readBytes()
      if (bytes.isEmpty()) return defaultValue
      return runCatching {
         val json = String(bytes)
         _jsonAdapter.fromJson(json)!!
      }.getOrElse { e ->
         when (e) {
            is JsonDataException -> onError(
               DatastoreReadJsonException(
                  message = "Error read json for ${clazz.name}",
                  cause = e,
               )
            )
            is java.io.IOException -> onError(
               DatastoreReadJsonException(
                  message = "Error read json for ${clazz.name}",
                  cause = e,
               )
            )
            else -> throw e
         }
         defaultValue
      }
   }
}

internal data class Model<T>(
   val data: T? = null,
)