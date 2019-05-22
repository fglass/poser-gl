package model

import CACHE_PATH
import javassist.NotFoundException
import net.openrs.cache.Cache
import net.openrs.cache.FileStore
import render.Loader
import java.io.File
import net.openrs.model.Model

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
        val verticesArray = IntArray(model.faceCount * 12)
        var index = 0
        val vertexX = model.vertexX
        val vertexY = model.vertexY
        val vertexZ = model.vertexZ

        for (i in 0 until model.faceCount) {
            // 16-bit value in HSB format. First 6 bits hue, next 3 bits saturation, last 7 bits brightness
            val faceColour = model.faceColor[i].toInt()
            val triangles = intArrayOf(model.triangleX[i], model.triangleY[i], model.triangleZ[i])

            for (triangle in triangles) {
                verticesArray[index++] = vertexX[triangle]
                verticesArray[index++] = vertexY[triangle]
                verticesArray[index++] = vertexZ[triangle]
                verticesArray[index++] = faceColour
            }
        }
        return loader.loadToVao(verticesArray)
    }
}