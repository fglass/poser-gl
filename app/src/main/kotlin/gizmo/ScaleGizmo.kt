package gizmo

import render.Loader
import shader.GizmoShader

class ScaleGizmo(loader: Loader, shader: GizmoShader): TranslationGizmo(loader, shader) {
    override var model = getModel("scale", loader)
}