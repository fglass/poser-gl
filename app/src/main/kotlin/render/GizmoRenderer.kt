package render

import animation.ReferenceNode
import animation.TransformationType
import entity.Camera
import gizmo.Gizmo
import org.joml.Vector2f
import org.liquidengine.legui.event.MouseClickEvent
import org.liquidengine.legui.input.Mouse
import org.lwjgl.opengl.GL30.*
import shader.GizmoShader

class GizmoRenderer(private val context: RenderContext) {

    var enabled = false
    var transforming = false
    private val loader = Loader()
    private val shader = GizmoShader()
    private val translation = Gizmo("translation", loader, shader)

    fun enable(node: ReferenceNode, type: TransformationType) {
        translation.position = node.position
        enabled = true
    }

    fun render(camera: Camera) {
        if (enabled) {
            prepare()
            translation.render(context, camera)
            finish()
        }
    }

    private fun prepare() {
        shader.start()
        glDisable(GL_DEPTH_TEST)
    }

    private fun finish() {
        glEnable(GL_DEPTH_TEST)
        glDisableVertexAttribArray(0)
        glBindVertexArray(0)
        shader.stop()
    }

    fun handleDrag(delta: Vector2f) { // TODO: use ray instead
        if (transforming) {
            translation.transform(delta, context)
        }
    }

    fun handleClick(button: Mouse.MouseButton, action: MouseClickEvent.MouseClickAction) {
        if (!enabled) {
            return
        }
        if (button == Mouse.MouseButton.MOUSE_BUTTON_LEFT) {
            if (action == MouseClickEvent.MouseClickAction.PRESS) {
                transforming = true
            } else if (action == MouseClickEvent.MouseClickAction.RELEASE) {
                transforming = false
                translation.endTransform()
            }
        }
    }

    fun cleanUp() {
        loader.cleanUp()
        shader.cleanUp()
    }
}