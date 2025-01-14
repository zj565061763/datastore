package com.sd.demo.datastore

import com.sd.lib.datastore.DatastoreType
import com.sd.lib.datastore.FDatastore
import com.sd.lib.datastore.withDefault
import kotlinx.coroutines.flow.Flow

@DatastoreType("UserInfo")
data class UserInfo(
  val age: Int = 0,
  val name: String = "name",
) {
  companion object {
    private val _store = FDatastore.get(UserInfo::class.java).withDefault {
      UserInfo().also { logMsg { "create default user" } }
    }

    val flow: Flow<UserInfo>
      get() = _store.flow

    suspend fun increment() {
      _store.update {
        it.copy(age = it.age + 1)
      }
    }

    suspend fun decrement() {
      _store.update {
        it.copy(age = it.age - 1)
      }
    }
  }
}