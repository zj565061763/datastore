package com.sd.demo.datastore.model.version1

import com.sd.lib.datastore.DatastoreType

@DatastoreType("UserInfo")
data class UserInfo(
    val age: Int,
)