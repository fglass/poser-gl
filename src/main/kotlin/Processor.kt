import animation.AnimationHandler
import cache.CacheService
import render.NodeRenderer
import entity.Entity
import entity.EntityHandler
import gui.GuiManager
import io.MouseHandler
import model.ModelParser
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
import render.PlaneRenderer
import shader.StaticShader
import util.VSyncTimer

const val TITLE = "PoserGL"
const val CACHE_PATH = "./repository/cache/"
const val RESOURCES_PATH = "src/main/resources/"
const val SPRITE_PATH = "src/main/resources/sprite/"
val BG_COLOUR = Vector4f(33 / 255f, 33 / 255f, 33 / 255f, 1f)

fun main() {
    Processor().run()
}

const val WIDTH = 800
const val HEIGHT = 600

class Processor {

    private var running = true
    lateinit var gui: GuiManager
    lateinit var framebuffer: Framebuffer
    lateinit var nodeRenderer: NodeRenderer
    lateinit var planeRenderer: PlaneRenderer

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

        val window = glfwCreateWindow(WIDTH, HEIGHT, TITLE, MemoryUtil.NULL, MemoryUtil.NULL)
        glfwShowWindow(window)

        glfwMakeContextCurrent(window)
        GL.createCapabilities()

        val context = Context(window)
        context.updateGlfwWindow()

        val frame = Frame(WIDTH.toFloat(), HEIGHT.toFloat())
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

        entityHandler.loadPlayer()
        glEnable(GL_PROGRAM_POINT_SIZE_EXT)

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
            } catch (ignored: NullPointerException) {
                println("Render error")
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
            } catch (ignored: NullPointerException) {
                println("Layout error")
            }

            // Run legui animations
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