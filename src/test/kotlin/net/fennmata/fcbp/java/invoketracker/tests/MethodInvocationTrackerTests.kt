package net.fennmata.fcbp.java.invoketracker.tests

import net.fennmata.fcbp.java.invoketracker.MethodInvocationTracker
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch

class MethodInvocationTrackerTests {

    @BeforeEach
    fun `clear MethodInvocationTracker's inner storage`() {
        MethodInvocationTracker.clear()
    }

    @Test
    fun `check isMethodInvoked with empty storage`() {
        val actual = MethodInvocationTracker.isMethodInvoked(TEST_METHOD_NAME)
        Assertions.assertEquals(false, actual) {
            "MethodInvocation.isMethodInvoked returned $actual for a method with no invocations"
        }
    }

    @Test
    fun `check countMethodCallStackFrames with empty storage`() {
        val actual = MethodInvocationTracker.countMethodCallStackFrames(TEST_METHOD_NAME)
        Assertions.assertEquals(0, actual) {
            "MethodInvocationTracker.countMethodCallStackFrames returned $actual for a method with no invocations"
        }
    }

    @Test
    fun `check isMethodInvoked with non-empty storage`() {
        MethodInvocationTracker.onMethodEntry(TEST_METHOD_NAME)
        val actual = MethodInvocationTracker.isMethodInvoked(TEST_METHOD_NAME)
        Assertions.assertEquals(true, actual) {
            "MethodInvocation.isMethodInvoked returned $actual for a method with an invocation"
        }
    }

    @Test
    fun `check countMethodCallStackFrames with non-empty storage`() {
        MethodInvocationTracker.onMethodEntry(TEST_METHOD_NAME)
        val actual = MethodInvocationTracker.countMethodCallStackFrames(TEST_METHOD_NAME)
        Assertions.assertEquals(1, actual) {
            "MethodInvocation.countMethodCallStackFrames returned $actual for a method with an invocation"
        }
    }

    @Test
    fun `check isMethodInvoked for a single frame lifecycle`() {
        MethodInvocationTracker.onMethodEntry(TEST_METHOD_NAME)
        MethodInvocationTracker.onMethodExit(TEST_METHOD_NAME)
        `check isMethodInvoked with empty storage`()
    }

    @Test
    fun `check countMethodCallStackFrames for a single frame lifecycle`() {
        MethodInvocationTracker.onMethodEntry(TEST_METHOD_NAME)
        MethodInvocationTracker.onMethodExit(TEST_METHOD_NAME)
        `check countMethodCallStackFrames with empty storage`()
    }

    private enum class ThreadAction { ON_ENTRY, ON_EXIT, IS_INVOKED, COUNT_FRAMES }

    private fun runSimultaneously(vararg actions: ThreadAction) {
        fun run(action: ThreadAction) {
            when (action) {
                ThreadAction.ON_ENTRY -> MethodInvocationTracker.onMethodEntry(TEST_METHOD_NAME)
                ThreadAction.ON_EXIT -> MethodInvocationTracker.onMethodExit(TEST_METHOD_NAME)
                ThreadAction.IS_INVOKED -> MethodInvocationTracker.isMethodInvoked(TEST_METHOD_NAME)
                ThreadAction.COUNT_FRAMES -> MethodInvocationTracker.countMethodCallStackFrames(TEST_METHOD_NAME)
            }
        }

        val countDownLatch = CountDownLatch(1)
        val threads = actions.map { action ->
            Thread {
                countDownLatch.await()
                run(action)
            }.apply {
                start()
            }
        }
        Thread.sleep(5)
        countDownLatch.countDown()
        threads.forEach(Thread::join)
    }

    @RepeatedTest(100)
    fun `check synchronization on simultaneous method entries`() {
        runSimultaneously(ThreadAction.ON_ENTRY, ThreadAction.ON_ENTRY)
        val actual = MethodInvocationTracker.countMethodCallStackFrames(TEST_METHOD_NAME)
        Assertions.assertEquals(2, actual) {
            "MethodInvocation.countMethodCallStackFrames returned $actual for a method with two invocations"
        }
    }

    @RepeatedTest(100)
    fun `check synchronization on simultaneous method exits`() {
        repeat(2) { MethodInvocationTracker.onMethodEntry(TEST_METHOD_NAME) }
        runSimultaneously(ThreadAction.ON_EXIT, ThreadAction.ON_EXIT)
        val actual = MethodInvocationTracker.countMethodCallStackFrames(TEST_METHOD_NAME)
        Assertions.assertEquals(0, actual) {
            "MethodInvocation.countMethodCallStackFrames returned $actual for a method with no invocations"
        }
    }

    @RepeatedTest(100)
    fun `check synchronization on simultaneous method entry and exit`() {
        MethodInvocationTracker.onMethodEntry(TEST_METHOD_NAME)
        runSimultaneously(ThreadAction.ON_EXIT, ThreadAction.ON_ENTRY)
        val actual = MethodInvocationTracker.countMethodCallStackFrames(TEST_METHOD_NAME)
        Assertions.assertEquals(1, actual) {
            "MethodInvocation.countMethodCallStackFrames returned $actual for a method with an invocation"
        }
    }

    private companion object {

        private const val TEST_METHOD_NAME = "foo"

    }

}
