import animation.AnimationHandler
import cache.CacheService
import entity.Entity
import entity.EntityHandler
import gui.GuiManager
import gui.component.Popup
import io.MouseHandler
import model.ModelParser
import mu.KotlinLogging
import org.joml.Vector2f
import org.joml.Vector2i
import org.joml.Vector4f
import org.liquidengine.legui.animation.AnimatorProvider
import org.liquidengine.legui.component.Frame
import org.liquidengine.legui.event.WindowSizeEvent
import org.liquidengine.legui.listener.processor.EventProcessor
import org.liquidengine.legui.system.context.CallbackKeeper
import org.liquidengine.legui.system.context.Context
import org.liquidengine.legui.system.context.DefaultCallbackKeeper
import org.liquidengine.legui.system.handler.processor.SystemEventProcessor
import org.liquidengine.legui.system.layout.LayoutManager
import org.liquidengine.legui.system.renderer.nvg.NvgRenderer
import org.liquidengine.legui.theme.Themes
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWWindowCloseCallbackI
import org.lwjgl.opengl.EXTGeometryShader4.GL_PROGRAM_POINT_SIZE_EXT
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryUtil
import render.Framebuffer
import render.Loader
import render.NodeRenderer
import render.PlaneRenderer
import shader.StaticShader
import util.VSyncTimer
import java.lang.management.ManagementFactory
import java.util.*

const val TITLE = "PoserGL"
const val VERSION = "1.1"
const val CACHE_PATH = "./repository/cache317/"
const val SPRITE_PATH = "sprite/"
val BG_COLOUR = Vector4f(33 / 255f, 33 / 255f, 33 / 255f, 1f)

const val WIDTH = 800
const val HEIGHT = 600
private val logger = KotlinLogging.logger {}

fun main() {
    try {
        if (restartJVM()) {
            return
        }
        Processor().run()
    } catch (e: Exception) {
        logger.error(e) { "Main exception encountered" }
    }
}

class Processor {

    val frame = Frame(WIDTH.toFloat(), HEIGHT.toFloat())
    lateinit var gui: GuiManager
    lateinit var framebuffer: Framebuffer
    lateinit var nodeRenderer: NodeRenderer
    lateinit var planeRenderer: PlaneRenderer

    private var running = true
    val cacheService = CacheService(this)
    val loader = Loader()
    val modelParser = ModelParser(loader)
    val entityHandler = EntityHandler(this)
    val animationHandler = AnimationHandler(this)
    var entity: Entity? = null

