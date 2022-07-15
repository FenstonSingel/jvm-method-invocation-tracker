package net.fennmata.fcbp.java.invoketracker

import java.lang.instrument.Instrumentation

@Suppress("UNUSED")
object PremainClass {

    @JvmStatic
    fun premain(argumentsString: String?, instrumentation: Instrumentation) {
        with(instrumentation) {
            addTransformer(MITClassFileTransformer, isRetransformClassesSupported)
        }
    }

}
