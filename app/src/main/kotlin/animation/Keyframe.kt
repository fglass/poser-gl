package animation

import api.animation.IKeyframe
import api.definition.FrameMapDefinition
import api.definition.ModelDefinition
import api.definition.ModelDefinition.Companion.animOffsetX
import api.definition.ModelDefinition.Companion.animOffsetY
import api.definition.ModelDefinition.Companion.animOffsetZ
import entity.Entity
import render.RenderContext
import shader.ShadingType

class Keyframe(
    val id: Int = -1,
    val frameId: Int = -1,
    var length: Int = -1,
    override val frameMap: FrameMapDefinition = FrameMapDefinition()
) : IKeyframe {

    // Copy constructor
    constructor(newId: Int, keyframe: Keyframe): this(newId, keyframe.frameId, keyframe.length, keyframe.frameMap) {
        modified = keyframe.modified
        rootNode = keyframe.rootNode
        keyframe.transformations.forEach {
            if (it is ReferenceNode) {
                val newReference = ReferenceNode(it)
                transformations.add(newReference)
                newReference.children.forEach { child -> transformations.add(child.value) }
            }
        }
    }

    override var modified = false
    override val transformations = ArrayList<Transformation>()
    var rootNode: ReferenceNode? = null

    fun buildSkeleton() {
        val references = transformations.filterIsInstance<ReferenceNode>()

        for (reference in references) {

            trySetRootNode(reference)

            for (other in references) {
                reference.trySetParent(other)
            }
        }

        tryConnectSections(references)
    }

    private fun trySetRootNode(reference: ReferenceNode) {
        val rotation = reference.getRotation() ?: return

        if (rootNode == null) {
            rootNode = reference
            return
        }

        val rootRotation = rootNode?.getRotation() ?: return

        if (rotation.frameMap.size > rootRotation.frameMap.size) {
            rootNode = reference
        }
    }

    private fun tryConnectSections(references: List<ReferenceNode>) {
        if (rootNode == null) {
            return
        }

        val maxId = references.maxOfOrNull(ReferenceNode::id) ?: return
        val sectionChildren = IntArray(maxId + 1) { 0 }
        val sectionRoots = mutableListOf<ReferenceNode>()

        for (reference in references) {

            val parentId = reference.parent?.id ?: continue

            sectionChildren[parentId] += 1

            if (parentId == rootNode?.id) {
                sectionRoots.add(reference)
            }
        }

        val mainSectionRoot = sectionRoots.maxByOrNull { sectionChildren[it.id] } ?: return

        for (root in sectionRoots) {
            if (root.id != mainSectionRoot.id && sectionChildren[root.id] > 0) {
                root.parent = mainSectionRoot
            }
        }
    }

    fun apply(context: RenderContext) {
        context.nodeRenderer.nodes.clear()

        val entity = context.entityHandler.entity ?: return
        val def = entity.model.definition
        def.resetTransformations()

        transformations.forEach { it.apply(def) }

        transformations.filterIsInstance<ReferenceNode>().forEach {
            context.nodeRenderer.addNode(it, def) // After all transformations applied
        }

        loadTransformedModel(context, entity, def)
    }

    private fun ModelDefinition.resetTransformations() {
        animOffsetX = 0
        animOffsetY = 0
        animOffsetZ = 0

        origVX?.let { System.arraycopy(it, 0, this.vertexPositionsX, 0, it.size) }
        origVY?.let { System.arraycopy(it, 0, this.vertexPositionsY, 0, it.size) }
        origVZ?.let { System.arraycopy(it, 0, this.vertexPositionsZ, 0, it.size) }
    }

    private fun loadTransformedModel(context: RenderContext, entity: Entity, def: ModelDefinition) {
        context.modelParser.cleanUp()
        val useFlatShading = context.framebuffer.shadingType == ShadingType.FLAT
        entity.model = context.modelParser.parse(def, useFlatShading)
    }
}