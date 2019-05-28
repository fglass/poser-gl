import entity.Camera
import entity.Entity
import entity.Light
import gui.Gui
import input.Mouse
import model.DatLoader
import net.openrs.cache.Cache
import net.openrs.cache.FileStore
import org.joml.Vector3f
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
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.EXTGeometryShader4.GL_PROGRAM_POINT_SIZE_EXT
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryUtil
import render.Loader
import render.Renderer
import shader.StaticShader
import java.awt.Rectangle
import java.io.File

const val TITLE = "PoserGL"
const val WIDTH = 762
const val HEIGHT = 503
const val BG_COLOUR = 33/255f
const val CACHE_PATH = "./repository/cache/"
val CLIP_REGION = Rectangle(312, 0, 100, 52)

fun main() {
    Processor().run()
}

class Processor {

    private var running = true
    var shading = true
    var vertices = false
    var wireframe = false

    private val datLoader = DatLoader()
    private val loader = Loader()
    private var entity: Entity? = null

    fun run() {
        System.setProperty("joml.nounsafe", java.lang.Boolean.TRUE.toString())
        System.setProperty("java.awt.headless", java.lang.Boolean.TRUE.toString())

        if (!GLFW.glfwInit()) {
            throw RuntimeException("Unable to initialize GLFW")
        }

        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3)
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 2)
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE)
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE)

        val window = GLFW.glfwCreateWindow(WIDTH, HEIGHT, TITLE, MemoryUtil.NULL, MemoryUtil.NULL)
        GLFW.glfwShowWindow(window)

        GLFW.glfwMakeContextCurrent(window)
        GL.createCapabilities()
        GLFW.glfwSwapInterval(1) // Enable vsync

        val frame = Frame(WIDTH.toFloat(), HEIGHT.toFloat())
        Themes.setDefaultTheme(Themes.FLAT_DARK)
        Themes.getDefaultTheme().applyAll(frame)

        val gui = Gui(0f, 0f, WIDTH.toFloat(), HEIGHT.toFloat(), this)
        gui.createElements()
        frame.container.add(gui)
        frame.container.listenerMap.addListener(WindowSizeEvent::class.java) {
            gui.resize(frame.container.size)
        }

        val context = Context(window)
        val keeper = DefaultCallbackKeeper()
        CallbackKeeper.registerCallbacks(window, keeper)

        val mouse = Mouse()
        val glfwMouseCallbackI = {_: Long, button: Int, action: Int, _: Int -> mouse.handleClick(button, action) }
        val glfwScrollCallbackI = {_: Long, dx: Double, dy: Double -> mouse.handleScroll(dx, dy) }
        val glfwCursorCallbackI = {_: Long, x: Double, y: Double -> mouse.handlePosition(x, y) }
        val glfwWindowCloseCallbackI = { _: Long -> running = false }

        keeper.chainMouseButtonCallback.add(glfwMouseCallbackI)
        keeper.chainScrollCallback.add(glfwScrollCallbackI)
        keeper.chainCursorPosCallback.add(glfwCursorCallbackI)
        keeper.chainWindowCloseCallback.add(glfwWindowCloseCallbackI)

        val systemEventProcessor = SystemEventProcessor()
        systemEventProcessor.addDefaultCallbacks(keeper)

        val renderer = NvgRenderer()
        renderer.initialize()

        val shader = StaticShader()
        val camera = Camera(mouse)
        val light = Light(Vector3f(0f, -20f, -100F), Vector3f(1F, 1F, 1F))
        val glRenderer = Renderer(shader)
        glEnable(GL_PROGRAM_POINT_SIZE_EXT)

        // Render loop
        while (running) {
            context.updateGlfwWindow()
            val windowSize = context.framebufferSize

            glClearColor(BG_COLOUR, BG_COLOUR, BG_COLOUR, 1f)
            glViewport(0, 0, windowSize.x, windowSize.y)
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT or GL_STENCIL_BUFFER_BIT)
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL)

            // Render frame
            try {
                renderer.render(frame, context)
            } catch (ignore: NullPointerException) {
                println("Render error: ${ignore.message}")
            }

            glViewport(
                CLIP_REGION.x, CLIP_REGION.y, windowSize.x - CLIP_REGION.width, windowSize.y - CLIP_REGION.height
            )
            glEnable(GL_CULL_FACE)
            glCullFace(GL_BACK)

            if (vertices) {
                glPolygonMode(GL_FRONT_AND_BACK, GL_POINT)
            } else if (wireframe) {
                glPolygonMode(GL_FRONT_AND_BACK, GL_LINE)
            }

            // Render gl
            if (entity != null) {
                camera.move(entity!!)
                shader.start()
                shader.loadLight(light)
                shader.loadShadingToggle(shading)
                shader.loadViewMatrix(camera)
                glRenderer.render(entity!!, shader)
                shader.stop()
            }

            // Poll events to callbacks
            GLFW.glfwPollEvents()
            GLFW.glfwSwapBuffers(window)

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
        }

        shader.cleanUp()
        loader.cleanUp()
        renderer.destroy()
        GLFW.glfwDestroyWindow(window)
        GLFW.glfwTerminate()
    }

    fun getMaxModels(): Int {
        Cache(FileStore.open(File(CACHE_PATH))).use {
            val table = it.getReferenceTable(7)
            return table.capacity()
        }
    }

    fun setModel(id: Int) {
        loader.cleanUp()
        val model = datLoader.load(id, loader)
        entity = Entity(model, Vector3f(0f, -20f, -50f), 0.0, 0.0, 180.0, 0.05f)
    }
}