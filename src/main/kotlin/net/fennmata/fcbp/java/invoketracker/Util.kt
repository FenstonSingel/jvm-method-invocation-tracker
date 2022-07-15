package net.fennmata.fcbp.java.invoketracker

val Class<*>.internalName: String get() = name.replace(".", "/")
