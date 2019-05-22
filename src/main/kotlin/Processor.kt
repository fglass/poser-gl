import entity.Camera
import entity.Entity
import gui.Gui
import input.MouseHandler
import model.DatLoader
import net.openrs.cache.Cache
import net.openrs.cache.FileStore
import org.joml.Vector3f
import org.liquidengine.legui.animation.AnimatorProvider
import org.liquidengine.legui.component.Frame
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
import org.lwjgl.opengl.GL11
import org.lwjgl.system.MemoryUtil
import render.Loader
import render.Renderer
import shader.StaticShader
import java.io.File

const val TITLE = "PoserGL"
const val WIDTH = 762
const val HEIGHT = 503
const val CACHE_PATH = "./repository/cache/"

fun main() {
    Processor().run()
}

class Processor {

    private var running = true
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
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GL11.GL_TRUE)

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


        val context = Context(window)
        val keeper = DefaultCallbackKeeper()
        CallbackKeeper.registerCallbacks(window, keeper)

        val glfwMouseCallbackI = {_: Long, x: Double, y: Double -> MouseHandler().invoke(x, y) }
        val glfwKeyCallbackI = { _: Long, key: Int, _: Int, action: Int, _: Int ->
            running = !(key == GLFW.GLFW_KEY_ESCAPE && action != GLFW.GLFW_RELEASE)
        }
        val glfwWindowCloseCallbackI = { _: Long -> running = false }

        keeper.chainCursorPosCallback.add(glfwMouseCallbackI)
        keeper.chainKeyCallback.add(glfwKeyCallbackI)
        keeper.chainWindowCloseCallback.add(glfwWindowCloseCallbackI)

        val systemEventProcessor = SystemEventProcessor()
        systemEventProcessor.addDefaultCallbacks(keeper)

        val renderer = NvgRenderer()
        renderer.initialize()

        val shader = StaticShader()
        val glRenderer = Renderer(shader)
        GL11.glEnable(GL_PROGRAM_POINT_SIZE_EXT)

        val camera = Camera()

        // Render loop
        while (running) {
            context.updateGlfwWindow()
            val windowSize = context.framebufferSize

            GL11.glClearColor(1f, 0f, 0f, 1f)
            GL11.glViewport(0, 0, windowSize.x, windowSize.y)
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT or GL11.GL_STENCIL_BUFFER_BIT)

            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL)

            // Render frame
            try {
                renderer.render(frame, context)
            } catch (ignore: NullPointerException) {
                println("Error: ${ignore.message}")
            }

            GL11.glEnable(GL11.GL_CULL_FACE)
            GL11.glCullFace(GL11.GL_BACK)

            if (vertices) {
                GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_POINT)
            } else if (wireframe) {
                GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE)
            }

            // Render gl
            if (entity != null) {
                shader.start()
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
                println("Error: ${ignore.message}")
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
        entity = Entity(model, Vector3f(0f, -20f, -50f), 180.0, 0.0, 0.0, 0.05f)
    }
}