package render

import animation.AnimationHandler
import api.cache.ICacheLoader
import cache.CacheService
import cache.PluginLoader
import entity.EntityHandler
import gui.GuiManager
import gui.component.AboutDialog
import gui.component.ConfirmDialog
import gui.component.StartDialog
import model.ModelParser
import mu.KotlinLogging
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector2i
import org.liquidengine.legui.DefaultInitializer
import org.liquidengine.legui.animation.AnimatorProvider
import org.liquidengine.legui.component.Frame
import org.liquidengine.legui.input.Mouse
import org.liquidengine.legui.style.color.ColorUtil
import org.liquidengine.legui.system.layout.LayoutManager
import org.liquidengine.legui.theme.Themes
import org.liquidengine.legui.theme.colored.FlatColoredTheme
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
const val APP_VERSION = "1.4.0"
const val WIDTH = 800
const val HEIGHT = 600

private val LOGGER = KotlinLogging.logger {}

private val DEFAULT_THEME = FlatColoredTheme(
    ColorUtil.fromInt(33, 33, 33, 1f),
    ColorUtil.fromInt(97, 97, 97, 1f),
    ColorUtil.fromInt(2, 119, 189, 1f),
    ColorUtil.fromInt(27, 94, 32, 1f),
    ColorUtil.fromInt(183, 28, 28, 1f),
    null
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
        LOGGER.info("Running v$APP_VERSION")

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

        Themes.setDefaultTheme(DEFAULT_THEME)
        DEFAULT_THEME.applyAll(frame)

        val initializer = DefaultInitializer(window, frame)
        val context = initializer.context
        context.updateGlfwWindow()

        val keeper = initializer.callbackKeeper

        keeper.chainKeyCallback.add(KeyCallback(this, context))
        keeper.chainWindowCloseCallback.add {
            if (cacheService.loaded) {
                ConfirmDialog(this, "Warning", "Unsaved changes will be lost", "Exit", true) {
                    running = false
                }.show(frame)
            } else {
                running = false
            }
        }

        val animator = AnimatorProvider.getAnimator()
        val guiRenderer = initializer.renderer
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
            LOGGER.error("No compatible plugins found")
            ConfirmDialog(this, "Error", "No compatible plugins found", "Exit", false) {
                exitProcess(1)
            }.show(frame)
        } else {
            LOGGER.info("Loaded ${loaders.size} plugins")
            openApplication()
        }

        while (running) {
            framebuffer.render()

            context.updateGlfwWindow()

            val colour = Colour.GRAY.rgba
            glClearColor(colour.x, colour.y, colour.z, colour.w)

            val windowSize = context.framebufferSize
            glViewport(0, 0, windowSize.x, windowSize.y)

            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT or GL_STENCIL_BUFFER_BIT)
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL)

            try {
                guiRenderer.render(frame, context)
            } catch (e: NullPointerException) {
                LOGGER.error(e) { "Render error" }
            }

            // Poll events to callbacks
            glfwPollEvents()
            glfwSwapBuffers(window)

            // Run GUI animations
            animator.runAnimations()

            // Process system events
            initializer.systemEventProcessor.processEvents(frame, context)
            initializer.guiEventProcessor.processEvents()

            // Relayout components
            try {
                LayoutManager.getInstance().layout(frame, context)
            } catch (e: NullPointerException) {
                LOGGER.error(e) { "Layout error" }
            }

            vSync.waitIfNecessary()
        }

        cleanUp()
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

    private fun openApplication() {
        val devMode = System.getenv("DEV_MODE")
        if (devMode.toBoolean()) {
            LOGGER.info("Running in development mode")
            val cachePath = System.getenv("DEV_CACHE")
            val devPlugin = System.getenv("DEV_PLUGIN")
            val plugin = loaders.first { it.toString() == devPlugin }
            loadCache(cachePath, plugin)
        } else {
            StartDialog(this).show(frame)
        }
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

    fun openAboutDialog() {
        AboutDialog(this).display()
    }

    fun reset() {
        ConfirmDialog(this, "Warning", "Unsaved changes will be lost", "Continue", true) {
            cacheService.loaded = false
            nodeRenderer.enabled = false
            entityHandler.clear()
            gui.container.clearChildComponents()
            StartDialog(this).show(frame)
        }.show(frame)
    }

    fun cleanUp() {
        modelParser.cleanUp()
        entityRenderer.cleanUp()
        gizmoRenderer.cleanUp()
        nodeRenderer.cleanUp()
        lineRenderer.cleanUp()
    }
}