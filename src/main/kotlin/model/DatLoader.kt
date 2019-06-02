package model

import CACHE_PATH
import render.Loader
import java.io.File
import net.runelite.cache.IndexType
import net.runelite.cache.definitions.ModelDefinition
import net.runelite.cache.definitions.loaders.ModelLoader
import net.runelite.cache.fs.Store
import org.joml.Vector3f

class DatLoader {

    fun load(id: Int, flatShading: Boolean, loader: Loader): RawModel {
        Store(File(CACHE_PATH)).use { store ->
            store.load()
            val storage = store.storage
            val index = store.getIndex(IndexType.MODELS)

            val archive = index.getArchive(id)
            val contents = archive.decompress(storage.loadArchive(archive))

            val modelLoader = ModelLoader()
            val def = modelLoader.load(archive.archiveId, contents)

            return parse(def, flatShading, loader)
        }
    }

    fun parse(def: ModelDefinition, flatShading: Boolean, loader: Loader): RawModel {
        val positions = IntArray(def.faceCount * 12)
        val normals = IntArray(def.faceCount * 9)
        val vertexX = def.vertexPositionsX
        val vertexY = def.vertexPositionsY
        val vertexZ = def.vertexPositionsZ
        var vIndex = 0
        var nIndex = 0

        for (i in 0 until def.faceCount) {
            // 16-bit value in HSB format. First 6 bits hue, next 3 bits saturation, last 7 bits brightness
            val faceColour = def.faceColors[i].toInt()
            val points = intArrayOf(def.faceVertexIndices1[i], def.faceVertexIndices2[i], def.faceVertexIndices3[i])

            for (point in points) {
                setVertex(positions, vIndex, vertexX[point], vertexY[point], vertexZ[point], faceColour)

                if (flatShading) {
                    val normal = getFaceNormal(points, vertexX, vertexY, vertexZ)
                    setNormal(normals, nIndex, normal.x.toInt(), normal.y.toInt(), normal.z.toInt())
                } else {
                    val normal = def.vertexNormals[point]
                    setNormal(normals, nIndex, normal.x, normal.y, normal.z)
                }

                vIndex += 4
                nIndex += 3
            }
        }
        return loader.loadToVao(positions, normals, def)
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