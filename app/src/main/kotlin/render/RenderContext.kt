package render

import animation.AnimationHandler
import api.cache.ICacheLoader
import cache.CacheService
import cache.PluginLoader
import entity.EntityHandler
import gui.GuiManager
import gui.component.ConfirmDialog
import gui.component.StartDialog
import model.ModelParser
import mu.KotlinLogging
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector2i
import com.spinyowl.legui.animation.AnimatorProvider
import com.spinyowl.legui.component.Frame
import com.spinyowl.legui.input.Mouse
import com.spinyowl.legui.listener.processor.EventProcessorProvider
import com.spinyowl.legui.style.Style
import com.spinyowl.legui.style.color.ColorConstants
import com.spinyowl.legui.style.color.ColorUtil
import com.spinyowl.legui.style.length.Length
import com.spinyowl.legui.style.length.LengthType
import com.spinyowl.legui.system.context.CallbackKeeper
import com.spinyowl.legui.system.context.Context
import com.spinyowl.legui.system.context.DefaultCallbackKeeper
import com.spinyowl.legui.system.handler.processor.SystemEventProcessor
import com.spinyowl.legui.system.handler.processor.SystemEventProcessorImpl
import com.spinyowl.legui.system.layout.LayoutManager
import com.spinyowl.legui.system.renderer.nvg.NvgRenderer
import com.spinyowl.legui.theme.Themes
import com.spinyowl.legui.theme.colored.FlatColoredTheme
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWImage
import org.lwjgl.opengl.EXTGeometryShader4.GL_PROGRAM_POINT_SIZE_EXT
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryUtil
import transfer.ExportManager
import transfer.ImportManager
import util.*
import java.lang.Boolean.TRUE
import kotlin.system.exitProcess

const val TITLE = "PoserGL"
const val VERSION = "1.4.0"
const val WIDTH = 800
const val HEIGHT = 600

private val LOGGER = KotlinLogging.logger {}

private val DEFAULT_THEME = FlatColoredTheme(
    ColorUtil.fromInt(33, 33, 33, 1f),
    ColorUtil.fromInt(97, 97, 97, 1f),
    ColorUtil.fromInt(2, 119, 189, 1f),
    ColorUtil.fromInt(27, 94, 32, 1f),
    ColorUtil.fromInt(183, 28, 28, 1f),
    null,
    ColorUtil.fromInt(255, 255, 255, 1f)
)

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
    val settingsManager = SettingsManager(this)
    val importManager = ImportManager(this)
    val exportManager = ExportManager(this)

    val modelParser = ModelParser()
    val entityHandler = EntityHandler(this)
    val animationHandler = AnimationHandler(this)

    private val plugins = PluginLoader.load()
    val loaders = plugins.first
    val packers = plugins.second

    fun run() {
        var running = true
        LOGGER.info { "Running v$VERSION" }

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
        if (window == MemoryUtil.NULL) {
            throw RuntimeException("Failed to create GLFW window")
        }

        window.setIcon()
        glfwSetWindowSizeLimits(window, WIDTH, HEIGHT, GLFW_DONT_CARE, GLFW_DONT_CARE)
        glfwShowWindow(window)
        glfwMakeContextCurrent(window)
        GL.createCapabilities()

        val context = Context(window)
        context.updateGlfwWindow()

        Themes.setDefaultTheme(DEFAULT_THEME)
        DEFAULT_THEME.applyAll(frame)

        val keeper = DefaultCallbackKeeper()
        CallbackKeeper.registerCallbacks(window, keeper)

        keeper.chainWindowCloseCallback.add { running = false }
        keeper.chainKeyCallback.add(KeyCallback(this, context))

        val systemEventProcessor = SystemEventProcessorImpl()
        SystemEventProcessor.addDefaultCallbacks(keeper, systemEventProcessor)

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
        settingsManager.load()

        if (loaders.isEmpty() || packers.isEmpty()) {
            LOGGER.error("No plugins found")
            exitProcess(1)
        } else {
            LOGGER.info("Loaded ${loaders.size} plugins")
        }

        val devMode = System.getenv("DEV_MODE")

        if (devMode.toBoolean()) {
            LOGGER.info("Running in development mode")
            val cachePath = System.getenv("DEV_CACHE")
            val plugin = loaders.first { it.toString() == System.getenv("DEV_PLUGIN") }
            loadCache(cachePath, plugin)
        } else {
            StartDialog(this).show(frame)
        }

        // Render loop
        while (running) {
            context.updateGlfwWindow()

            val colour = Colour.GRAY.rgba
            glClearColor(colour.x, colour.y, colour.z, colour.w)

            val windowSize = context.framebufferSize
            glViewport(0, 0, windowSize.x, windowSize.y)

            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT or GL_STENCIL_BUFFER_BIT)
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL)

            // Render gui
            try {
                guiRenderer.render(frame, context)
            } catch (e: NullPointerException) {
                LOGGER.error(e) { "Render error" }
            }

            // Render fbo
            framebuffer.render()

            // Poll events to callbacks
            glfwPollEvents()
            glfwSwapBuffers(window)

            // Process system events
            systemEventProcessor.processEvents(frame, context)
            EventProcessorProvider.getInstance().processEvents()

            // Relayout components
            try {
                LayoutManager.getInstance().layout(frame)
            } catch (e: NullPointerException) {
                LOGGER.error(e) { "Layout error" }
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

    private fun Long.setIcon() {
        val icon = ResourceMap["icon"]
        val image = GLFWImage.malloc()
        image.set(icon.width, icon.height, icon.imageData)

        val images = GLFWImage.malloc(1)
        images.put(0, image)
        glfwSetWindowIcon(this, images)

        image.free()
        images.free()
    }

    private fun isRetinaDisplay(contextSize: Vector2i, frameSize: Vector2f): Boolean {
        return contextSize.x == frameSize.x.toInt() * 2 && contextSize.y == frameSize.y.toInt() * 2
    }

    fun loadCache(cachePath: String, plugin: ICacheLoader) {
        cacheService.init(cachePath, plugin)

        if (cacheService.loaded) {
            startApplication()
        }
    }

    private fun startApplication() {
        gui = GuiManager(this)
        entityHandler.loadPlayer()
    }

    fun reset() {
        ConfirmDialog(this, "Warning", "Any unsaved changes will be lost", "Continue") {
            nodeRenderer.enabled = false
            entityHandler.clear()
            gui.container.clearChildComponents()
            StartDialog(this).show(frame)
        }.show(frame)
    }
}