package render

import BG_COLOUR
import Processor
import entity.Camera
import io.MouseHandler
import org.joml.Vector2f
import org.liquidengine.legui.component.ImageView
import org.liquidengine.legui.event.*
import org.liquidengine.legui.image.FBOImage
import org.liquidengine.legui.style.Style
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL32.*
import shader.ShadingType
import shader.StaticShader

class Framebuffer(private val context: Processor, private val shader: StaticShader,
                  private val mouse: MouseHandler, private val scaleFactor: Int): ImageView() {

    private var id: Int = 0
    private var textureId: Int = 0
    private var textureWidth = 0
    private var textureHeight = 0

    val entityRenderer = EntityRenderer(shader)
    var polygonMode = PolygonMode.FILL
    var shadingType = ShadingType.SMOOTH
    private val camera = Camera(mouse)

    init {
        style.setMargin(5f, 0f, 5f, 0f)
        style.position = Style.PositionType.RELATIVE
        style.flexStyle.flexGrow = 1
        style.focusedStrokeColor = null

        listenerMap.addListener(MouseClickEvent::class.java) { event ->
            mouse.handleClick(event.button, event.action)
            context.nodeRenderer.handleClick(event.button, event.action)
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
        if (id == 0 || !context.cacheService.loaded) {
            return
        }

        glBindFramebuffer(GL_FRAMEBUFFER, id)
        glViewport(0, 0, textureWidth, textureHeight)

        glClearColor(BG_COLOUR.x, BG_COLOUR.y, BG_COLOUR.z, BG_COLOUR.w)
        glClear(GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT or GL_STENCIL_BUFFER_BIT)
        setGlState()

        context.animationHandler.tick()
        camera.move()

        renderEntity()
        context.nodeRenderer.render(camera)
        context.planeRenderer.render(camera)

        context.lineRenderer.render(context.nodeRenderer.nodes.toList(), camera)
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
    }

    private fun setGlState() {
        glEnable(GL_DEPTH_TEST)
        glEnable(GL_CULL_FACE)
        glCullFace(GL_FRONT)
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
        entityRenderer.render(context.entity, shader)
        shader.stop()
    }

    override fun setSize(width: Float, height: Float) {
        val previous = size.x
        super.setSize(width, height)
        if (previous != width) {
            resize(width.toInt(), height.toInt())
        }
    }

    private fun resize(width: Int, height: Int) {
        setTexture(width, height)
        entityRenderer.init(width, height)
    }

    private fun setTexture(width: Int, height: Int) {
        textureWidth = width * scaleFactor
        textureHeight = height * scaleFactor
        image = createTexture()
    }
}