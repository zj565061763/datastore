package com.sd.demo.datastore

import app.cash.turbine.test
import com.sd.lib.datastore.DatastoreApiWithDefault
import com.sd.lib.datastore.FDatastore
import com.sd.lib.datastore.get
import com.sd.lib.datastore.withDefault
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DatastoreWithDefaultTest {
  @Test
  fun testGet() = runTest {
    val store = getStore()
    assertEquals(TestModel(), store.get())
  }

  @Test
  fun testUpdate() = runTest {
    val store = getStore()
    val model = TestModel(age = 1)
    store.update { model }
    assertEquals(model, store.get())
  }

  @Test
  fun testFlow() = runTest {
    with(getStore()) {
      flow.test {
        assertEquals(TestModel(), awaitItem())

        update { TestModel(age = 1) }
        update { TestModel(age = 1) }
        assertEquals(1, awaitItem().age)

        update { it.copy(age = 2) }
        assertEquals(2, awaitItem().age)
      }
    }
  }
}

private suspend fun getStore(): DatastoreApiWithDefault<TestModel> {
  return FDatastore.get(TestModel::class.java)
    .also { it.update { null } }
    .withDefault { TestModel() }
}