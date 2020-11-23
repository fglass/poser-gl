package model

import api.definition.ModelDefinition
import org.joml.Vector3f
import org.joml.Vector3i
import render.Loader

class ModelParser {

    private val loader = Loader()

    fun parse(def: ModelDefinition, flatShading: Boolean): Model { // TODO: scale based on max vertex positions
        val nPosition = 4
        val nNormal = 3

        val positions = IntArray(def.faceCount * nPosition * 3)
        val normals = IntArray(def.faceCount * nNormal * 3)
        val vertexNormals = computeNormals(def)

        val vertexX = def.vertexPositionsX
        val vertexY = def.vertexPositionsY
        val vertexZ = def.vertexPositionsZ
        var vIndex = 0
        var nIndex = 0

        repeat(def.faceCount) {
            var alpha = 0
            def.faceAlphas?.let { alphas ->
                alpha = (alphas[it].toInt() and 0xFFFF) shl 16

            }

            // 16-bit value in HSB format. First 6 bits hue, next 3 bits saturation, last 7 bits brightness
            val faceColour = def.faceColors[it].toInt() and 0xFFFF
            val points = intArrayOf(def.faceVertexIndices1[it], def.faceVertexIndices2[it], def.faceVertexIndices3[it])

            for (point in points) {
                setVertex(positions, vIndex, vertexX[point], vertexY[point], vertexZ[point], alpha or faceColour)

                val normal = if (flatShading) getFaceNormal(points, vertexX, vertexY, vertexZ) else vertexNormals[point]
                setNormal(normals, nIndex, normal.x.toInt(), normal.y.toInt(), normal.z.toInt())

                vIndex += nPosition
                nIndex += nNormal
            }
        }
        return loader.loadToVao(positions, normals, def)
    }

    private fun computeNormals(def: ModelDefinition): Array<Vector3f> {
        val normals = Array(def.vertexCount) { Vector3f(0f, 0f, 0f) }
        for (i in 0 until def.vertexCount) {
            val vertex = Vector3i(def.vertexPositionsX[i], def.vertexPositionsY[i], def.vertexPositionsZ[i])

            for (j in 0 until def.faceCount) {
                val points = intArrayOf(def.faceVertexIndices1[j], def.faceVertexIndices2[j], def.faceVertexIndices3[j])

                for (point in points) {
                    val faceVertex =
                        Vector3i(def.vertexPositionsX[point], def.vertexPositionsY[point], def.vertexPositionsZ[point])

                    if (faceVertex == vertex) { // Face contains vertex
                        val faceNormal =
                            getFaceNormal(points, def.vertexPositionsX, def.vertexPositionsY, def.vertexPositionsZ)
                        normals[i].add(faceNormal)
                    }
                }
            }
        }
        return normals
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

    fun cleanUp() {
        loader.cleanUp()
    }
}