package render

import animation.ReferenceNode
import animation.TransformationType
import gizmo.TranslationGizmo
import org.joml.Matrix4f
import org.joml.Rayf
import org.liquidengine.legui.event.MouseClickEvent
import org.liquidengine.legui.input.Mouse
import org.lwjgl.opengl.GL30.*
import shader.GizmoShader

class GizmoRenderer(private val context: RenderContext) {

    var enabled = false
    private val loader = Loader()
    private val shader = GizmoShader()
    val gizmo = TranslationGizmo(loader, shader)

    fun enable(node: ReferenceNode, type: TransformationType) {
        gizmo.position = node.position
        enabled = true
    }

    fun handleClick(button: Mouse.MouseButton, action: MouseClickEvent.MouseClickAction) {
        if (enabled && button == Mouse.MouseButton.MOUSE_BUTTON_LEFT) {
            if (action == MouseClickEvent.MouseClickAction.PRESS) {
                gizmo.active = true
            } else if (action == MouseClickEvent.MouseClickAction.RELEASE) {
                gizmo.deactivate()
            }
        }
    }

    fun render(viewMatrix: Matrix4f, ray: Rayf) {
        if (enabled) {
            prepare()
            gizmo.render(context, viewMatrix, ray)
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

    fun cleanUp() {
        loader.cleanUp()
        shader.cleanUp()
    }
}