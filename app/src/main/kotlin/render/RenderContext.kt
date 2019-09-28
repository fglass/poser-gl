package render

import animation.AnimationHandler
import cache.CacheService
import cache.PluginLoader
import entity.Entity
import entity.EntityHandler
import gui.GuiManager
import gui.component.StartDialog
import model.ModelParser
import mu.KotlinLogging
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector2i
import org.joml.Vector4f
import org.liquidengine.legui.animation.AnimatorProvider
import org.liquidengine.legui.component.Frame
import org.liquidengine.legui.input.Mouse
import org.liquidengine.legui.listener.processor.EventProcessor
import org.liquidengine.legui.style.color.ColorUtil
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
import transfer.ExportManager
import transfer.ImportManager
import util.MouseButtonHandler
import util.VSyncTimer
import org.liquidengine.legui.theme.colored.FlatColoredTheme
import util.MatrixCreator
import java.lang.Boolean.TRUE

const val TITLE = "PoserGL"
const val VERSION = "1.3"
const val SPRITE_PATH = "sprite/" // TODO: resources map
const val WIDTH = 800
const val HEIGHT = 600

private val logger = KotlinLogging.logger {}
val BG_COLOUR: Vector4f = ColorUtil.fromInt(33, 33, 33, 1f)

class RenderContext {

    val frame = Frame(WIDTH.toFloat(), HEIGHT.toFloat())
    lateinit var gui: GuiManager
    lateinit var framebuffer: Framebuffer
    lateinit var entityRenderer: EntityRenderer
    lateinit var nodeRenderer: NodeRenderer
    lateinit var lineRenderer: LineRenderer
    lateinit var gizmoRenderer: GizmoRenderer
    lateinit var projectionMatrix: Matrix4f

    val cacheService = CacheService(this)
    val importManager = ImportManager(this)
    val exportManager = ExportManager(this)
    val loaders = PluginLoader.getLoaders()

    val modelParser = ModelParser()
    var entity: Entity? = null
    val entityHandler = EntityHandler(this)
    val animationHandler = AnimationHandler(this)

    fun run() {
        var running = true
        System.setProperty("joml.nounsafe", TRUE.toString())
        System.setProperty("java.awt.headless", TRUE.toString())

        if (!glfwInit()) {
            throw RuntimeException("Unable to initialize GLFW")
        }

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2)
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE)

        val window = glfwCreateWindow(WIDTH, HEIGHT, TITLE, MemoryUtil.NULL, MemoryUtil.NULL)
        glfwSetWindowSizeLimits(window, WIDTH, HEIGHT, GLFW_DONT_CARE, GLFW_DONT_CARE)
        glfwShowWindow(window)
        glfwMakeContextCurrent(window)
        GL.createCapabilities()

        val context = Context(window)
        context.updateGlfwWindow()

        val theme = FlatColoredTheme(
            ColorUtil.fromInt(33, 33, 33, 1f),
            ColorUtil.fromInt(97, 97, 97, 1f),
            ColorUtil.fromInt(2, 119, 189, 1f),
            ColorUtil.fromInt(27, 94, 32, 1f),
            ColorUtil.fromInt(183, 28, 28, 1f),
            null
        )
        Themes.setDefaultTheme(theme)
        theme.applyAll(frame)

        val keeper = DefaultCallbackKeeper()
        CallbackKeeper.registerCallbacks(window, keeper)

        val windowCloseCallback = GLFWWindowCloseCallbackI { running = false }
        keeper.chainWindowCloseCallback.add(windowCloseCallback)

        val systemEventProcessor = SystemEventProcessor()
        systemEventProcessor.addDefaultCallbacks(keeper)

        val guiRenderer = NvgRenderer()
        guiRenderer.initialize()

        val vSync = VSyncTimer()
        val scaleFactor = if (isRetinaDisplay(context.framebufferSize, frame.container.size)) 2 else 1
        val lmb = MouseButtonHandler(Mouse.MouseButton.MOUSE_BUTTON_LEFT)
        val mmb = MouseButtonHandler(Mouse.MouseButton.MOUSE_BUTTON_MIDDLE)
        val rmb = MouseButtonHandler(Mouse.MouseButton.MOUSE_BUTTON_RIGHT)

        framebuffer = Framebuffer(this, scaleFactor, arrayOf(lmb, mmb, rmb))
        entityRenderer = EntityRenderer(this)
        lineRenderer = LineRenderer(this)
        nodeRenderer = NodeRenderer(this, lmb)
        gizmoRenderer = GizmoRenderer(this, lmb)
        projectionMatrix = MatrixCreator.createProjectionMatrix(WIDTH, HEIGHT)

        glEnable(GL_PROGRAM_POINT_SIZE_EXT)
        StartDialog(this).show(frame)
        logger.info { "Loaded ${loaders.size} plugins" }

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

        modelParser.cleanUp()
        entityRenderer.cleanUp()
        gizmoRenderer.cleanUp()
        nodeRenderer.cleanUp()
        lineRenderer.cleanUp()

        guiRenderer.destroy()
        glfwDestroyWindow(window)
        glfwTerminate()
    }

    private fun isRetinaDisplay(contextSize: Vector2i, frameSize: Vector2f): Boolean {
        return contextSize.x == frameSize.x.toInt() * 2 && contextSize.y == frameSize.y.toInt() * 2
    }
}