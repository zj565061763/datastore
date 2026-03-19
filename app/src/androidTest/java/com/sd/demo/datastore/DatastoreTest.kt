package com.sd.demo.datastore

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.sd.lib.datastore.DatastoreApi
import com.sd.lib.datastore.DatastoreType
import com.sd.lib.datastore.FDatastore
import com.sd.lib.datastore.get
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatastoreTest {
  @Test
  fun testNoAnnotation() {
    val result = runCatching {
      FDatastore.get(TestModelNoAnnotation::class.java)
    }
    assertEquals(
      "Annotation ${DatastoreType::class.java.simpleName} was not found in ${TestModelNoAnnotation::class.java.name}",
      result.exceptionOrNull()!!.message
    )
  }

  @Test
  fun testEmptyId() {
    val result = runCatching {
      FDatastore.get(TestModelEmptyId::class.java)
    }
    assertEquals(
      "DatastoreType.id is empty in ${TestModelEmptyId::class.java.name}",
      result.exceptionOrNull()!!.message
    )
  }

  @Test
  fun testSameId() {
    FDatastore.get(TestModelSameId1::class.java)
    runCatching {
      FDatastore.get(TestModelSameId2::class.java)
    }.also { result ->
      assertEquals(
        "id:SameId has bound to ${TestModelSameId1::class.java.name} when bind ${TestModelSameId2::class.java.name}",
        result.exceptionOrNull()!!.message
      )
    }
  }

  @Test
  fun testGetDatastoreApi() = runTest {
    val store1 = getStore()
    val store2 = getStore()
    assertEquals(true, store1 === store2)
  }

  @Test
  fun testDatastore() = runTest {
    with(getStore()) {
      testUpdateSuccess(1)
      testUpdateSuccess(2)
      testUpdateNull()
    }
  }

  @Test
  fun testFlow() = runTest {
    with(getStore()) {
      flow.test {
        assertEquals(null, awaitItem())

        update { TestModel(age = 1) }
        update { TestModel(age = 1) }
        assertEquals(1, awaitItem()!!.age)

        update { it?.copy(age = 2) }
        assertEquals(2, awaitItem()!!.age)
      }
    }
  }

  @Test
  fun testCancelInTransform() = runTest {
    val store = getStore()
    runCatching {
      store.update { throw CancellationException("update cancel") }
    }.also { result ->
      assertEquals(true, result.exceptionOrNull()!! is CancellationException)
      assertEquals("update cancel", result.exceptionOrNull()!!.message)
    }
  }

  @Test
  fun testExceptionInTransform() = runTest {
    val store = getStore()
    runCatching {
      store.update { throw TestTransformException("update error") }
    }.also { result ->
      assertEquals(true, result.exceptionOrNull() is TestTransformException)
      assertEquals("update error", result.exceptionOrNull()!!.message)
    }
  }
}

private suspend fun getStore(): DatastoreApi<TestModel> {
  return FDatastore.get(TestModel::class.java).also { it.testUpdateNull() }
}

private suspend fun DatastoreApi<TestModel>.testUpdateNull() {
  update { null }
  assertEquals(null, get())
}

private suspend fun DatastoreApi<TestModel>.testUpdateSuccess(age: Int) {
  val data = TestModel(age = age)
  update { data }
  assertEquals(true, get() === data)
  assertEquals(true, get() === data)
}

private class TestTransformException(override val message: String) : Throwable()