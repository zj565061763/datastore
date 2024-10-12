package com.sd.demo.datastore

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.sd.lib.datastore.DatastoreApi
import com.sd.lib.datastore.DatastoreType
import com.sd.lib.datastore.FDatastore
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatastoreTest {
   @Test
   fun testNoAnnotation() {
      val result = runCatching {
         FDatastore.api(TestModelNoAnnotation::class.java)
      }
      assertEquals(
         "Annotation ${DatastoreType::class.java.simpleName} was not found in ${TestModelNoAnnotation::class.java.name}",
         result.exceptionOrNull()!!.message
      )
   }

   @Test
   fun testEmptyId() {
      val result = runCatching {
         FDatastore.api(TestModelEmptyId::class.java)
      }
      assertEquals(
         "DatastoreType.id is empty in ${TestModelEmptyId::class.java.name}",
         result.exceptionOrNull()!!.message
      )
   }

   @Test
   fun testSameId() {
      FDatastore.api(TestModelSameId1::class.java)
      val result = runCatching {
         FDatastore.api(TestModelSameId2::class.java)
      }
      assertEquals(
         "id:SameId has bound to ${TestModelSameId1::class.java.name} when bind ${TestModelSameId2::class.java.name}",
         result.exceptionOrNull()!!.message
      )
   }

   @Test
   fun testGetDatastoreApi() = runBlocking {
      val store1 = getUserInfoStore()
      val store2 = getUserInfoStore()
      assertEquals(true, store1 === store2)
   }

   @Test
   fun testDatastore(): Unit = runBlocking {
      val store = getUserInfoStore()
      testReplaceSuccess(store, 1)
      testReplaceSuccess(store, 2)
      testReplaceNull(store)
   }

   @Test
   fun testUpdate(): Unit = runBlocking {
      val store = getUserInfoStore()

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
      val store = getUserInfoStore()

      store.dataFlow.test {
         assertEquals(null, awaitItem())

         store.replace(UserInfo(age = 1))
         store.replace(UserInfo(age = 1))
         assertEquals(1, awaitItem()!!.age)

         store.update { it.copy(age = 2) }
         assertEquals(2, awaitItem()!!.age)
      }
   }

   @Test
   fun testNoneNullDataFlow(): Unit = runBlocking {
      val store = getUserInfoStore()
      store.dataFlow { UserInfo(age = 1) }.test {
         assertEquals(1, awaitItem().age)
         assertEquals(null, store.get())
      }
   }

   @Test
   fun testNoneNullDataFlowSave(): Unit = runBlocking {
      val store = getUserInfoStore()
      store.dataFlow(save = true) { UserInfo(age = 1) }.test {
         // 第一次是默认值
         assertEquals(1, awaitItem().age)
         // 第二次是默认值保存成功后触发的
         assertEquals(1, awaitItem().age)
         assertEquals(1, store.get()!!.age)
      }
   }
}

private suspend fun getUserInfoStore(): DatastoreApi<UserInfo> {
   return FDatastore.api(UserInfo::class.java).also {
      testReplaceNull(it)
   }
}

private suspend fun testReplaceNull(store: DatastoreApi<UserInfo>) {
   store.replace(null)
   assertEquals(null, store.get())
}

private suspend fun testReplaceSuccess(store: DatastoreApi<UserInfo>, age: Int) {
   store.replace(UserInfo(age = age))
   assertEquals(age, store.get()!!.age)
   assertEquals(true, store.get()!! === store.get())
}