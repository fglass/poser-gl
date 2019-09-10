package util

import org.joml.Matrix4f
import org.joml.Vector3f
import entity.Camera
import java.lang.Math.toRadians
import kotlin.math.tan

const val FOV = 70f
const val NEAR_PLANE = 1f
const val FAR_PLANE = 10000f

object MatrixCreator {

    fun createProjectionMatrix(width: Int, height: Int): Matrix4f {
        val aspectRatio = width.toFloat() / height
        val yScale = ((1f / tan(toRadians((FOV / 2f).toDouble()))) * aspectRatio).toFloat()
        val xScale = yScale / aspectRatio
        val frustumLength = FAR_PLANE - NEAR_PLANE

        val projectionMatrix = Matrix4f()
        projectionMatrix.m00(xScale)
        projectionMatrix.m11(yScale)
        projectionMatrix.m22(-((FAR_PLANE + NEAR_PLANE) / frustumLength))
        projectionMatrix.m23(-1f)
        projectionMatrix.m32(-((2 * NEAR_PLANE * FAR_PLANE) / frustumLength))
        projectionMatrix.m33(0f)
        return projectionMatrix
    }

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