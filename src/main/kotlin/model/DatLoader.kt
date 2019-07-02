package model

import CACHE_317_PATH
import CACHE_PATH
import cache.IndexType
import cache.ModelLoader
import entity.EntityComponent
import net.runelite.cache.definitions.ModelDefinition
import org.displee.CacheLibrary
import org.joml.Vector3f
import render.Loader

class DatLoader(private val loader: Loader) {

    private val maxPriority = 255

    fun load(component: EntityComponent): ModelDefinition {
        val library = CacheLibrary(CACHE_PATH)
        val model = library.getIndex(IndexType.MODEL.getIndexId(library.isOSRS)).getArchive(component.id).getFile(0)
        val def = ModelLoader().load(component.id, model.data)

        if (component.originalColours != null && component.newColours != null) {
            for (i in 0 until component.originalColours.size) {
                def.recolor(component.originalColours[i], component.newColours[i])
            }
        }
        library.close()
        return def
    }

    fun parse(def: ModelDefinition, flatShading: Boolean): Model {
        val nPosition = 4
        val nNormal = 3
        val positions = IntArray(def.faceCount * nPosition * 3)
        val normals = IntArray(def.faceCount * nNormal * 3)
        val vertexX = def.vertexPositionsX
        val vertexY = def.vertexPositionsY
        val vertexZ = def.vertexPositionsZ
        var vIndex = 0
        var nIndex = 0

        //for (priority in maxPriority downTo 0) {
            for (i in 0 until def.faceCount) {

                /*if (!prioritised(priority, i, def)) {
                    continue
                }*/

                var alpha = 0
                if (def.faceAlphas != null) {
                    alpha = (def.faceAlphas[i].toInt() and 0xFFFF) shl 16
                }

                // 16-bit value in HSB format. First 6 bits hue, next 3 bits saturation, last 7 bits brightness
                val faceColour = def.faceColors[i].toInt() and 0xFFFF
                val points = intArrayOf(def.faceVertexIndices1[i], def.faceVertexIndices2[i], def.faceVertexIndices3[i])

                for (point in points) {
                    setVertex(positions, vIndex, vertexX[point], vertexY[point], vertexZ[point], alpha or faceColour)

                    if (flatShading) {
                        val normal = getFaceNormal(points, vertexX, vertexY, vertexZ)
                        setNormal(normals, nIndex, normal.x.toInt(), normal.y.toInt(), normal.z.toInt())
                    } else {
                        val normal = def.vertexNormals[point]
                        setNormal(normals, nIndex, normal.x, normal.y, normal.z)
                    }

                    vIndex += nPosition
                    nIndex += nNormal
                }
            }
        //}
        return loader.loadToVao(positions, normals, def)
    }

    private fun prioritised(priority: Int, index: Int, def: ModelDefinition): Boolean {
        return if (def.faceRenderPriorities != null) {
            val renderPriority = def.faceRenderPriorities[index].toInt() and 0xFF
            renderPriority == priority
        } else {
            priority == maxPriority
        }
    }

    private fun getFaceNormal(points: IntArray, vertexX: IntArray, vertexY: IntArray, vertexZ: IntArray): Vector3f {
        val p1 = Vector3f(vertexX[points[0]].toFloat(), vertexY[points[0]].toFloat(), vertexZ[points[0]].toFloat())
        val p2 = Vector3f(vertexX[points[1]].toFloat(), vertexY[points[1]].toFloat(), vertexZ[points[1]].toFloat())
        val p3 = Vector3f(vertexX[points[2]].toFloat(), vertexY[points[2]].toFloat(), vertexZ[points[2]].toFloat())

        val u = p2.sub(p1)
        val v = p3.sub(p1)

        return u.cross(v) // Normalize in fragment shader
    }

    private fun setVertex(positions: IntArray, index: Int, x: Int, y: Int, z: Int, colour: Int) {
        positions[index] = x
        positions[index + 1] = y
        positions[index + 2] = z
        positions[index + 3] = colour
    }

    private fun setNormal(normals: IntArray, index: Int, x: Int, y: Int, z: Int) {
        normals[index] = x
        normals[index + 1] = y
        normals[index + 2] = z
    }
}