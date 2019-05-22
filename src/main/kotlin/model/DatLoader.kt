package model

import javassist.NotFoundException
import net.openrs.cache.Cache
import net.openrs.cache.FileStore
import render.Loader
import java.io.File

const val CACHE_PATH = "/repository/cache/"

class DatLoader {

    private var vertices: Int = 0
    private var faces: Int = 0
    private var textureFaces: Int = 0
    private var facePriority: Byte = 0
    private lateinit var vertexX: IntArray
    private lateinit var vertexY: IntArray
    private lateinit var vertexZ: IntArray
    private lateinit var faceA: IntArray
    private lateinit var faceB: IntArray
    private lateinit var faceC: IntArray
    private lateinit var faceColours: ShortArray
    private lateinit var textureType: ByteArray
    private lateinit var vertexVSkin: IntArray
    private lateinit var faceDrawType: IntArray
    private lateinit var faceRenderPriorities: ByteArray
    private lateinit var faceAlpha: IntArray
    private lateinit var triangleTSkin: IntArray
    private lateinit var texture: ShortArray
    private lateinit var textureCoords: ByteArray
    private lateinit var texFaceA: ShortArray
    private lateinit var texFaceB: ShortArray
    private lateinit var texFaceC: ShortArray

    fun load(id: Int, loader: Loader): Model {
        Cache(FileStore.open(File(this::class.java.getResource(CACHE_PATH).toURI()))).use { cache ->
            val table = cache.getReferenceTable(7)

            if (table.getEntry(id) == null) {
                throw NotFoundException("Invalid entry")
            }

            val container = cache.read(7, id)
            val bytes = ByteArray(container.data.limit())
            container.data.get(bytes)
            return parse(bytes, loader)
        }
    }

    private fun parse(data: ByteArray, loader: Loader): Model {
        //val data = this::class.java.getResource("/$filename.dat").readBytes()
        if (data[data.size - 1].toInt() == -1 && data[data.size - 2].toInt() == -1) {
            println("New model – can't load")
        } else {
            decode(data)
        }

        val verticesArray = IntArray(faces * 12)
        var index = 0

        for (i in 0 until faces) {
            val triangleA = faceA[i]
            val triangleB = faceB[i]
            val triangleC = faceC[i]

            // 16-bit value in HSB format. First 6 bits hue, next 3 bits saturation, last 7 bits brightness
            val faceColour = faceColours[i].toInt()

            var a: Int
            var b: Int
            var c: Int

            a = vertexX[triangleA]
            b = vertexY[triangleA]
            c = vertexZ[triangleA]

            verticesArray[index++] = a
            verticesArray[index++] = b
            verticesArray[index++] = c
            verticesArray[index++] = faceColour

            a = vertexX[triangleB]
            b = vertexY[triangleB]
            c = vertexZ[triangleB]

            verticesArray[index++] = a
            verticesArray[index++] = b
            verticesArray[index++] = c
            verticesArray[index++] = faceColour

            a = vertexX[triangleC]
            b = vertexY[triangleC]
            c = vertexZ[triangleC]

            verticesArray[index++] = a
            verticesArray[index++] = b
            verticesArray[index++] = c
            verticesArray[index++] = faceColour
        }

        return loader.loadToVao(verticesArray)
    }

