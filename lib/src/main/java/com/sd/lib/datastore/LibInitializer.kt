package com.sd.lib.datastore

import android.content.Context
import androidx.startup.Initializer

internal class LibInitializer : Initializer<Context> {
  override fun create(context: Context): Context {
    FDatastore.init(context)
    return context
  }

  override fun dependencies(): List<Class<out Initializer<*>>> {
    return emptyList()
  }
}