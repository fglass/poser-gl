package animation

import org.dom4j.io.SAXReader
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector4f
import kotlin.math.*


object ColladaDecoder{

//    private val transformation = Matrix4f(
//        0.994637f, 0.074397f, 0.071847f, -0.926863f,
//        -0.002311f, 0.710484f, -0.703710f, 79.846878f,
//        -0.103400f, 0.699770f, 0.706846f, 1.540113f,
//        0.000000f, 0.000000f, 0.000000f, 1.000000f,
//    )

    private val JOINTS = setOf(
        "Torso", "Neck", "Upper_Arm_L", "Upper_Arm_R", "Lower_Arm_L", "Lower_Arm_R", "Hand_L", "Hand_R",
        "Upper_Leg_L", "Upper_Leg_R", "Lower_Leg_L", "Lower_Leg_R", "Foot_L", "Foot_R"
    )

    private fun toQuaternion(matrix: Matrix4f): Vector4f {
        val w: Float
        val x: Float
        val y: Float
        val z: Float
        val diagonal = matrix.m00() + matrix.m11() + matrix.m22()
        if (diagonal > 0) {
            val w4 = (sqrt(diagonal + 1f.toDouble()) * 2f).toFloat()
            w = w4 / 4f
            x = (matrix.m21() - matrix.m12()) / w4
            y = (matrix.m02() - matrix.m20()) / w4
            z = (matrix.m10() - matrix.m01()) / w4
        } else if (matrix.m00() > matrix.m11() && matrix.m00() > matrix.m22()) {
            val x4 = (sqrt(1f + matrix.m00() - matrix.m11() - matrix.m22().toDouble()) * 2f).toFloat()
            w = (matrix.m21() - matrix.m12()) / x4
            x = x4 / 4f
            y = (matrix.m01() + matrix.m10()) / x4
            z = (matrix.m02() + matrix.m20()) / x4
        } else if (matrix.m11() > matrix.m22()) {
            val y4 = (sqrt(1f + matrix.m11() - matrix.m00() - matrix.m22().toDouble()) * 2f).toFloat()
            w = (matrix.m02() - matrix.m20()) / y4
            x = (matrix.m01() + matrix.m10()) / y4
            y = y4 / 4f
            z = (matrix.m12() + matrix.m21()) / y4
        } else {
            val z4 = (sqrt(1f + matrix.m22() - matrix.m00() - matrix.m11().toDouble()) * 2f).toFloat()
            w = (matrix.m10() - matrix.m01()) / z4
            x = (matrix.m02() + matrix.m20()) / z4
            y = (matrix.m12() + matrix.m21()) / z4
            z = z4 / 4f
        }
        return Vector4f(x, y, z, w).normalize()
    }

    private fun toAngles(q: Vector4f): Triple<Double, Double, Double> { // Quat -> Euler angles
        //        val yaw = atan2(2.0 * (q.y * q.z + q.w * q.x), (q.w * q.w - q.x * q.x - q.y * q.y + q.z * q.z).toDouble())
//        val pitch = asin(-2.0 * (q.x * q.z - q.w * q.y))
//        val roll = atan2(2.0 * (q.x * q.y + q.w * q.z), (q.w * q.w + q.x * q.x - q.y * q.y - q.z * q.z).toDouble())

        // roll (x-axis rotation)
        val sinr_cosp = 2 * (q.w * q.x + q.y * q.z);
        val cosr_cosp = 1 - 2 * (q.x * q.x + q.y * q.y);
        val roll = atan2(sinr_cosp, cosr_cosp);

        // pitch (y-axis rotation)
        val sinp = 2 * (q.w * q.y - q.z * q.x);
        val pitch = if (abs(sinp) >= 1) {
            //copysign(PI / 2, sinp); // use 90 degrees if out of range
            println("err")
            1f
        } else {
            asin(sinp)
        }

        // yaw (z-axis rotation)
        val siny_cosp = 2 * (q.w * q.z + q.x * q.y);
        val cosy_cosp = 1 - 2 * (q.y * q.y + q.z * q.z);
        val yaw = atan2(siny_cosp, cosy_cosp);

        return Triple(-roll.toDegrees(), pitch.toDegrees(), yaw.toDegrees() + 35) // -, + 35
    }

