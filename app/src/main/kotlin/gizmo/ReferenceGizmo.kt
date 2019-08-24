package gizmo

import org.joml.Vector3f
import org.liquidengine.legui.style.color.ColorUtil
import render.Loader
import shader.GizmoShader

class ReferenceGizmo(loader: Loader, shader: GizmoShader): TranslationGizmo(loader, shader) {
    override var axes = arrayOf(
        GizmoAxis(AxisType.X, ColorUtil.fromInt(220, 14, 44, 1f), Vector3f(0f, 180f, 0f)),
        GizmoAxis(AxisType.Y, ColorUtil.fromInt(221, 215, 0, 1f), Vector3f(0f, 0f, -90f)),
        GizmoAxis(AxisType.Z, ColorUtil.fromInt(14, 44, 220, 1f), Vector3f(0f, 90f, 0f))
    )
}