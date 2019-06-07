package render

import BG_COLOUR
import entity.Camera
import org.joml.Vector2f
import org.liquidengine.legui.component.ImageView
import org.liquidengine.legui.image.FBOImage
import org.lwjgl.opengl.GL30
import org.lwjgl.opengl.GL32
import shader.StaticShader
import shader.ShadingType
import Processor
import input.Mouse
import org.liquidengine.legui.event.MouseClickEvent
import org.liquidengine.legui.event.MouseDragEvent
import org.liquidengine.legui.event.ScrollEvent

class Framebuffer(private val context: Processor, private val shader: StaticShader,
                  mouse: Mouse, private val scaleFactor: Int): ImageView() {

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
    }

    fun lateInit() {
        glRenderer = Renderer(context, shader)
    }

    private fun getFboSize(): Vector2f {
        return Vector2f(context.gui.size.x - 340, context.gui.size.y - 159)
    }

    private fun createTexture(): FBOImage {
        id = GL30.glGenFramebuffers()
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, id)

        textureId = GL30.glGenTextures()
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, textureId)

        GL30.glTexImage2D(
            GL30.GL_TEXTURE_2D,
            0,
            GL30.GL_RGBA,
            textureWidth,
            textureHeight,
            0,
            GL30.GL_RGBA,
            GL30.GL_UNSIGNED_BYTE,
            0
        )

        GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_NEAREST)
        GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_NEAREST)

        val renderBufferId = GL30.glGenRenderbuffers()
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, renderBufferId)
        GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL30.GL_DEPTH_COMPONENT, textureWidth, textureHeight)
        GL30.glFramebufferRenderbuffer(
            GL30.GL_FRAMEBUFFER,
            GL30.GL_DEPTH_ATTACHMENT,
            GL30.GL_RENDERBUFFER,
            renderBufferId
        )

        GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, textureId, 0)
        GL30.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0)
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0)

        return FBOImage(textureId, textureWidth, textureHeight)
    }

    fun render() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, id)
        GL30.glViewport(0, 0, textureWidth, textureHeight)

        GL30.glClearColor(BG_COLOUR, BG_COLOUR, BG_COLOUR, 1f)
        GL30.glClear(GL30.GL_COLOR_BUFFER_BIT or GL30.GL_DEPTH_BUFFER_BIT or GL30.GL_STENCIL_BUFFER_BIT)

        GL30.glEnable(GL30.GL_DEPTH_TEST)
        GL30.glEnable(GL30.GL_CULL_FACE)
        GL30.glCullFace(GL30.GL_BACK)
        GL30.glEnable(GL30.GL_BLEND)
        GL30.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA)

        if (polygonMode == PolygonMode.POINT) {
            GL30.glPolygonMode(GL30.GL_FRONT_AND_BACK, GL30.GL_POINT)
        } else if (polygonMode == PolygonMode.LINE) {
            GL30.glPolygonMode(GL30.GL_FRONT_AND_BACK, GL30.GL_LINE)
        }

        // Render entities
        context.animationHandler.tickAnimation()
        camera.move()
        shader.start()
        shader.loadViewMatrix(camera)
        shader.loadShadingToggle(shadingType != ShadingType.NONE)
        glRenderer.render(context.entities, shader)
        shader.stop()

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0)
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
}