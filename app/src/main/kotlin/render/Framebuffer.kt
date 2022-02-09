package render

import entity.Camera
import gui.component.Dialog
import util.MouseButtonHandler
import com.spinyowl.legui.component.ImageView
import com.spinyowl.legui.event.*
import com.spinyowl.legui.image.FBOImage
import com.spinyowl.legui.input.Mouse
import com.spinyowl.legui.style.Style
import org.lwjgl.opengl.GL32.*
import shader.ShadingType
import util.MatrixCreator

class Framebuffer(
    private val context: RenderContext,
    private val scaleFactor: Int,
    private val buttons: Array<MouseButtonHandler>
): ImageView() {

    private var id: Int = 0
    private var textureId: Int = 0
    private var textureWidth = 0
    private var textureHeight = 0
    
    private val camera = Camera(context.settingsManager, lmb=buttons[0], mmb=buttons[1], rmb=buttons[2])
    var polygonMode = PolygonMode.FILL
    var shadingType = ShadingType.SMOOTH
    var activeDialog: Dialog? = null

    init {
        style.setMargin(5f, 0f, 5f, 0f)
        style.position = Style.PositionType.RELATIVE
        style.flexStyle.flexGrow = 1
        style.focusedStrokeColor = null

        listenerMap.addListener(MouseClickEvent::class.java) { event ->
            buttons.forEach { it.handleClick(event.button, event.action) }
        }
        listenerMap.addListener(MouseDragEvent::class.java) { event ->
            buttons.forEach {
                // Prevent camera pan when transforming gizmo
                if (it.button != Mouse.MouseButton.MOUSE_BUTTON_LEFT || context.gizmoRenderer.gizmo?.selectedAxis == null) {
                    it.handleDrag(event.delta)
                }
            }
        }
        listenerMap.addListener(ScrollEvent::class.java) { event ->
            camera.handleScroll(event.yoffset)
        }
        listenerMap.addListener(CursorEnterEvent::class.java) { event ->
            buttons.forEach { it.handleCursorEvent(event.isEntered) }
        }
    }

    private fun createTexture(): FBOImage { // TODO: multisample
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

        val colour = context.settingsManager.background.rgba
        glClearColor(colour.x, colour.y, colour.z, colour.w)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT or GL_STENCIL_BUFFER_BIT)
        setGlState()

        context.animationHandler.tick()
        camera.tick()

        val viewMatrix = MatrixCreator.createViewMatrix(camera)
        val ray = camera.calculateRay(context, viewMatrix)
        camera.pan(viewMatrix)

        context.entityRenderer.render(context.entityHandler.entity, viewMatrix, shadingType)
        context.nodeRenderer.render(viewMatrix, ray)
        context.lineRenderer.renderGrid(viewMatrix)
        context.nodeRenderer.renderSelected(viewMatrix, ray)

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

    override fun setSize(width: Float, height: Float) {
        val previous = size.x
        super.setSize(width, height)
        if (previous != width) {
            resize(width.toInt(), height.toInt())
        }
    }

    private fun resize(width: Int, height: Int) {
        setTexture(width, height)
        context.projectionMatrix = MatrixCreator.createProjectionMatrix(width, height)
        activeDialog?.center(this)
    }

    private fun setTexture(width: Int, height: Int) {
        textureWidth = width * scaleFactor
        textureHeight = height * scaleFactor
        image = createTexture()
    }
}