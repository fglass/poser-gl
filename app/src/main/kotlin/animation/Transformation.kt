package animation

import api.animation.ITransformation
import api.animation.TransformationType
import api.definition.ModelDefinition
import net.runelite.cache.models.CircularAngle
import org.joml.Vector3i

open class Transformation(override var id: Int, override val type: TransformationType,
                          var frameMap: IntArray, override var delta: Vector3i) : ITransformation {

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

        val var6 = frameMap.size
        var var7: Int
        var var8: Int
        var var11: Int
        var var12: Int
        if (type == 0) {
            var7 = 0
            ModelDefinition.animOffsetX = 0
            ModelDefinition.animOffsetY = 0
            ModelDefinition.animOffsetZ = 0
            var8 = 0
            while (var8 < var6) {
                val var9 = frameMap[var8]
                if (var9 < this.vertexGroups.size) {
                    val var10: IntArray = this.vertexGroups[var9]
                    var11 = 0
                    while (var11 < var10.size) {
                        var12 = var10[var11]
                        ModelDefinition.animOffsetX += verticesX[var12]
                        ModelDefinition.animOffsetY += verticesY[var12]
                        ModelDefinition.animOffsetZ += verticesZ[var12]
                        ++var7
                        ++var11
                    }
                }
                ++var8
            }
            if (var7 > 0) {
                ModelDefinition.animOffsetX = dx + ModelDefinition.animOffsetX / var7
                ModelDefinition.animOffsetY = dy + ModelDefinition.animOffsetY / var7
                ModelDefinition.animOffsetZ = dz + ModelDefinition.animOffsetZ / var7
            } else {
                ModelDefinition.animOffsetX = dx
                ModelDefinition.animOffsetY = dy
                ModelDefinition.animOffsetZ = dz
            }
        } else {
            var var18: IntArray
            var var19: Int
            if (type == 1) {
                var7 = 0
                while (var7 < var6) {
                    var8 = frameMap[var7]
                    if (var8 < this.vertexGroups.size) {
                        var18 = this.vertexGroups[var8]
                        var19 = 0
                        while (var19 < var18.size) {
                            var11 = var18[var19]
                            verticesX[var11] += dx
                            verticesY[var11] += dy
                            verticesZ[var11] += dz
                            ++var19
                        }
                    }
                    ++var7
                }
            } else if (type == 2) {
                var7 = 0
                while (var7 < var6) {
                    var8 = frameMap[var7]
                    if (var8 < this.vertexGroups.size) {
                        var18 = this.vertexGroups[var8]
                        var19 = 0
                        while (var19 < var18.size) {
                            var11 = var18[var19]
                            verticesX[var11] -= ModelDefinition.animOffsetX
                            verticesY[var11] -= ModelDefinition.animOffsetY
                            verticesZ[var11] -= ModelDefinition.animOffsetZ
                            var12 = (dx and 255) * 8
                            val var13 = (dy and 255) * 8
                            val var14 = (dz and 255) * 8
                            var var15: Int
                            var var16: Int
                            var var17: Int
                            if (var14 != 0) {
                                var15 = CircularAngle.SINE[var14]
                                var16 = CircularAngle.COSINE[var14]
                                var17 = var15 * verticesY[var11] + var16 * verticesX[var11] shr 16
                                verticesY[var11] =
                                    var16 * verticesY[var11] - var15 * verticesX[var11] shr 16
                                verticesX[var11] = var17
                            }
                            if (var12 != 0) {
                                var15 = CircularAngle.SINE[var12]
                                var16 = CircularAngle.COSINE[var12]
                                var17 = var16 * verticesY[var11] - var15 * verticesZ[var11] shr 16
                                verticesZ[var11] =
                                    var15 * verticesY[var11] + var16 * verticesZ[var11] shr 16
                                verticesY[var11] = var17
                            }
                            if (var13 != 0) {
                                var15 = CircularAngle.SINE[var13]
                                var16 = CircularAngle.COSINE[var13]
                                var17 = var15 * verticesZ[var11] + var16 * verticesX[var11] shr 16
                                verticesZ[var11] =
                                    var16 * verticesZ[var11] - var15 * verticesX[var11] shr 16
                                verticesX[var11] = var17
                            }
                            verticesX[var11] += ModelDefinition.animOffsetX
                            verticesY[var11] += ModelDefinition.animOffsetY
                            verticesZ[var11] += ModelDefinition.animOffsetZ
                            ++var19
                        }
                    }
                    ++var7
                }
            } else if (type == 3) {
                var7 = 0
                while (var7 < var6) {
                    var8 = frameMap[var7]
                    if (var8 < this.vertexGroups.size) {
                        var18 = this.vertexGroups[var8]
                        var19 = 0
                        while (var19 < var18.size) {
                            var11 = var18[var19]
                            verticesX[var11] -= ModelDefinition.animOffsetX
                            verticesY[var11] -= ModelDefinition.animOffsetY
                            verticesZ[var11] -= ModelDefinition.animOffsetZ
                            verticesX[var11] = dx * verticesX[var11] / 128
                            verticesY[var11] = dy * verticesY[var11] / 128
                            verticesZ[var11] = dz * verticesZ[var11] / 128
                            verticesX[var11] += ModelDefinition.animOffsetX
                            verticesY[var11] += ModelDefinition.animOffsetY
                            verticesZ[var11] += ModelDefinition.animOffsetZ
                            ++var19
                        }
                    }
                    ++var7
                }
            }
        }
    }
}