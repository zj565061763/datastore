package com.sd.demo.datastore

import com.sd.lib.datastore.DatastoreType
import com.sd.lib.datastore.FDatastore
import com.sd.lib.datastore.flowWithDefault
import com.sd.lib.datastore.update

@DatastoreType("UserInfo")
data class UserInfo(
   val age: Int = 0,
   val name: String = "name",
) {
   companion object {
      private val store = FDatastore.api(UserInfo::class.java)

      val flow = store.flowWithDefault(save = true) { UserInfo() }

      suspend fun increment() = store.update {
         it.copy(age = it.age + 1)
      }

      suspend fun decrement() = store.update {
         it.copy(age = it.age - 1)
      }
   }
}