package utils

import org.joml.Matrix4f
import org.joml.Vector3f
import entity.Camera

class Maths {

    companion object {

        fun createTransformationMatrix(translation: Vector3f, rotation: Vector3f, scale: Float): Matrix4f {
            val matrix = Matrix4f()
            matrix.identity()
            matrix.translate(translation)
            matrix.rotate(Math.toRadians(rotation.x.toDouble()).toFloat(), 1.0f, 0.0f, 0.0f)
            matrix.rotate(Math.toRadians(rotation.y.toDouble()).toFloat(), 0.0f, 1.0f, 0.0f)
            matrix.rotate(Math.toRadians(rotation.z.toDouble()).toFloat(), 0.0f, 0.0f, 1.0f)
            matrix.scale(scale)
            return matrix
        }

        fun createViewMatrix(camera: Camera): Matrix4f {
            val matrix = Matrix4f()
            matrix.identity()

            matrix.rotate(Math.toRadians(camera.pitch.toDouble()).toFloat(), Vector3f(1F, 0F, 0F))
            matrix.rotate(Math.toRadians(camera.yaw.toDouble()).toFloat(), Vector3f(0F, 1F, 0F))
            matrix.rotate(Math.toRadians(camera.roll.toDouble()).toFloat(), Vector3f(0F, 0F, 1F))

            val pos = camera.position
            val negativeCameraPos = Vector3f(-pos.x, -pos.y, -pos.z)
            matrix.translate(negativeCameraPos)
            return matrix
        }
    }
}