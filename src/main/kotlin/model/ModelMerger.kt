package model

import net.runelite.cache.definitions.ModelDefinition

/**
 * Originates from OSRS 179 deob TODO: Clean-up
 */
class ModelMerger {

    companion object {

        fun merge(models: ArrayList<ModelDefinition>): ModelDefinition {
            val newDef = ModelDefinition()
            newDef.vertexCount = 0
            newDef.faceCount = 0
            newDef.priority = 0
            var var3 = false
            var var4 = false
            var var5 = false
            newDef.vertexCount = 0
            newDef.faceCount = 0
            newDef.priority = -1

            var current: ModelDefinition?
            var i = 0
            while (i < models.size) {
                current = models[i]
                newDef.vertexCount += current.vertexCount
                newDef.faceCount += current.faceCount
                if (current.faceRenderPriorities != null) {
                    var4 = true
                } else {
                    if (newDef.priority.toInt() == -1) {
                        newDef.priority = current.priority
                    }

                    if (newDef.priority != current.priority) {
                        var4 = true
                    }
                }

                var3 = var3 or (current.faceRenderTypes != null)
                var5 = var5 or (current.faceAlphas != null)
                ++i
            }

            newDef.vertexPositionsX = IntArray(newDef.vertexCount)
            newDef.vertexPositionsY = IntArray(newDef.vertexCount)
            newDef.vertexPositionsZ = IntArray(newDef.vertexCount)
            newDef.vertexSkins = IntArray(newDef.vertexCount)
            newDef.faceVertexIndices1 = IntArray(newDef.faceCount)
            newDef.faceVertexIndices2 = IntArray(newDef.faceCount)
            newDef.faceVertexIndices3 = IntArray(newDef.faceCount)
            if (var3) {
                newDef.faceRenderTypes = ByteArray(newDef.faceCount)
            }

            if (var4) {
                newDef.faceRenderPriorities = ByteArray(newDef.faceCount)
            }

            if (var5) {
                newDef.faceAlphas = ByteArray(newDef.faceCount)
            }

            newDef.faceColors = ShortArray(newDef.faceCount)
            newDef.vertexCount = 0
            newDef.faceCount = 0

            i = 0
            while (i < models.size) {
                current = models[i]
                var var11 = 0
                while (var11 < current.faceCount) {
                    if (var3 && current.faceRenderTypes != null) {
                        newDef.faceRenderTypes[newDef.faceCount] = current.faceRenderTypes[var11]
                    }

                    if (var4) {
                        if (current.faceRenderPriorities != null) {
                            newDef.faceRenderPriorities[newDef.faceCount] = current.faceRenderPriorities[var11]
                        } else {
                            newDef.faceRenderPriorities[newDef.faceCount] = current.priority
                        }
                    }

                    if (var5 && current.faceAlphas != null) {
                        newDef.faceAlphas[newDef.faceCount] = current.faceAlphas[var11]
                    }

                    newDef.faceColors[newDef.faceCount] = current.faceColors[var11]
                    newDef.faceVertexIndices1[newDef.faceCount] =
                        increment(newDef, current, current.faceVertexIndices1[var11])
                    newDef.faceVertexIndices2[newDef.faceCount] =
                        increment(newDef, current, current.faceVertexIndices2[var11])
                    newDef.faceVertexIndices3[newDef.faceCount] =
                        increment(newDef, current, current.faceVertexIndices3[var11])
                    ++newDef.faceCount
                    ++var11
                }

                ++i
            }
            return newDef
        }

        private fun increment(newDef: ModelDefinition, current: ModelDefinition, var2: Int): Int {
            var var3 = -1
            val var4 = current.vertexPositionsX[var2]
            val var5 = current.vertexPositionsY[var2]
            val var6 = current.vertexPositionsZ[var2]

            for (var7 in 0 until newDef.vertexCount) {
                if (var4 == newDef.vertexPositionsX[var7] && var5 == newDef.vertexPositionsY[var7] && var6 == newDef.vertexPositionsZ[var7]) {
                    var3 = var7
                    break
                }
            }

            if (var3 == -1) {
                newDef.vertexPositionsX[newDef.vertexCount] = var4
                newDef.vertexPositionsY[newDef.vertexCount] = var5
                newDef.vertexPositionsZ[newDef.vertexCount] = var6
                if (current.vertexSkins != null) {
                    newDef.vertexSkins[newDef.vertexCount] = current.vertexSkins[var2]
                }
                var3 = newDef.vertexCount++
            }

            return var3
        }
    }
}