    private fun Float.toDegrees() = this * 180 / PI

    private fun Double.toDegrees() = this * 180 / PI

    private fun Double.toRadians() = this * PI / 180

    private fun Double.toRS(): Int {
        val conversionFactor = 256 / (2 * PI)
        return (this * conversionFactor).roundToInt()
    }

    private fun Double.toRSDeg(): Int {
        val conversionFactor = 256 / 360.0
        return (this * conversionFactor).roundToInt()
    }

    private fun toRSFromDeg(deg: Double): Int {
        val unit = 0.0030679615757712823
        val limit = 256

        val rad = deg.toRadians()
        val idx = rad / unit

        val drs = idx / 8.0
        return drs.rem(limit).roundToInt()
    }

    private fun toDegFromRS(drs: Int): Double {
        val unit = 0.0030679615757712823 // = 0.175... degrees = 360 / 2048
        val idx = (drs and 255) * 8
        val rads = idx * unit
        return rads.toDegrees()
    }

    fun test() {
        // val startPosition = Vector4f(-0f, -107.25f, -0.75f, 1f)
        // val newPosition = firstTransformation.transform(startPosition)

        // x -51.9 y 38.4 z -34.5
//        println("x ${(-2.02).toRSDeg()} y ${111.0.toRSDeg()} z ${89.5.toRSDeg()}")
//
//        // 45, -45, -35
////        val quart = Vector4f(0.231f, -0.444f, -0.117f, 0.858f)
//        val quart = Vector4f(-0.587f, 0.578f, 0.409f, 0.392f) // TODO: not working
////        val quart = Vector4f(-0.237f, 0.406f, -0.107f, 0.876f)
//        val angles = toAngles(quart) // To Euler angles
//        println(angles)
//        println(angles.toList().map { it.toRSDeg() }) // z -> y -> x ?
//        // 45, -45, -35

        val quart = Vector4f(-0.587f, 0.578f, 0.409f, 0.392f)

        println("x ${toRSFromDeg(90.0)} y ${toRSFromDeg(0.0)} z ${toRSFromDeg(90.0)}")
        println("x ${(-2.02).toRSDeg()} y ${111.0.toRSDeg()} z ${(89.5).toRSDeg()}")

        println(toDegFromRS(64))
    }

    fun parse() {
        val url = "C:\\Users\\Fred\\Downloads\\model.dae"
        val reader = SAXReader()
        val document = reader.read(url)

        val root = document.rootElement

        root.elements().forEach { lib ->

            if (lib.name == "library_animations") {

                for (anim in lib.elements()) {

                    val id = anim.attribute("id").value

                    if (!JOINTS.any { it in id }) {
                        continue
                    }
                    
                    println(id)

                    for (el in anim.elements()) {
                        val elId = el.attribute("id")?.value ?: continue

                        if ("-input" in elId) {
                            val data = el.elements().first().data.toString().split(" ")
                            // println("$elId: $data")
                        }

                        if ("-output" in elId) {
                            val data = el.elements().first().data.toString().split(" ") // TODO: to matrix
                            val chunked = data.chunked(16)

                            for (chunk in chunked) {
                                val floats = chunk.map { it.toFloat() }
                                val matrix = Matrix4f(
                                    floats[0], floats[1], floats[2], floats[3],
                                    floats[4], floats[5], floats[6], floats[7],
                                    floats[8], floats[9], floats[10], floats[11],
                                    floats[12], floats[13], floats[14], floats[15],
                                )

                                val translation = Vector3f(matrix.m30(), matrix.m31(), matrix.m32())
                                val rotation = toQuaternion(matrix)
                                val (yaw, pitch, roll) = toAngles(rotation)

//                                println("\t roll ${roll.toDegrees()} pitch ${pitch.toDegrees()} yaw ${yaw.toDegrees()} | rotation ${rotation.x} ${rotation.y} ${rotation.z} | translation ${translation.x} ${translation.y} ${translation.z}")
                            }
                        }
                    }
                }
            }
        }
    }
}

fun main() {
    ColladaDecoder.test()
//    ColladaDecoder.parse()
}