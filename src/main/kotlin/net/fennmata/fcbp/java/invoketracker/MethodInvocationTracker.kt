package net.fennmata.fcbp.java.invoketracker

import java.util.concurrent.ConcurrentHashMap

// TODO implement generating a dependency with this object only
// TODO implement less memory-intensive method tags
object MethodInvocationTracker {

    private val callStackFramesCounters: MutableMap<String, Int> = ConcurrentHashMap()

    @JvmStatic
    fun isMethodInvoked(methodTag: String): Boolean = methodTag in callStackFramesCounters

    @JvmStatic
    fun countMethodCallStackFrames(methodTag: String): Int = callStackFramesCounters.getOrDefault(methodTag, 0)

    @JvmStatic
    fun onMethodEntry(methodTag: String) {
        callStackFramesCounters.merge(methodTag, 1, Int::plus)
    }

    @JvmStatic
    fun onMethodExit(methodTag: String) {
        callStackFramesCounters.computeIfPresent(methodTag) { _, counter -> counter.dec().takeIf { it > 0 } }
    }

    /**
     * This method should only be used in test environments.
     */
    @JvmStatic
    @Synchronized
    fun clear() {
        callStackFramesCounters.clear()
    }

}
