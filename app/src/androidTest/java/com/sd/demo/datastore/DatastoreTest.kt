package com.sd.demo.datastore

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.sd.lib.datastore.DatastoreApi
import com.sd.lib.datastore.DatastoreType
import com.sd.lib.datastore.FDatastore
import com.sd.lib.datastore.get
import com.sd.lib.datastore.update
import kotlinx.coroutines.runBlocking
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
   fun testGetDatastoreApi() = runBlocking {
      val store1 = getStore()
      val store2 = getStore()
      assertEquals(true, store1 === store2)
   }

   @Test
   fun testDatastore(): Unit = runBlocking {
      val store = getStore()
      testReplaceSuccess(store, 1)
      testReplaceSuccess(store, 2)
      testReplaceNull(store)
   }

   @Test
   fun testUpdate(): Unit = runBlocking {
      val store = getStore()

      run {
         var count = 0
         store.update {
            count++
            it.copy(age = 1)
         }
         assertEquals(null, store.get())
         assertEquals(0, count)
      }

      run {
         testReplaceSuccess(store, Int.MAX_VALUE)
         var count = 0
         store.update {
            count++
            it.copy(age = 2)
         }
         assertEquals(2, store.get()!!.age)
         assertEquals(1, count)
      }
   }

   @Test
   fun testDataFlow(): Unit = runBlocking {
      val store = getStore()

      store.flow.test {
         assertEquals(null, awaitItem())

         store.replace { TestModel(age = 1) }
         store.replace { TestModel(age = 1) }
         assertEquals(1, awaitItem()!!.age)

         store.update { it.copy(age = 2) }
         assertEquals(2, awaitItem()!!.age)
      }
   }

   @Test
   fun testNoneNullDataFlow(): Unit = runBlocking {
//      val store = getStore()
//      store.flowWithDefault { TestModel(age = 1) }.test {
//         assertEquals(1, awaitItem().age)
//         assertEquals(null, store.get())
//      }
   }

   @Test
   fun testNoneNullDataFlowSave(): Unit = runBlocking {
//      val store = getStore()
//      store.flowWithDefault(save = true) { TestModel(age = 1) }.test {
//         assertEquals(1, awaitItem().age)
//         assertEquals(1, store.get()!!.age)
//      }
   }
}

private suspend fun getStore(): DatastoreApi<TestModel> {
   return FDatastore.get(TestModel::class.java).also {
      testReplaceNull(it)
   }
}

private suspend fun testReplaceNull(store: DatastoreApi<TestModel>) {
   store.replace { null }
   assertEquals(null, store.get())
}

private suspend fun testReplaceSuccess(store: DatastoreApi<TestModel>, age: Int) {
   store.replace { TestModel(age = age) }
   assertEquals(age, store.get()!!.age)
   assertEquals(true, store.get()!! === store.get())
}