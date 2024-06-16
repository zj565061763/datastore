package com.sd.demo.datastore

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.sd.demo.datastore.model.version1.UserInfo
import com.sd.lib.datastore.FDatastore
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatastoreTest {
    @Test
    fun testGetDatastoreApi() {
        val api1 = FDatastore.api(UserInfo::class.java)
        val api2 = FDatastore.api(UserInfo::class.java)
        assertEquals(true, api1 === api2)
    }

    @Test
    fun testGetSetRemove(): Unit = runBlocking {
        val api = FDatastore.api(UserInfo::class.java)

        api.replace { null }
        assertEquals(null, api.get())

        api.replace { UserInfo(Int.MAX_VALUE) }
        assertEquals(Int.MAX_VALUE, api.get()?.age)
        assertEquals(true, api.get() === api.get())

        api.replace { UserInfo(Int.MIN_VALUE) }
        assertEquals(Int.MIN_VALUE, api.get()?.age)
        assertEquals(true, api.get() === api.get())

        api.replace { null }
        assertEquals(null, api.get())
    }

    @Test
    fun testUpdate(): Unit = runBlocking {
        val api = FDatastore.api(UserInfo::class.java)

        api.replace { null }
        assertEquals(null, api.get())

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