    fun run() {
        System.setProperty("joml.nounsafe", java.lang.Boolean.TRUE.toString())
        System.setProperty("java.awt.headless", java.lang.Boolean.TRUE.toString())

        if (!glfwInit()) {
            throw RuntimeException("Unable to initialize GLFW")
        }

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2)
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE)

        val window = glfwCreateWindow(WIDTH, HEIGHT, "$TITLE v$VERSION", MemoryUtil.NULL, MemoryUtil.NULL)
        glfwShowWindow(window)

        glfwMakeContextCurrent(window)
        GL.createCapabilities()

        val context = Context(window)
        context.updateGlfwWindow()
        Themes.setDefaultTheme(Themes.FLAT_DARK)
        Themes.getDefaultTheme().applyAll(frame)

        val frameSize = frame.container.size
        gui = GuiManager(Vector2f(0f, 0f), frameSize, this)
        gui.createElements()
        frame.container.add(gui)

        val keeper = DefaultCallbackKeeper()
        CallbackKeeper.registerCallbacks(window, keeper)

        val mouse = MouseHandler()
        val windowCloseCallback = GLFWWindowCloseCallbackI { running = false }
        keeper.chainWindowCloseCallback.add(windowCloseCallback)

        val systemEventProcessor = SystemEventProcessor()
        systemEventProcessor.addDefaultCallbacks(keeper)

        val guiRenderer = NvgRenderer()
        guiRenderer.initialize()

        val vSync = VSyncTimer()
        val shader = StaticShader()
        val scaleFactor = if (isRetinaDisplay(context.framebufferSize, frameSize)) 2 else 1

        framebuffer = Framebuffer(this, shader, mouse, scaleFactor)
        framebuffer.lateInit()
        frame.container.add(framebuffer)
        gui.listenerMap.addListener(WindowSizeEvent::class.java) { event ->
            gui.resize(Vector2f(event.width.toFloat(), event.height.toFloat()))
            framebuffer.resize()
        }

        nodeRenderer = NodeRenderer(this, framebuffer)
        planeRenderer = PlaneRenderer(framebuffer)
        glEnable(GL_PROGRAM_POINT_SIZE_EXT)

        if (cacheService.loaded) {
            entityHandler.loadPlayer()
        } else {
            Popup("Cache Error", "Unable to load a valid cache", 260f, 70f).show(frame)
        }

        // Render loop
        while (running) {
            context.updateGlfwWindow()
            val windowSize = context.framebufferSize

            glClearColor(BG_COLOUR.x, BG_COLOUR.y, BG_COLOUR.z, BG_COLOUR.w)
            glViewport(0, 0, windowSize.x, windowSize.y)
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT or GL_STENCIL_BUFFER_BIT)
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL)

            // Render gui
            try {
                guiRenderer.render(frame, context)
            } catch (e: NullPointerException) {
                logger.error(e) { "UI render error" }
            }

            // Render fbo
            framebuffer.render()

            // Poll events to callbacks
            glfwPollEvents()
            glfwSwapBuffers(window)

            // Process system events
            systemEventProcessor.processEvents(frame, context)
            EventProcessor.getInstance().processEvents()

            // Relayout components
            try {
                LayoutManager.getInstance().layout(frame)
            } catch (e: NullPointerException) {
                logger.error(e) { "Layout error" }
            }

            // Run gui animations
            AnimatorProvider.getAnimator().runAnimations()

            // Control fps
            vSync.waitIfNecessary()
        }

        nodeRenderer.cleanUp()
        planeRenderer.cleanUp()
        shader.cleanUp()
        loader.cleanUp()
        guiRenderer.destroy()
        glfwDestroyWindow(window)
        glfwTerminate()
    }

    private fun isRetinaDisplay(contextSize: Vector2i, frameSize: Vector2f): Boolean {
        return contextSize.x == frameSize.x.toInt() * 2 && contextSize.y == frameSize.y.toInt() * 2
    }
}

fun restartJVM(): Boolean {
    val osName = System.getProperty("os.name")

    // If not a mac return false
    if (!osName.startsWith("Mac") && !osName.startsWith("Darwin")) {
        return false
    }

    // Get current jvm process pid
    val pid =
        ManagementFactory.getRuntimeMXBean().name.split("@".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]

    //Get environment variable on whether XstartOnFirstThread is enabled
    val env = System.getenv("JAVA_STARTED_ON_FIRST_THREAD_$pid")

    // If environment variable is "1" then XstartOnFirstThread is enabled
    if (env != null && env == "1") {
        return false
    }

    // Restart jvm with -XstartOnFirstThread
    val separator = System.getProperty("file.separator")
    val classpath = System.getProperty("java.class.path")
    val mainClass = System.getenv("JAVA_MAIN_CLASS_$pid")
    val jvmPath = System.getProperty("java.home") + separator + "bin" + separator + "java"

    val inputArguments = ManagementFactory.getRuntimeMXBean().inputArguments
    val jvmArgs = ArrayList<String>()

    jvmArgs.add(jvmPath)
    jvmArgs.add("-XstartOnFirstThread")
    jvmArgs.addAll(inputArguments)
    jvmArgs.add("-cp")
    jvmArgs.add(classpath)
    jvmArgs.add(mainClass)

    val processBuilder = ProcessBuilder(jvmArgs)
    processBuilder.start()
    return true
}