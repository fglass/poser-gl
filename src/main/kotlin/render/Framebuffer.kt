package render

import BG_COLOUR
import Processor
import animation.node.NodeRenderer
import entity.Camera
import input.MouseHandler
import org.joml.Vector2f
import org.liquidengine.legui.component.ImageView
import org.liquidengine.legui.event.CursorEnterEvent
import org.liquidengine.legui.event.MouseClickEvent
import org.liquidengine.legui.event.MouseDragEvent
import org.liquidengine.legui.event.ScrollEvent
import org.liquidengine.legui.image.FBOImage
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30
import org.lwjgl.opengl.GL32.*
import shader.ShadingType
import shader.StaticShader

class Framebuffer(private val context: Processor, private val shader: StaticShader,
                  private val mouse: MouseHandler, private val scaleFactor: Int): ImageView() {

    private var id: Int = 0
    private var textureId: Int = 0
    private var textureWidth = 0
    private var textureHeight = 0

    private lateinit var glRenderer: GlRenderer
    lateinit var nodeRenderer: NodeRenderer
    private val camera = Camera(mouse)

    var polygonMode = PolygonMode.FILL
    var shadingType = ShadingType.SMOOTH

    init {
        position = Vector2f(174f, 5f)
        resize()

        listenerMap.addListener(MouseClickEvent::class.java) { event ->
            mouse.handleClick(event.button, event.action)
            nodeRenderer.handleClick(event.button, event.action)
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
        style.focusedStrokeColor = null
    }

    fun lateInit() {
        glRenderer = GlRenderer(context, shader)
        nodeRenderer = NodeRenderer(context, glRenderer.projectionMatrix, size, position)
    }

    private fun createTexture(): FBOImage {
        id = glGenFramebuffers()
        glBindFramebuffer(GL_FRAMEBUFFER, id)

        textureId = glGenTextures()
        glBindTexture(GL_TEXTURE_2D, textureId)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, textureWidth, textureHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0)

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

        glClearColor(BG_COLOUR.x, BG_COLOUR.y, BG_COLOUR.z, BG_COLOUR.w)
        glClear(GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT or GL_STENCIL_BUFFER_BIT)
        setGlState()

        context.animationHandler.tick()
        camera.move()
        renderEntity()
        nodeRenderer.render(camera)

        glBindFramebuffer(GL_FRAMEBUFFER, 0)
    }

    private fun setGlState() {
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
    }

    private fun renderEntity() {
        shader.start()
        shader.loadViewMatrix(camera)
        shader.loadShadingToggle(shadingType != ShadingType.NONE)
        glRenderer.render(context.entity, shader)
        shader.stop()
    }

    fun resize() {
        size = getFboSize()
        textureWidth = (size.x * scaleFactor).toInt()
        textureHeight = (size.y * scaleFactor).toInt()
        image = createTexture()

        if (::glRenderer.isInitialized) {
            glRenderer.reloadProjectionMatrix()
            nodeRenderer.resize(glRenderer.projectionMatrix, size, position)
        }
    }

    private fun getFboSize(): Vector2f {
        return Vector2f(context.gui.size.x - 354, context.gui.size.y - 128)
    }
}