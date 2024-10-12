package com.sd.demo.datastore

import com.sd.lib.datastore.DatastoreType

data class TestModelNoAnnotation(
   val name: String,
)

@DatastoreType("")
data class TestModelEmptyId(
   val name: String,
)

@DatastoreType("SameId")
data class TestModelSameId1(
   val name: String,
)

@DatastoreType("SameId")
data class TestModelSameId2(
   val name: String,
)