package gizmo

import model.Model
import org.joml.*
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.glDrawArrays
import org.lwjgl.opengl.GL20.glEnableVertexAttribArray
import org.lwjgl.opengl.GL30.glBindVertexArray
import render.Loader
import render.RenderContext
import shader.GizmoShader
import util.MatrixCreator
import kotlin.math.sqrt

abstract class Gizmo(private val context: RenderContext, private val shader: GizmoShader) {

    internal open var scale = 1f
    internal open lateinit var model: Model
    internal open lateinit var axes: Array<GizmoAxis>

    var active = false
    var position = Vector3f()
    internal var selectedAxis: GizmoAxis? = null

    internal fun getModel(filename: String, loader: Loader) = GizmoLoader.load(filename, loader)

    internal fun getRelativeScale() = context.entityHandler.entity?.let { scale * sqrt(it.size.toFloat()) } ?: 0f

    private fun prepare(context: RenderContext, model: Model, shader: GizmoShader, viewMatrix: Matrix4f) {
        glBindVertexArray(model.vaoId)
        glEnableVertexAttribArray(0)
        shader.loadViewMatrix(viewMatrix)
        shader.loadProjectionMatrix(context.projectionMatrix)
    }

    fun render(viewMatrix: Matrix4f, ray: Rayf) {
        prepare(context, model, shader, viewMatrix)

        when {
            !active -> selectAxis(getClosestAxis(ray))
            else -> manipulate(ray)
        }

        // Render gizmo axes
        for (axis in axes.reversed()) {
            if (active && this is RotationGizmo && axis != selectedAxis) { // Only render selected rotation axis
                continue
            }

            val transformation = MatrixCreator.createTransformationMatrix(position, axis.rotation, getRelativeScale())
            shader.loadTransformationMatrix(transformation)

            axis.colour.w = if (axis == selectedAxis) 0.6f else 1f // Lower opacity to indicate highlighted axis
            shader.loadColour(axis.colour)
            glDrawArrays(GL11.GL_TRIANGLES, 0, model.vertexCount)
        }
    }

    abstract fun getClosestAxis(ray: Rayf): GizmoAxis?

    private fun selectAxis(axis: GizmoAxis?) {
        if (axis != selectedAxis) {
            selectedAxis = axis
            selectedAxis?.previousIntersection = Vector3f() // Reset intersection
        }
    }

    abstract fun manipulate(ray: Rayf)

    internal fun getPlaneIntersection(ray: Rayf): Vector3f {
        // Allow for transforming without need to hover over axis
        val plane = Planef(Vector3f(position), Vector3f(-ray.dX, -ray.dY, -ray.dZ))
        val epsilon = Intersectionf.intersectRayPlane(ray, plane, 0f)

        // Origin + direction * epsilon
        return Vector3f(ray.oX, ray.oY, ray.oZ).add(Vector3f(ray.dX, ray.dY, ray.dZ).mul(epsilon))
    }

    internal fun deactivate() {
        active = false
        selectedAxis = null
    }
}