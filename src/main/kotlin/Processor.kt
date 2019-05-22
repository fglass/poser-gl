import entity.Entity
import entity.Camera
import gui.Gui
import input.MouseHandler
import model.DatLoader
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
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.EXTGeometryShader4.GL_PROGRAM_POINT_SIZE_EXT
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.system.MemoryUtil
import render.Loader
import render.Renderer
import shader.StaticShader

const val TITLE = "PoserGL"
const val WIDTH = 762
const val HEIGHT = 503

fun main() {
    Processor().run()
}

class Processor {

    private var running = true
    var wireframe = false
    var vertices = false

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
        val gui = Gui(this)
        gui.createElements(frame)

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

        val loader = Loader()
        val shader = StaticShader()
        val glRenderer = Renderer(shader)
        GL11.glEnable(GL_PROGRAM_POINT_SIZE_EXT)

        val model = DatLoader().load(23889, loader)
        val entity = Entity(model, Vector3f(0f, -20f, -50f), 180.0, 0.0, 0.0, 0.05f)
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
            renderer.render(frame, context)

            GL11.glEnable(GL11.GL_CULL_FACE)
            GL11.glCullFace(GL11.GL_BACK)

            if (wireframe) {
                GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE)
            } else if (vertices) {
                GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_POINT)
            }

            // Render gl
            shader.start()
            shader.loadViewMatrix(camera)
            glRenderer.render(entity, shader)
            shader.stop()

            // Poll events to callbacks
            GLFW.glfwPollEvents()
            GLFW.glfwSwapBuffers(window)

            // Process system events
            systemEventProcessor.processEvents(frame, context)
            EventProcessor.getInstance().processEvents()

            // Relayout components
            LayoutManager.getInstance().layout(frame)

            // Run animations
            AnimatorProvider.getAnimator().runAnimations()
        }

        shader.cleanUp()
        loader.cleanUp()
        renderer.destroy()
        GLFW.glfwDestroyWindow(window)
        GLFW.glfwTerminate()
    }
}