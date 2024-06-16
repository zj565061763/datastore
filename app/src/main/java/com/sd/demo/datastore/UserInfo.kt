package com.sd.demo.datastore

import com.sd.lib.datastore.DatastoreType
import com.sd.lib.datastore.FDatastore
import com.sd.lib.datastore.replaceBlocking

val userInfoDatastoreApi = FDatastore.defaultGroupApi(UserInfo::class.java)
    .apply {
        replaceBlocking {
            it ?: UserInfo(age = 0)
        }
    }

@DatastoreType
data class UserInfo(
    val age: Int,
)