package render

import animation.ReferenceNode
import animation.TransformationType
import gizmo.Gizmo
import gizmo.ReferenceGizmo
import gizmo.TranslationGizmo
import gizmo.RotationGizmo
import gizmo.ScaleGizmo
import org.joml.Matrix4f
import org.joml.Rayf
import org.joml.Vector2f
import org.lwjgl.opengl.GL30.*
import shader.GizmoShader
import util.MouseHandler

class GizmoRenderer(context: RenderContext, private val mouse: MouseHandler) {

    private val loader = Loader()
    private val shader = GizmoShader()

    var gizmo: Gizmo? = null
    private val referenceGizmo = ReferenceGizmo(context, loader, shader)
    private val translationGizmo = TranslationGizmo(context, loader, shader)
    private val rotationGizmo = RotationGizmo(context, loader, shader)
    private val scaleGizmo = ScaleGizmo(context, loader, shader)

    fun enable(node: ReferenceNode, type: TransformationType) {
        gizmo = when (type) {
            TransformationType.REFERENCE -> referenceGizmo
            TransformationType.TRANSLATION -> translationGizmo
            TransformationType.ROTATION -> rotationGizmo
            TransformationType.SCALE -> scaleGizmo
        }
        gizmo?.position = node.position
        (gizmo as? RotationGizmo)?.reset() // TODO: refactor
    }

    fun render(node: ReferenceNode, viewMatrix: Matrix4f, ray: Rayf) {
        gizmo?.let {
            prepare()
            //it.position = node.position
            if (mouse.pressed) it.active = true else it.deactivate()
            it.render(viewMatrix, ray)
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

    fun reset() {
        gizmo = null
    }

    fun cleanUp() {
        loader.cleanUp()
        shader.cleanUp()
    }
}