package render

import animation.ReferenceNode
import animation.TransformationType
import gizmo.TranslationGizmo
import org.joml.Matrix4f
import org.joml.Rayf
import org.lwjgl.opengl.GL30.*
import shader.GizmoShader
import util.MouseHandler

class GizmoRenderer(private val context: RenderContext, private val mouse: MouseHandler) {

    var enabled = false
    private val loader = Loader()
    private val shader = GizmoShader()
    val gizmo = TranslationGizmo(loader, shader)

    fun enable(node: ReferenceNode, type: TransformationType) {
        gizmo.position = node.position
        enabled = true
    }

    fun render(viewMatrix: Matrix4f, ray: Rayf) {
        if (enabled) {
            prepare()
            if (mouse.pressed) gizmo.active = true else gizmo.deactivate()
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