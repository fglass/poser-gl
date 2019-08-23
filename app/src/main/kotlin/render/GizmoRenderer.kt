package render

import animation.ReferenceNode
import animation.TransformationType
import gizmo.Gizmo
import gizmo.TranslationGizmo
import gizmo.RotationGizmo
import org.joml.Matrix4f
import org.joml.Rayf
import org.lwjgl.opengl.GL30.*
import shader.GizmoShader
import util.MouseHandler

class GizmoRenderer(private val context: RenderContext, private val mouse: MouseHandler) {

    private val loader = Loader()
    private val shader = GizmoShader()

    var gizmo: Gizmo? = null
    private val translationGizmo = TranslationGizmo(loader, shader)
    private val rotationGizmo = RotationGizmo(loader, shader)

    fun enable(node: ReferenceNode, type: TransformationType) {
        gizmo = when (type) {
            TransformationType.REFERENCE -> translationGizmo
            TransformationType.TRANSLATION -> translationGizmo
            TransformationType.ROTATION -> rotationGizmo
            TransformationType.SCALE -> translationGizmo
        }
        gizmo?.position = node.position
        (gizmo as? RotationGizmo)?.reset() // TODO: refactor
    }

    fun render(viewMatrix: Matrix4f, ray: Rayf) {
        gizmo?.let {
            prepare()
            if (mouse.pressed) it.active = true else it.deactivate()
            it.render(context, viewMatrix, ray)
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