package gizmo

import model.Model
import org.joml.Vector3f
import render.Loader
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.*

object GizmoLoader {

    fun load(filename: String, loader: Loader): Model {
        val file = File("/Users/fred/Documents/PoserGL/app/src/main/resources/gizmo/translation.obj")
        //File("resources/gizmo/$filename.obj") TODO
        val reader = BufferedReader(FileReader(file))

        val vertices = ArrayList<Vertex>()
        val normals = ArrayList<Vector3f>()
        val indices = ArrayList<Int>()

        for (line in reader.readLines()) {
            when {
                line.startsWith("v ") -> {
                    val split = line.split(" ")
                    val vertex = Vector3f(split[1].toFloat(), split[2].toFloat(), split[3].toFloat())
                    val newVertex = Vertex(vertices.size, vertex)
                    vertices.add(newVertex)
                }
                line.startsWith("vn ") -> {
                    val split = line.split(" ")
                    val normal = Vector3f(split[1].toFloat(), split[2].toFloat(), split[3].toFloat())
                    normals.add(normal)
                }
                line.startsWith("f ") -> {
                    val split = line.split(" ")
                    repeat(3) {
                        processVertex(split[it + 1].split("//"), vertices, indices)
                    }
                }
            }
        }

        reader.close()
        removeUnusedVertices(vertices)

        val verticesArray = FloatArray(vertices.size * 3)
        val normalsArray = FloatArray(vertices.size * 3)
        val furthest = convertDataToArrays(vertices, normals, verticesArray, normalsArray)
        val indicesArray = convertIndicesListToArray(indices)

        return loader.loadToVao(verticesArray, 3) // TODO
    }

    private fun processVertex(vertex: List<String>, vertices: MutableList<Vertex>, indices: MutableList<Int>) {
        val index = vertex[0].toInt() - 1
        val normalIndex = vertex[1].toInt() - 1
        val currentVertex = vertices[index]

        if (!currentVertex.isSet()) {
            currentVertex.normalIndex = normalIndex
            indices.add(index)
        } else {
            dealWithAlreadyProcessedVertex(currentVertex, normalIndex, indices, vertices)
        }
    }

    private fun convertIndicesListToArray(indices: List<Int>): IntArray {
        val indicesArray = IntArray(indices.size)
        for (i in indicesArray.indices) {
            indicesArray[i] = indices[i]
        }
        return indicesArray
    }

    private fun convertDataToArrays(vertices: List<Vertex>, normals: List<Vector3f>, verticesArray: FloatArray,
                                    normalsArray: FloatArray): Float {
        var furthestPoint = 0F
        for (i in vertices.indices) {

            val currentVertex = vertices[i]
            if (currentVertex.length > furthestPoint) {
                furthestPoint = currentVertex.length
            }

            val position = currentVertex.position
            val normalVector = normals[currentVertex.normalIndex]

            verticesArray[i * 3] = position.x
            verticesArray[i * 3 + 1] = position.y
            verticesArray[i * 3 + 2] = position.z
            normalsArray[i * 3] = normalVector.x
            normalsArray[i * 3 + 1] = normalVector.y
            normalsArray[i * 3 + 2] = normalVector.z
        }
        return furthestPoint
    }

    private fun dealWithAlreadyProcessedVertex(previousVertex: Vertex, newNormalIndex: Int,
                                               indices: MutableList<Int>, vertices: MutableList<Vertex>) {

        if (previousVertex.sameNormal(newNormalIndex)) {
            indices.add(previousVertex.index)
        } else {
            val anotherVertex = previousVertex.duplicateVertex
            if (anotherVertex != null) {
                dealWithAlreadyProcessedVertex(anotherVertex, newNormalIndex, indices, vertices)
            } else {
                val duplicateVertex = Vertex(vertices.size, previousVertex.position)
                duplicateVertex.normalIndex = newNormalIndex
                previousVertex.duplicateVertex = duplicateVertex
                vertices.add(duplicateVertex)
                indices.add(duplicateVertex.index)
            }
        }
    }

    private fun removeUnusedVertices(vertices: List<Vertex>) {
            for (vertex in vertices) {
                if (!vertex.isSet()) {
                    vertex.normalIndex = 0
                }
            }
        }

    class Vertex(val index: Int, val position: Vector3f) { // TODO

        var normalIndex = -1
        val length: Float = position.length()
        var duplicateVertex: Vertex? = null

        fun isSet(): Boolean {
            return  normalIndex != -1
        }

        fun sameNormal(other: Int): Boolean {
            return normalIndex == other
        }
    }
}