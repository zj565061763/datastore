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
   fun testGetDatastoreApi() {
      val api1 = FDatastore.api(UserInfo::class.java)
      val api2 = FDatastore.api(UserInfo::class.java)
      assertEquals(true, api1 === api2)
   }

   @Test
   fun testGetSetRemove(): Unit = runBlocking {
      val api = FDatastore.api(UserInfo::class.java)
      testReplaceNull(api)
      testReplaceSuccess(api, 1)
      testReplaceSuccess(api, 2)
      testReplaceNull(api)
   }


   @Test
   fun testUpdate(): Unit = runBlocking {
      val api = FDatastore.api(UserInfo::class.java)
      testReplaceNull(api)

      api.update { it.copy(age = 1) }
      assertEquals(null, api.get())

      api.replace { UserInfo(Int.MAX_VALUE) }
      assertEquals(Int.MAX_VALUE, api.get()?.age)

      api.update { it.copy(age = 2) }
      assertEquals(2, api.get()?.age)
   }

   @Test
   fun testDataFlow(): Unit = runBlocking {
      val api = FDatastore.api(UserInfo::class.java)

      api.replace { null }
      assertEquals(null, api.get())

      api.dataFlow.test {
         assertEquals(null, awaitItem())

         api.replace { UserInfo(0) }
         assertEquals(0, awaitItem()?.age)

         api.update { it.copy(age = 1) }
         assertEquals(1, awaitItem()?.age)
      }
   }
}

private suspend fun testReplaceNull(api: DatastoreApi<UserInfo>) {
   api.replace { null }
   assertEquals(null, api.get())
}

private suspend fun testReplaceSuccess(api: DatastoreApi<UserInfo>, value: Int) {
   api.replace { UserInfo(value) }
   assertEquals(value, api.get()!!.age)
   assertEquals(true, api.get()!! === api.get())
}