package animation

import api.animation.ITransformation
import api.animation.TransformationType
import api.definition.ModelDefinition
import net.runelite.cache.models.CircularAngle
import org.joml.Vector3i

open class Transformation(
    override var id: Int,
    override val type: TransformationType,
    var frameMap: IntArray,
    override var delta: Vector3i
) : ITransformation {

    constructor(transformation: Transformation): this(
        transformation.id, transformation.type,
        transformation.frameMap, Vector3i(transformation.delta)
    )

    fun apply(def: ModelDefinition) {
        def.animate(type.id, frameMap, delta.x, delta.y, delta.z)
    }

    private fun ModelDefinition.animate(type: Int, frameMap: IntArray, dx: Int, dy: Int, dz: Int) { // From deob
        if (this.origVX == null) {
            this.origVX = this.vertexPositionsX.copyOf(this.vertexPositionsX.size)
            this.origVY = this.vertexPositionsY.copyOf(this.vertexPositionsY.size)
            this.origVZ = this.vertexPositionsZ.copyOf(this.vertexPositionsZ.size)
        }

        val verticesX = this.vertexPositionsX
        val verticesY = this.vertexPositionsY
        val verticesZ = this.vertexPositionsZ

        val frameMapSize = frameMap.size
        var count: Int
        var vGroupIndex: Int
        var vIndex: Int
        var var12: Int

        if (type == TransformationType.REFERENCE.id) {
            ModelDefinition.animOffsetX = 0
            ModelDefinition.animOffsetY = 0
            ModelDefinition.animOffsetZ = 0
            vGroupIndex = 0
            count = 0

            while (vGroupIndex < frameMapSize) {
                val var9 = frameMap[vGroupIndex]
                if (var9 < this.vertexGroups.size) {
                    val vertexGroup = this.vertexGroups[var9]
                    vIndex = 0
                    while (vIndex < vertexGroup.size) {
                        var12 = vertexGroup[vIndex]
                        ModelDefinition.animOffsetX += verticesX[var12]
                        ModelDefinition.animOffsetY += verticesY[var12]
                        ModelDefinition.animOffsetZ += verticesZ[var12]
                        ++count
                        ++vIndex
                    }
                }
                ++vGroupIndex
            }
            if (count > 0) {
                ModelDefinition.animOffsetX = dx + ModelDefinition.animOffsetX / count
                ModelDefinition.animOffsetY = dy + ModelDefinition.animOffsetY / count
                ModelDefinition.animOffsetZ = dz + ModelDefinition.animOffsetZ / count
            } else {
                ModelDefinition.animOffsetX = dx
                ModelDefinition.animOffsetY = dy
                ModelDefinition.animOffsetZ = dz
            }
        } else {
            var vertexGroup: IntArray
            var var19: Int
            if (type == TransformationType.TRANSLATION.id) {
                count = 0
                while (count < frameMapSize) {
                    vGroupIndex = frameMap[count]
                    if (vGroupIndex < this.vertexGroups.size) {
                        vertexGroup = this.vertexGroups[vGroupIndex]
                        var19 = 0
                        while (var19 < vertexGroup.size) {
                            vIndex = vertexGroup[var19]
                            verticesX[vIndex] += dx
                            verticesY[vIndex] += dy
                            verticesZ[vIndex] += dz
                            ++var19
                        }
                    }
                    ++count
                }
            } else if (type == TransformationType.ROTATION.id) {
                count = 0
                while (count < frameMapSize) {
                    vGroupIndex = frameMap[count]
                    if (vGroupIndex < this.vertexGroups.size) {
                        vertexGroup = this.vertexGroups[vGroupIndex]
                        var19 = 0
                        while (var19 < vertexGroup.size) {
                            vIndex = vertexGroup[var19]
                            verticesX[vIndex] -= ModelDefinition.animOffsetX
                            verticesY[vIndex] -= ModelDefinition.animOffsetY
                            verticesZ[vIndex] -= ModelDefinition.animOffsetZ

                            var12 = (dx and 255) * 8
                            val yAngle = (dy and 255) * 8
                            val zAngle = (dz and 255) * 8

                            var sin: Int
                            var cos: Int
                            var temp: Int

                            if (zAngle != 0) {
                                sin = CircularAngle.SINE[zAngle]
                                cos = CircularAngle.COSINE[zAngle]
                                temp = sin * verticesY[vIndex] + cos * verticesX[vIndex] shr 16
                                verticesY[vIndex] = cos * verticesY[vIndex] - sin * verticesX[vIndex] shr 16
                                verticesX[vIndex] = temp
                            }
                            if (var12 != 0) {
                                sin = CircularAngle.SINE[var12]
                                cos = CircularAngle.COSINE[var12]
                                temp = cos * verticesY[vIndex] - sin * verticesZ[vIndex] shr 16
                                verticesZ[vIndex] = sin * verticesY[vIndex] + cos * verticesZ[vIndex] shr 16
                                verticesY[vIndex] = temp
                            }
                            if (yAngle != 0) {
                                sin = CircularAngle.SINE[yAngle]
                                cos = CircularAngle.COSINE[yAngle]
                                temp = sin * verticesZ[vIndex] + cos * verticesX[vIndex] shr 16
                                verticesZ[vIndex] = cos * verticesZ[vIndex] - sin * verticesX[vIndex] shr 16
                                verticesX[vIndex] = temp
                            }

                            verticesX[vIndex] += ModelDefinition.animOffsetX
                            verticesY[vIndex] += ModelDefinition.animOffsetY
                            verticesZ[vIndex] += ModelDefinition.animOffsetZ
                            ++var19
                        }
                    }
                    ++count
                }
            } else if (type == TransformationType.SCALE.id) {
                count = 0
                while (count < frameMapSize) {
                    vGroupIndex = frameMap[count]
                    if (vGroupIndex < this.vertexGroups.size) {
                        vertexGroup = this.vertexGroups[vGroupIndex]
                        var19 = 0
                        while (var19 < vertexGroup.size) {
                            vIndex = vertexGroup[var19]
                            verticesX[vIndex] -= ModelDefinition.animOffsetX
                            verticesY[vIndex] -= ModelDefinition.animOffsetY
                            verticesZ[vIndex] -= ModelDefinition.animOffsetZ

                            verticesX[vIndex] = dx * verticesX[vIndex] / 128
                            verticesY[vIndex] = dy * verticesY[vIndex] / 128
                            verticesZ[vIndex] = dz * verticesZ[vIndex] / 128

                            verticesX[vIndex] += ModelDefinition.animOffsetX
                            verticesY[vIndex] += ModelDefinition.animOffsetY
                            verticesZ[vIndex] += ModelDefinition.animOffsetZ
                            ++var19
                        }
                    }
                    ++count
                }
            }
        }
    }
}