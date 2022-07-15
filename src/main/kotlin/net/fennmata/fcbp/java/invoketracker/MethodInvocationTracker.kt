package net.fennmata.fcbp.java.invoketracker

// TODO implement actual JVM method invocation tracking
// TODO implement generating a dependency with this object only
// TODO implement less memory-intensive method tags
object MethodInvocationTracker {

    @JvmStatic
    fun onMethodEntry(methodTag: String) {
        println("detected method entry: $methodTag")
    }

    @JvmStatic
    fun onMethodExit(methodTag: String) {
        println("detected method exit: $methodTag")

    }

}
