package render

import BG_COLOUR
import Processor
import entity.Camera
import input.Mouse
import org.joml.Vector2f
import org.liquidengine.legui.component.ImageView
import org.liquidengine.legui.event.CursorEnterEvent
import org.liquidengine.legui.event.MouseClickEvent
import org.liquidengine.legui.event.MouseDragEvent
import org.liquidengine.legui.event.ScrollEvent
import org.liquidengine.legui.image.FBOImage
import org.liquidengine.legui.style.Style
import org.lwjgl.opengl.GL32.*
import shader.ShadingType
import shader.StaticShader

class Framebuffer(
    private val context: Processor, private val shader: StaticShader,
    private val mouse: Mouse, private val scaleFactor: Int) : ImageView() {

    private var id: Int = 0
    private var textureId: Int = 0
    private var textureWidth = 0
    private var textureHeight = 0

    private lateinit var glRenderer: Renderer
    private val camera = Camera(mouse)

    var polygonMode = PolygonMode.FILL
    var shadingType = ShadingType.SMOOTH

    init {
        position = (Vector2f(160f, 49f))
        resize()
        focusedStyle.border.isEnabled = false
        focusedStyle.display = Style.DisplayType.NONE

        listenerMap.addListener(MouseClickEvent::class.java) { event ->
            mouse.handleClick(event.button, event.action)
        }
        listenerMap.addListener(MouseDragEvent::class.java) { event ->
            mouse.handleDrag(event.delta)
        }
        listenerMap.addListener(ScrollEvent::class.java) { event ->
            mouse.handleScroll(event.yoffset)
        }
        listenerMap.addListener(CursorEnterEvent::class.java) { event ->
            mouse.handleCursorEvent(event.isEntered)
        }
    }

    fun lateInit() {
        glRenderer = Renderer(context, shader)
    }

    private fun createTexture(): FBOImage {
        id = glGenFramebuffers()
        glBindFramebuffer(GL_FRAMEBUFFER, id)

        textureId = glGenTextures()
        glBindTexture(GL_TEXTURE_2D, textureId)

        glTexImage2D(
            GL_TEXTURE_2D,
            0,
            GL_RGBA,
            textureWidth,
            textureHeight,
            0,
            GL_RGBA,
            GL_UNSIGNED_BYTE,
            0
        )

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)

        val renderBufferId = glGenRenderbuffers()
        glBindRenderbuffer(GL_RENDERBUFFER, renderBufferId)
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, textureWidth, textureHeight)
        glFramebufferRenderbuffer(
            GL_FRAMEBUFFER,
            GL_DEPTH_ATTACHMENT,
            GL_RENDERBUFFER,
            renderBufferId
        )

        glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, textureId, 0)
        glDrawBuffer(GL_COLOR_ATTACHMENT0)
        glBindFramebuffer(GL_FRAMEBUFFER, 0)

        return FBOImage(textureId, textureWidth, textureHeight)
    }

    fun render() {
        glBindFramebuffer(GL_FRAMEBUFFER, id)
        glViewport(0, 0, textureWidth, textureHeight)

        glClearColor(BG_COLOUR, BG_COLOUR, BG_COLOUR, 1f)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT or GL_STENCIL_BUFFER_BIT)

        glEnable(GL_DEPTH_TEST)
        glEnable(GL_CULL_FACE)
        glCullFace(GL_BACK)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        if (polygonMode == PolygonMode.POINT) {
            glPolygonMode(GL_FRONT_AND_BACK, GL_POINT)
        } else if (polygonMode == PolygonMode.LINE) {
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE)
        }

        // Render entities
        context.animationHandler.tickAnimation()
        camera.move()
        shader.start()
        shader.loadViewMatrix(camera)
        shader.loadShadingToggle(shadingType != ShadingType.NONE)
        glRenderer.render(context.entities, shader)
        shader.stop()

        glBindFramebuffer(GL_FRAMEBUFFER, 0)
    }

    fun resize() {
        size = getFboSize()
        textureWidth = (size.x * scaleFactor).toInt()
        textureHeight = (size.y * scaleFactor).toInt()
        image = createTexture()

        if (::glRenderer.isInitialized) {
            glRenderer.reloadProjectionMatrix()
        }
    }

    private fun getFboSize(): Vector2f {
        return Vector2f(context.gui.size.x - 340, context.gui.size.y - 159)
    }
}