package util

import org.joml.Matrix4f
import org.joml.Vector3f
import entity.Camera
import java.lang.Math.toRadians

object MatrixCreator { // TODO: add projection matrix

    fun createTransformationMatrix(translation: Vector3f, rotation: Vector3f, scale: Float): Matrix4f {
        val matrix = Matrix4f()
        matrix.translate(translation)
        matrix.rotateXYZ(
            toRadians(rotation.x.toDouble()).toFloat(),
            toRadians(rotation.y.toDouble()).toFloat(),
            toRadians(rotation.z.toDouble()).toFloat()
        )
        matrix.scale(scale)
        return matrix
    }

    fun createViewMatrix(camera: Camera): Matrix4f {
        val matrix = Matrix4f()
        matrix.rotateXYZ(
            toRadians(camera.pitch.toDouble()).toFloat(),
            toRadians(camera.yaw.toDouble()).toFloat(),
            toRadians(camera.roll.toDouble()).toFloat()
        )

        val negativeCameraPos = Vector3f(camera.position).negate()
        matrix.translate(negativeCameraPos)
        return matrix
    }
}