package gizmo

import render.Loader
import render.RenderContext
import shader.GizmoShader

class ScaleGizmo(context: RenderContext, loader: Loader, shader: GizmoShader):
      TranslationGizmo(context, loader, shader) {
    override var model = getModel("scale", loader)
}