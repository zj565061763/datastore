package com.sd.demo.datastore

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sd.lib.datastore.FDatastore
import com.sd.lib.datastore.getBlocking
import com.sd.lib.datastore.replaceBlocking
import com.sd.lib.datastore.updateBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatastoreBlockingTest {
    @Test
    fun testGetSetRemove() {
        val api = FDatastore.defaultGroupApi(UserInfo::class.java)

        api.replaceBlocking { null }
        assertEquals(null, api.getBlocking())

        api.replaceBlocking { UserInfo(Int.MAX_VALUE) }
        assertEquals(Int.MAX_VALUE, api.getBlocking()?.age)
        assertEquals(true, api.getBlocking() === api.getBlocking())

        api.replaceBlocking { UserInfo(Int.MIN_VALUE) }
        assertEquals(Int.MIN_VALUE, api.getBlocking()?.age)
        assertEquals(true, api.getBlocking() === api.getBlocking())

        api.replaceBlocking { null }
        assertEquals(null, api.getBlocking())
    }

    @Test
    fun testUpdate() {
        val api = FDatastore.defaultGroupApi(UserInfo::class.java)

        api.replaceBlocking { null }
        assertEquals(null, api.getBlocking())

        api.updateBlocking { it.copy(age = 1) }
        assertEquals(null, api.getBlocking())

        api.replaceBlocking { UserInfo(Int.MAX_VALUE) }
        assertEquals(Int.MAX_VALUE, api.getBlocking()?.age)

        api.updateBlocking { it.copy(age = 2) }
        assertEquals(2, api.getBlocking()?.age)
    }
}