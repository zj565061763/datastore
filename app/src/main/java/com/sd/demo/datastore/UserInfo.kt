package com.sd.demo.datastore

import com.sd.lib.datastore.DatastoreType
import com.sd.lib.datastore.FDatastore
import kotlinx.coroutines.flow.Flow

@DatastoreType("UserInfo")
data class UserInfo(
   val age: Int = 0,
   val name: String = "name",
)

val userInfoStore = FDatastore.api(UserInfo::class.java)

val userInfoFlow: Flow<UserInfo> = userInfoStore.dataFlow(save = true) { UserInfo() }