    private fun decode(data: ByteArray) {
        val first = Buffer(data)
        val second = Buffer(data)
        val third = Buffer(data)
        val fourth = Buffer(data)
        val fifth = Buffer(data)

        first.currentPosition = data.size - 18
        vertices = first.readUShort()
        faces = first.readUShort()
        textureFaces = first.readUnsignedByte()
        println("Vertices: $vertices Triangles: $faces TextureFaces: $textureFaces")

        val typeOpcode = first.readUnsignedByte()
        val priorityOpcode = first.readUnsignedByte()
        val alphaOpcode = first.readUnsignedByte()
        val tSkinOpcode = first.readUnsignedByte()
        val vSkinOpcode = first.readUnsignedByte()
        val i254 = first.readUShort()
        val i255 = first.readUShort()
        val i256 = first.readUShort()
        val i257 = first.readUShort()
        var i258 = 0
        val i259 = i258
        i258 += vertices

        val i260 = i258
        i258 += faces

        val i261 = i258
        if (priorityOpcode == 255)
            i258 += faces

        val i262 = i258
        if (tSkinOpcode == 1)
            i258 += faces

        val i263 = i258
        if (typeOpcode == 1)
            i258 += faces

        val i264 = i258
        if (vSkinOpcode == 1)
            i258 += vertices

        val i265 = i258
        if (alphaOpcode == 1)
            i258 += faces

        val i266 = i258
        i258 += i257

        val i267 = i258
        i258 += faces * 2

        val i268 = i258
        i258 += textureFaces * 6

        val i269 = i258
        i258 += i254

        val i270 = i258
        i258 += i255

        val i271 = i258
        i258 += i256

        vertexX = IntArray(vertices)
        vertexY = IntArray(vertices)
        vertexZ = IntArray(vertices)
        faceA = IntArray(faces)
        faceB = IntArray(faces)
        faceC = IntArray(faces)
        if (textureFaces > 0) {
            textureType = ByteArray(textureFaces)
            texFaceA = ShortArray(textureFaces)
            texFaceB = ShortArray(textureFaces)
            texFaceC = ShortArray(textureFaces)
        }

        if (vSkinOpcode == 1)
            vertexVSkin = IntArray(vertices)

        if (typeOpcode == 1) {
            faceDrawType = IntArray(faces)
            textureCoords = ByteArray(faces)
            texture = ShortArray(faces)
        }

        if (priorityOpcode == 255)
            faceRenderPriorities = ByteArray(faces)
        else
            facePriority = priorityOpcode.toByte()

        if (alphaOpcode == 1)
            faceAlpha = IntArray(faces)

        if (tSkinOpcode == 1)
            triangleTSkin = IntArray(faces)

        faceColours = ShortArray(faces)
        first.currentPosition = i259
        second.currentPosition = i269
        third.currentPosition = i270
        fourth.currentPosition = i271
        fifth.currentPosition = i264

        var startX = 0
        var startY = 0
        var startZ = 0

        for (point in 0 until vertices) {
            val flag = first.readUnsignedByte()
            var x = 0
            if (flag and 0x1 != 0)
                x = second.readSmart()
            var y = 0
            if (flag and 0x2 != 0)
                y = third.readSmart()
            var z = 0
            if (flag and 0x4 != 0)
                z = fourth.readSmart()

            vertexX[point] = startX + x
            vertexY[point] = startY + y
            vertexZ[point] = startZ + z
            startX = vertexX[point]
            startY = vertexY[point]
            startZ = vertexZ[point]
            if (vSkinOpcode == 1) {
                vertexVSkin[point] = fifth.readUnsignedByte()
            }
        }

        first.currentPosition = i267
        second.currentPosition = i263
        third.currentPosition = i261
        fourth.currentPosition = i265
        fifth.currentPosition = i262

        for (face in 0 until faces) {
            faceColours[face] = first.readUShort().toShort()
            if (typeOpcode == 1) {
                val flag = second.readUnsignedByte()
                if (flag and 0x1 == 1) {
                    faceDrawType[face] = 1
                } else {
                    faceDrawType[face] = 0
                }

                if (flag and 0x2 != 0) {
                    textureCoords[face] = (flag shr 2).toByte()
                    texture[face] = faceColours[face]
                    faceColours[face] = 127
                } else {
                    textureCoords[face] = -1
                    texture[face] = -1
                }
            }
            if (priorityOpcode == 255)
                faceRenderPriorities[face] = third.readSignedByte()

            if (alphaOpcode == 1) {
                faceAlpha[face] = fourth.readSignedByte().toInt()
                if (faceAlpha[face] < 0)
                    faceAlpha[face] = 256 + faceAlpha[face]

            }
            if (tSkinOpcode == 1)
                triangleTSkin[face] = fifth.readUnsignedByte()

        }

        first.currentPosition = i266
        second.currentPosition = i260
        var coordA = 0
        var coordB = 0
        var coordC = 0
        var offset = 0
        var coordinate: Int

        for (face in 0 until faces) {
            val opcode = second.readUnsignedByte()
            if (opcode == 1) {
                coordA = first.readSmart() + offset
                offset = coordA
                coordB = first.readSmart() + offset
                offset = coordB
                coordC = first.readSmart() + offset
                offset = coordC
                faceA[face] = coordA
                faceB[face] = coordB
                faceC[face] = coordC
            }
            if (opcode == 2) {
                coordB = coordC
                coordC = first.readSmart() + offset
                offset = coordC
                faceA[face] = coordA
                faceB[face] = coordB
                faceC[face] = coordC
            }
            if (opcode == 3) {
                coordA = coordC
                coordC = first.readSmart() + offset
                offset = coordC
                faceA[face] = coordA
                faceB[face] = coordB
                faceC[face] = coordC
            }
            if (opcode == 4) {
                coordinate = coordA
                coordA = coordB
                coordB = coordinate
                coordC = first.readSmart() + offset
                offset = coordC
                faceA[face] = coordA
                faceB[face] = coordB
                faceC[face] = coordC
            }
        }

        first.currentPosition = i268
        for (face in 0 until textureFaces) {
            textureType[face] = 0
            texFaceA[face] = first.readUShort().toShort()
            texFaceB[face] = first.readUShort().toShort()
            texFaceC[face] = first.readUShort().toShort()
        }

        // TODO texture support
        /*if (texture_coordinates != null) {
            var textured = false
            for (face in 0 until numTriangles) {
                coordinate = texture_coordinates[face] and 0xff
                if (coordinate != 255) {
                    if (textures_face_a[coordinate] and 0xffff == facePointA[face] && textures_face_b[coordinate] and 0xffff == facePointB[face] && textures_face_c[coordinate] and 0xffff == facePointC[face]) {
                        texture_coordinates[face] = -1
                    } else {
                        textured = true
                    }
                }
            }
            if (!textured)
                texture_coordinates = null
        }
        if (!has_texture_type)
            texture = null

        if (!has_face_type)
            faceDrawType = null*/
    }

}