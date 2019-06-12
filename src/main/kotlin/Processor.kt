import animation.AnimationHandler
import entity.Entity
import gui.Gui
import input.Mouse
import model.DatLoader
import model.NpcLoader
import org.joml.Vector2f
import org.joml.Vector2i
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
import org.lwjgl.opengl.EXTGeometryShader4.GL_PROGRAM_POINT_SIZE_EXT
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL30.*
import org.lwjgl.system.MemoryUtil
import render.Framebuffer
import render.Loader
import shader.StaticShader
import utils.VSyncTimer

const val TITLE = "PoserGL"
const val BG_COLOUR = 33 / 255f
const val CACHE_PATH = "./repository/old/"
const val RESOURCES_PATH = "src/main/resources/"

fun main() {
    Processor().run()
}

class Processor {

    private var running = true
    lateinit var gui: Gui
    lateinit var framebuffer: Framebuffer

    val loader = Loader()
    val datLoader = DatLoader(loader)
    val npcLoader = NpcLoader(this)
    val animationHandler = AnimationHandler(this)
    var entity: Entity? = null // TODO: !!

    fun run() {
        System.setProperty("joml.nounsafe", java.lang.Boolean.TRUE.toString())
        System.setProperty("java.awt.headless", java.lang.Boolean.TRUE.toString())

        if (!glfwInit()) {
            throw RuntimeException("Unable to initialize GLFW")
        }

        val width = 800
        val height = 600

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2)
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE)

        val window = glfwCreateWindow(width, height, TITLE, MemoryUtil.NULL, MemoryUtil.NULL)
        glfwShowWindow(window)

        glfwMakeContextCurrent(window)
        GL.createCapabilities()

        val frame = Frame(width.toFloat(), height.toFloat())
        Themes.setDefaultTheme(Themes.FLAT_DARK)
        Themes.getDefaultTheme().applyAll(frame)

        val context = Context(window)
        context.updateGlfwWindow()

        val frameSize = frame.container.size
        gui = Gui(Vector2f(0f, 0f), frameSize, this)
        gui.createElements()
        frame.container.add(gui)

        val keeper = DefaultCallbackKeeper()
        CallbackKeeper.registerCallbacks(window, keeper)

        val mouse = Mouse()
        val windowCloseCallback = {_: Long -> running = false }
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

        glEnable(GL_PROGRAM_POINT_SIZE_EXT)
        npcLoader.load(npcLoader.manager.get(0)) // Load first npc

        // Render loop
        while (running) {
            context.updateGlfwWindow()
            val windowSize = context.framebufferSize

            glClearColor(BG_COLOUR, BG_COLOUR, BG_COLOUR, 1f)
            glViewport(0, 0, windowSize.x, windowSize.y)
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT or GL_STENCIL_BUFFER_BIT)
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL)

            // Render gui
            try {
                guiRenderer.render(frame, context)
            } catch (ignore: NullPointerException) {
                println("Render error: ${ignore.message}")
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
            } catch (ignore: NullPointerException) {
                println("Layout error: ${ignore.message}")
            }

            // Run animations
            AnimatorProvider.getAnimator().runAnimations()

            // Control fps
            vSync.waitIfNecessary()
        }

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