package model

import CACHE_PATH
import javassist.NotFoundException
import net.openrs.cache.Cache
import net.openrs.cache.FileStore
import render.Loader
import java.io.File
import net.openrs.model.Model
import org.joml.Vector3f

class DatLoader {

    fun load(id: Int, loader: Loader): RawModel {
        Cache(FileStore.open(File(CACHE_PATH))).use {
            val table = it.getReferenceTable(7)

            if (table.getEntry(id) == null) {
                throw NotFoundException("Invalid entry")
            }

            val container = it.read(7, id)
            val buffer = container.data

            val model = Model(id)
            model.decode(buffer)

            return parse(model, loader)
        }
    }

    private fun parse(model: Model, loader: Loader): RawModel {
        val positions = IntArray(model.faceCount * 12)
        val normals = FloatArray(model.faceCount * 9)
        var index = 0
        var nIndex = 0
        val vertexX = model.vertexX
        val vertexY = model.vertexY
        val vertexZ = model.vertexZ

        for (i in 0 until model.faceCount) {
            // 16-bit value in HSB format. First 6 bits hue, next 3 bits saturation, last 7 bits brightness
            val faceColour = model.faceColor[i].toInt()
            val points = intArrayOf(model.triangleX[i], model.triangleY[i], model.triangleZ[i])

            for (point in points) {
                positions[index++] = vertexX[point]
                positions[index++] = vertexY[point]
                positions[index++] = vertexZ[point]
                positions[index++] = faceColour

                val normal = calculateNormal(points, vertexX, vertexY, vertexZ)
                normals[nIndex++] = normal.x
                normals[nIndex++] = normal.y
                normals[nIndex++] = normal.z
            }
        }
        return loader.loadToVao(positions, normals)
    }

    private fun calculateNormal(points: IntArray, vertexX: IntArray, vertexY: IntArray, vertexZ: IntArray): Vector3f {
        val p1 = Vector3f(vertexX[points[0]].toFloat(), vertexY[points[0]].toFloat(), vertexZ[points[0]].toFloat())
        val p2 = Vector3f(vertexX[points[1]].toFloat(), vertexY[points[1]].toFloat(), vertexZ[points[1]].toFloat())
        val p3 = Vector3f(vertexX[points[2]].toFloat(), vertexY[points[2]].toFloat(), vertexZ[points[2]].toFloat())

        val u = p2.sub(p1)
        val v = p3.sub(p1)

        val normal = u.cross(v)
        return normal.normalize()
    }
}