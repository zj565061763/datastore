package com.sd.demo.datastore

import com.sd.lib.datastore.DatastoreWithDefaultApi
import com.sd.lib.datastore.FDatastore
import com.sd.lib.datastore.get
import com.sd.lib.datastore.withDefault
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class DatastoreWithDefaultTest {
   @Test
   fun testGet(): Unit = runBlocking {
      assertEquals(TestModel(), getStore().get())
   }

   @Test
   fun testUpdate(): Unit = runBlocking {
      val store = getStore()
      assertEquals(TestModel(), store.update { it })
   }
}

private suspend fun getStore(): DatastoreWithDefaultApi<TestModel> {
   return FDatastore.get(TestModel::class.java)
      .also { it.replace { null } }
      .withDefault { TestModel() }
}