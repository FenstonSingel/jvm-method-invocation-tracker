package net.fennmata.fcbp.java.invoketracker

import java.lang.instrument.Instrumentation
import java.util.concurrent.atomic.AtomicBoolean

@Suppress("UNUSED")
object PremainClass {

    @JvmStatic
    fun premain(argumentsString: String?, instrumentation: Instrumentation) {
        // this is a godforsaken workaround motivated by
        // a bug in IDEA's Gradle run delegation feature
        // that runs all(?) premain methods twice
        if (hasAgentAlreadyBeenStarted.getAndSet(true)) return

        with(instrumentation) {
            addTransformer(MITClassFileTransformer, isRetransformClassesSupported)
        }
    }

    private var hasAgentAlreadyBeenStarted = AtomicBoolean(false)

}
