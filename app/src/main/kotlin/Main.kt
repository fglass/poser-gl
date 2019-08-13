import mu.KotlinLogging
import render.RenderContext
import java.lang.management.ManagementFactory
import java.util.ArrayList

private val logger = KotlinLogging.logger {}

fun main() {
    try {
        if (!restart()) {
            RenderContext().run()
        }
    } catch (e: Exception) {
        logger.error(e) { "Main exception encountered" }
    }
}

fun restart(): Boolean {
    val os = System.getProperty("os.name")
    if (!os.startsWith("Mac") && !os.startsWith("Darwin")) {
        return false
    }

    // Get current JVM process pid
    val pid =
        ManagementFactory.getRuntimeMXBean().name.split("@".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]

    // Get environment variable on whether XstartOnFirstThread is enabled
    val env = System.getenv("JAVA_STARTED_ON_FIRST_THREAD_$pid")

    // If environment variable is "1" then XstartOnFirstThread is enabled
    if (env != null && env == "1") {
        return false
    }

    // Restart JVM with -XstartOnFirstThread
    val separator = System.getProperty("file.separator")
    val classpath = System.getProperty("java.class.path")
    val main = System.getenv("JAVA_MAIN_CLASS_$pid")
    val jvm = System.getProperty("java.home") + separator + "bin" + separator + "java"

    val inputArgs = ManagementFactory.getRuntimeMXBean().inputArguments
    val jvmArgs = ArrayList<String>()

    jvmArgs.add(jvm)
    jvmArgs.add("-XstartOnFirstThread")
    jvmArgs.addAll(inputArgs)
    jvmArgs.add("-cp")
    jvmArgs.add(classpath)
    jvmArgs.add(main)

    val processBuilder = ProcessBuilder(jvmArgs)
    processBuilder.start()
    return true
}