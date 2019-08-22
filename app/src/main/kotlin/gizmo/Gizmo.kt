package gizmo

import model.Model
import org.joml.Matrix4f
import org.joml.Rayf
import org.joml.Vector3f
import org.lwjgl.opengl.GL20.glEnableVertexAttribArray
import org.lwjgl.opengl.GL30.glBindVertexArray
import render.Loader
import render.RenderContext
import shader.GizmoShader

abstract class Gizmo { // TODO: deduplicate subclasses

    var active = false
    var position = Vector3f(0f, 0f, 0f)
    internal var selectedAxis: GizmoAxis? = null

    internal fun getModel(filename: String, loader: Loader) = GizmoLoader.load(filename, loader)

    internal fun prepare(context: RenderContext, model: Model, shader: GizmoShader, viewMatrix: Matrix4f) {
        glBindVertexArray(model.vaoId)
        glEnableVertexAttribArray(0)
        shader.loadViewMatrix(viewMatrix)
        shader.loadProjectionMatrix(context.entityRenderer.projectionMatrix)
    }

    abstract fun render(context: RenderContext, viewMatrix: Matrix4f, ray: Rayf)

    internal fun deactivate() {
        active = false
        selectedAxis = null
    }
}