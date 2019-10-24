package com.mobile.shannon.shannonasynctask

import androidx.test.annotation.UiThreadTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.Assert.*
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CountDownLatch

/**
 * Created by wmadao11 on 2019-10-17.
 */

@RunWith(AndroidJUnit4::class)
class HandlerTest {

    private val latch = CountDownLatch(1)

    @Test(timeout = 5000)
    @UiThreadTest
    fun main_to_bg_test() {
        ShannonTask {
            threadName() shouldBe "main"
            Thread.sleep(1000)
        }.onResponse {
            threadName() shouldStartWith "shannon"
            latch.countDown()
        }.onError {
            it?.printStackTrace()
            Assert.fail(it?.message)
            latch.countDown()
        }
            .onUI()
            .responseOnBG()
            .run()
        latch.await()
    }

    @Test(timeout = 5000)
    @UiThreadTest
    fun main_to_main_test() {
        ShannonTask {
            threadName() shouldBe "main"
            assertTrue(threadName().startsWith("main"))
            Thread.sleep(1000)
        }.onResponse {
            assertTrue(threadName().startsWith("main"))
            latch.countDown()
        }.onError {
            it?.printStackTrace()
            Assert.fail(it?.message)
            latch.countDown()
        }
            .onUI()
            .responseOnUI()
            .run()
        latch.await()
    }

    @Test(timeout = 5000)
    fun bg_to_main_test() {
        // 这里有个奇怪的点
        // 加上@UiThreadTest，就一直回不到onResponse/onError
        ShannonTask {
            threadName() shouldStartWith "shannon"
            Thread.sleep(1000)
        }.onResponse {
            threadName() shouldBe "main"
            latch.countDown()
        }.onError {
            it?.printStackTrace()
            Assert.fail(it?.message)
            latch.countDown()
        }
            .onBG()
            .responseOnUI()
            .run()
        latch.await()
    }

    @Test(timeout = 5000)
    fun bg_to_bg_test() {
        ShannonTask {
            threadName() shouldStartWith "shannon"
            Thread.sleep(1000)
        }.onResponse {
            threadName() shouldStartWith  "shannon"
            latch.countDown()
        }.onError {
            it?.printStackTrace()
            Assert.fail(it?.message)
            latch.countDown()
        }
            .onBG()
            .responseOnBG()
            .run()
        latch.await()
    }

    @Test(timeout = 5000)
    fun error_test() {
        ShannonTask {
            // throw a error
            @Suppress("DIVISION_BY_ZERO")
            val a = 123 / 0
            log(a)
        }.onResponse {
            log("onResponse")
            // if came here it's a fail
            Assert.fail("Should be in a onError")
            latch.countDown()
        }.onError {
            it?.printStackTrace()
            log(it?.message)
            assertNotNull(it)
            latch.countDown()
        }
            .onBG()
            .responseOnUI()
            .run()
        latch.await()
    }

}

val dateFormat = SimpleDateFormat("HH:mm:ss:SSS")
val now = {
    dateFormat.format(Date(System.currentTimeMillis()))
}
fun threadName(): String = Thread.currentThread().name
fun log(msg: Any?) = println("${now()} [${threadName()}] $msg")
infix fun <T> T?.shouldBe(expected: T?) = assertEquals(expected, this)

infix fun String.shouldStartWith(prefix: String) = assertTrue(startsWith(prefix))
