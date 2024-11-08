package com.sd.demo.datastore

import com.sd.lib.datastore.DatastoreType
import com.sd.lib.datastore.FDatastore
import com.sd.lib.datastore.update
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@DatastoreType("UserInfo")
data class UserInfo(
   val age: Int = 0,
   val name: String = "name",
) {
   companion object {
      private val _store = FDatastore.get(UserInfo::class.java)

      val flow = _store.flow

      suspend fun increment() = _store.update {
         it.copy(age = it.age + 1)
      }

      suspend fun decrement() = _store.update {
         it.copy(age = it.age - 1)
      }

      init {
         GlobalScope.launch {
            _store.replace { it ?: UserInfo() }
         }
      }
   }
}