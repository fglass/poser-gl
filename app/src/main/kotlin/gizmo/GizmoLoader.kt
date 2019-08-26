package gizmo

import model.Model
import org.joml.Vector3f
import render.Loader
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*

object GizmoLoader {

    fun load(filename: String, loader: Loader): Model {
        val stream = this::class.java.getResourceAsStream("$filename.obj")
        val reader = BufferedReader(InputStreamReader(stream))

        val vertices = ArrayList<Vector3f>()
        val positions = ArrayList<Float>()

        for (line in reader.readLines()) {
            when {
                line.startsWith("v ") -> {
                    val split = line.substring(2).split(" ").map { it.toFloat() }
                    val v = Vector3f(split[0], split[1], split[2])
                    vertices.add(v)
                }
                line.startsWith("f ") -> {
                    val split = line.split(" ")
                    for (i in 1..3) {
                        val index = split[i].split("//").map { it.toInt() - 1 }[0]
                        val v = vertices[index]
                        positions.add(v.x)
                        positions.add(v.y)
                        positions.add(v.z)
                    }
                }
            }
        }
        reader.close()
        return loader.loadToVao(positions.toFloatArray(), 3)
    }
}
