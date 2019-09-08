package render

import entity.Camera
import gui.component.Dialog
import util.MouseHandler
import org.liquidengine.legui.component.ImageView
import org.liquidengine.legui.event.*
import org.liquidengine.legui.image.FBOImage
import org.liquidengine.legui.style.Style
import org.lwjgl.opengl.GL32.*
import shader.ShadingType
import util.MatrixCreator

class Framebuffer(private val context: RenderContext, private val lmb: MouseHandler, private val rmb: MouseHandler,
                  private val scaleFactor: Int): ImageView() {

    private var id: Int = 0
    private var textureId: Int = 0
    private var textureWidth = 0
    private var textureHeight = 0
    
    private val camera = Camera(lmb, rmb)
    var polygonMode = PolygonMode.FILL
    var shadingType = ShadingType.SMOOTH
    var activeDialog: Dialog? = null

    init {
        style.setMargin(5f, 0f, 5f, 0f)
        style.position = Style.PositionType.RELATIVE
        style.flexStyle.flexGrow = 1
        style.focusedStrokeColor = null

        listenerMap.addListener(MouseClickEvent::class.java) { event ->
            lmb.handleClick(event.button, event.action)
            rmb.handleClick(event.button, event.action)
        }
        listenerMap.addListener(MouseDragEvent::class.java) { event ->
            if (context.gizmoRenderer.gizmo?.selectedAxis == null) { // Prevent camera pan when transforming gizmo
                lmb.handleDrag(event.delta)
            }
            rmb.handleDrag(event.delta)
        }
        listenerMap.addListener(ScrollEvent::class.java) { event ->
            lmb.handleScroll(event.yoffset)
            rmb.handleScroll(event.yoffset)
        }
        listenerMap.addListener(CursorEnterEvent::class.java) { event ->
            lmb.handleCursorEvent(event.isEntered)
            rmb.handleCursorEvent(event.isEntered)
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

        glClearColor(BG_COLOUR.x, BG_COLOUR.y, BG_COLOUR.z, BG_COLOUR.w)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT or GL_STENCIL_BUFFER_BIT)
        setGlState()

        context.animationHandler.tick()
        camera.tick()

        val viewMatrix = MatrixCreator.createViewMatrix(camera)
        val ray = camera.calculateRay(context, viewMatrix)
        camera.pan(viewMatrix)

        context.entityRenderer.render(context.entity, viewMatrix, shadingType)
        context.nodeRenderer.render(viewMatrix, ray)
        context.lineRenderer.renderGrid(viewMatrix)
        context.nodeRenderer.renderSelected(viewMatrix, ray)
        lmb.clicked = false

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
        context.entityRenderer.init(width, height)
        activeDialog?.center(this)
    }

    private fun setTexture(width: Int, height: Int) {
        textureWidth = width * scaleFactor
        textureHeight = height * scaleFactor
        image = createTexture()
    }
}