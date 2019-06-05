import entity.Camera
import entity.Entity
import gui.Gui
import input.Mouse
import model.AnimationHandler
import model.DatLoader
import model.NpcLoader
import model.RawModel
import net.runelite.cache.definitions.NpcDefinition
import org.joml.Vector2f
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
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.EXTGeometryShader4.GL_PROGRAM_POINT_SIZE_EXT
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryUtil
import render.Loader
import render.Renderer
import shader.ShadingType
import shader.StaticShader
import utils.VSyncTimer
import java.awt.Rectangle

const val TITLE = "PoserGL"
const val BG_COLOUR = 33 / 255f
const val CACHE_PATH = "./repository/old/"
val CLIP_REGION = Rectangle(312, 0, 100, 52)
val ENTITY_POS = Vector3f(0f, -20f, -50f)
val ENTITY_ROT = Vector3f(0f, 0f, 180f)

fun main() {
    Processor().run()
}

class Processor {

    private var running = true
    var vertices = false
    var wireframe = false
    var shading = ShadingType.SMOOTH

    lateinit var gui: Gui
    lateinit var glRenderer: Renderer
    val loader = Loader()
    val datLoader = DatLoader(loader)
    val npcLoader = NpcLoader(this)
    val animationHandler = AnimationHandler(this)
    val entities = ArrayList<Entity>()

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

        val size = frame.container.size
        gui = Gui(Vector2f(0f, 0f), size, this)
        gui.createElements()
        frame.container.add(gui)
        frame.container.listenerMap.addListener(WindowSizeEvent::class.java) {
            gui.resize(size)
        }

        val context = Context(window)
        val keeper = DefaultCallbackKeeper()
        CallbackKeeper.registerCallbacks(window, keeper)

        val mouse = Mouse()
        val mouseCallback = {_: Long, button: Int, action: Int, _: Int -> mouse.handleClick(button, action) }
        val scrollCallback = {_: Long, dx: Double, dy: Double -> mouse.handleScroll(dx, dy) }
        val cursorCallback = {_: Long, x: Double, y: Double -> mouse.handlePosition(x, y) }
        val windowCloseCallback = { _: Long -> running = false }

        keeper.chainMouseButtonCallback.add(mouseCallback)
        keeper.chainScrollCallback.add(scrollCallback)
        keeper.chainCursorPosCallback.add(cursorCallback)
        keeper.chainWindowCloseCallback.add(windowCloseCallback)

        val systemEventProcessor = SystemEventProcessor()
        systemEventProcessor.addDefaultCallbacks(keeper)

        val guiRenderer = NvgRenderer()
        guiRenderer.initialize()

        val vSync = VSyncTimer()
        val shader = StaticShader()
        val camera = Camera(mouse)
        glRenderer = Renderer(this, shader)
        npcLoader.load(npcLoader.manager.get(0))
        glEnable(GL_PROGRAM_POINT_SIZE_EXT)

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

            glViewport(
                CLIP_REGION.x, CLIP_REGION.y, windowSize.x - CLIP_REGION.width, windowSize.y - CLIP_REGION.height
            )

            glEnable(GL_DEPTH_TEST)
            glEnable(GL_CULL_FACE)
            glCullFace(GL_BACK)
            glEnable(GL_BLEND)
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

            if (vertices) {
                glPolygonMode(GL_FRONT_AND_BACK, GL_POINT)
            } else if (wireframe) {
                glPolygonMode(GL_FRONT_AND_BACK, GL_LINE)
            }

            // Render entities
            animationHandler.tickAnimation()
            camera.move()
            shader.start()
            shader.loadViewMatrix(camera)
            shader.loadShadingToggle(shading != ShadingType.NONE)
            glRenderer.render(entities, shader)
            shader.stop()

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

    fun addModel(model: RawModel) {
        entities.add(Entity(model, ENTITY_POS, ENTITY_ROT, 0.05f))
    }
}