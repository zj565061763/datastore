package com.sd.demo.datastore

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.sd.lib.datastore.DatastoreApi
import com.sd.lib.datastore.DatastoreType
import com.sd.lib.datastore.FDatastore
import com.sd.lib.datastore.get
import com.sd.lib.datastore.update
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
      }.let { result ->
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
         testReplaceSuccess(1)
         testReplaceSuccess(2)
         testReplaceNull()
      }
   }

   @Test
   fun testUpdate() = runTest {
      val store = getStore()

      run {
         var count = 0
         store.update {
            count++
            it.copy(age = 1)
         }.also { result ->
            assertEquals(0, count)
            assertEquals(null, result)
            assertEquals(null, store.get())
         }
      }

      run {
         store.testReplaceSuccess(Int.MAX_VALUE)
         var count = 0
         store.update {
            count++
            it.copy(age = 2)
         }.also { result ->
            assertEquals(1, count)
            assertEquals(2, result!!.age)
            assertEquals(true, store.get() === result)
            assertEquals(true, store.get() === result)
         }
      }
   }

   @Test
   fun testFlow() = runTest {
      with(getStore()) {
         flow.test {
            assertEquals(null, awaitItem())

            replace { TestModel(age = 1) }
            replace { TestModel(age = 1) }
            assertEquals(1, awaitItem()!!.age)

            update { it.copy(age = 2) }
            assertEquals(2, awaitItem()!!.age)
         }
      }
   }

   @Test
   fun testCancelInTransform() = runTest {
      val store = getStore()
      runCatching {
         store.replace { throw CancellationException() }
      }.let { result ->
         assertEquals(true, result.exceptionOrNull()!! is CancellationException)
      }
   }

   @Test
   fun testExceptionInTransform() = runTest {
      val store = getStore()
      runCatching {
         store.replace { throw TestTransformException() }
      }.let { result ->
         assertEquals(true, result.exceptionOrNull() is TestTransformException)
      }
   }
}

private suspend fun getStore(): DatastoreApi<TestModel> {
   return FDatastore.get(TestModel::class.java).also { it.testReplaceNull() }
}

private suspend fun DatastoreApi<TestModel>.testReplaceNull() {
   replace { null }.also {
      assertEquals(null, it)
   }
   assertEquals(null, get())
}

private suspend fun DatastoreApi<TestModel>.testReplaceSuccess(age: Int) {
   val data = TestModel(age = age)
   replace { data }.also {
      assertEquals(true, it === data)
   }
   assertEquals(true, get() === data)
   assertEquals(true, get() === data)
}

private class TestTransformException : Throwable()