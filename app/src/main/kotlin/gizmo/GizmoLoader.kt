package gizmo

import org.joml.Vector2f
import org.joml.Vector3f
import render.Loader
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.*

object GizmoLoader {

    fun load(filename: String, loader: Loader) {
        val file = File("gizmo/$filename.obj")
        val reader = BufferedReader(FileReader(file))

        val vertices = ArrayList<Vertex>()
        val textures = ArrayList<Vector2f>()
        val normals = ArrayList<Vector3f>()
        val indices = ArrayList<Int>()

        for (line in reader.readLines()) {

            when {
                line.startsWith("v ") -> {
                    val currentLine = line.split(" ")
                    val vertex = Vector3f(currentLine[1].toFloat(), currentLine[2].toFloat(), currentLine[3].toFloat())
                    val newVertex = Vertex(vertices.size, vertex)
                    vertices.add(newVertex)
                }
                line.startsWith("vt ") -> {
                    val currentLine = line.split(" ")
                    val texture = Vector2f(currentLine[1].toFloat(), currentLine[2].toFloat())
                    textures.add(texture)
                }
                line.startsWith("vn ") -> {
                    val currentLine = line.split(" ")
                    val normal =
                        Vector3f(currentLine[1].toFloat(), currentLine[2].toFloat(), currentLine[3].toFloat())
                    normals.add(normal)
                }
                line.startsWith("f ") -> {
                    val currentLine = line.split(" ")
                    val vertex1 = currentLine[1].split("/")
                    val vertex2 = currentLine[2].split("/")
                    val vertex3 = currentLine[3].split("/")
                    processVertex(vertex1, vertices, indices)
                    processVertex(vertex2, vertices, indices)
                    processVertex(vertex3, vertices, indices)
                }
            }
        }

        reader.close()
        removeUnusedVertices(vertices)

        val verticesArray = FloatArray(vertices.size * 3)
        val texturesArray = FloatArray(vertices.size * 2)
        val normalsArray = FloatArray(vertices.size * 3)
        val furthest = convertDataToArrays(vertices, textures, normals, verticesArray, texturesArray, normalsArray)
        val indicesArray = convertIndicesListToArray(indices)

        loader.loadToVao(verticesArray, 3) // TODO
    }

    private fun processVertex(vertex: List<String>, vertices: MutableList<Vertex>, indices: MutableList<Int>) {
        val index = vertex[0].toInt() - 1
        val currentVertex = vertices[index]
        val textureIndex = vertex[1].toInt() - 1
        val normalIndex = vertex[2].toInt() - 1

        if (!currentVertex.isSet()) {
            currentVertex.textureIndex = textureIndex
            currentVertex.normalIndex = normalIndex
            indices.add(index)
        } else {
            dealWithAlreadyProcessedVertex(currentVertex, textureIndex, normalIndex, indices, vertices)
        }
    }

    private fun convertIndicesListToArray(indices: List<Int>): IntArray {
        val indicesArray = IntArray(indices.size)
        for (i in indicesArray.indices) {
            indicesArray[i] = indices[i]
        }
        return indicesArray
    }

    private fun convertDataToArrays(vertices: List<Vertex>, textures: List<Vector2f>, normals: List<Vector3f>,
                                    verticesArray: FloatArray, texturesArray: FloatArray,
                                    normalsArray: FloatArray): Float {
        var furthestPoint = 0F
        for (i in vertices.indices) {

            val currentVertex = vertices[i]
            if (currentVertex.length > furthestPoint) {
                furthestPoint = currentVertex.length
            }

            val position = currentVertex.position
            val textureCoord = textures[currentVertex.textureIndex]
            val normalVector = normals[currentVertex.normalIndex]

            verticesArray[i * 3] = position.x
            verticesArray[i * 3 + 1] = position.y
            verticesArray[i * 3 + 2] = position.z
            texturesArray[i * 2] = textureCoord.x
            texturesArray[i * 2 + 1] = 1 - textureCoord.y
            normalsArray[i * 3] = normalVector.x
            normalsArray[i * 3 + 1] = normalVector.y
            normalsArray[i * 3 + 2] = normalVector.z
        }
        return furthestPoint
    }

    private fun dealWithAlreadyProcessedVertex(previousVertex: Vertex, newTextureIndex: Int, newNormalIndex: Int,
                                               indices: MutableList<Int>, vertices: MutableList<Vertex>) {

        if (previousVertex.hasSameTextureAndNormal(newTextureIndex, newNormalIndex)) {
            indices.add(previousVertex.index)
        } else {
            val anotherVertex = previousVertex.duplicateVertex
            if (anotherVertex != null) {
                dealWithAlreadyProcessedVertex(anotherVertex, newTextureIndex, newNormalIndex, indices, vertices)
            } else {
                val duplicateVertex = Vertex(vertices.size, previousVertex.position)
                duplicateVertex.textureIndex = newTextureIndex
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
                    vertex.textureIndex = 0
                    vertex.normalIndex = 0
                }
            }
        }

    class Vertex(val index: Int, val position: Vector3f) { // TODO

        var textureIndex = -1
        var normalIndex = -1
        val length: Float = position.length()
        var duplicateVertex: Vertex? = null

        fun isSet(): Boolean {
            return textureIndex != -1 && normalIndex != -1
        }

        fun hasSameTextureAndNormal(otherTexture: Int, otherNormal: Int): Boolean {
            return otherTexture == textureIndex && otherNormal == normalIndex
        }
    }
}