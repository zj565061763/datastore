package com.sd.demo.datastore.model

import com.sd.lib.datastore.DatastoreType

@DatastoreType("UserInfo")
data class UserInfo(
    val age: Int,
)