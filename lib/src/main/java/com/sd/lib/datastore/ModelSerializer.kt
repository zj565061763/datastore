package com.sd.lib.datastore

import androidx.datastore.core.Serializer
import com.sd.lib.moshi.fMoshi
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import java.io.InputStream
import java.io.OutputStream

internal class ModelSerializer<T>(
    clazz: Class<T>,
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
        val json = bytes.decodeToString()
        return checkNotNull(_jsonAdapter.fromJson(json))
    }
}

internal data class Model<T>(
    val data: T? = null,
)