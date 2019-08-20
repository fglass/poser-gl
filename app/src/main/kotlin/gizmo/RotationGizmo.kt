package gizmo

import model.Model
import org.joml.Matrix4f
import org.joml.Rayf
import org.joml.Vector3f
import org.liquidengine.legui.style.color.ColorUtil
import org.lwjgl.opengl.GL11.GL_TRIANGLES
import org.lwjgl.opengl.GL11.glDrawArrays
import render.Loader
import render.RenderContext
import shader.GizmoShader
import util.MatrixCreator

class RotationGizmo(loader: Loader, private val shader: GizmoShader): Gizmo() {

    private val scale = 30f
    private val model: Model = getModel("rotation", loader)
    private val axes = arrayOf(
        GizmoAxis(AxisType.X, ColorUtil.fromInt(220, 14, 44, 1f), Vector3f(0f, 0f, 90f)),
        GizmoAxis(AxisType.Y, ColorUtil.fromInt(14, 220, 44, 1f), Vector3f(0f, 0f, 0f)),
        GizmoAxis(AxisType.Z, ColorUtil.fromInt(14, 44, 220, 1f), Vector3f(90f, 0f, 0f))
    )

    override fun render(context: RenderContext, viewMatrix: Matrix4f, ray: Rayf) {
        prepare(context, model, shader, viewMatrix)

        for (axis in axes) {
            val transformation = MatrixCreator.createTransformationMatrix(position, axis.rotation, scale)
            shader.loadTransformationMatrix(transformation)

            axis.colour.w = if (axis == selectedAxis) 0.6f else 1f // Lower opacity to indicate highlighted axis
            shader.loadColour(axis.colour)
            glDrawArrays(GL_TRIANGLES, 0, model.vertexCount)
        }
    }
}