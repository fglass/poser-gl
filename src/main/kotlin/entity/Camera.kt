package entity

import io.MouseHandler
import org.joml.Vector3f
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

const val MIN_ZOOM = 100f
const val MAX_ZOOM = 1600f

class Camera(private val mouse: MouseHandler) {

    val position = Vector3f(0f, 0f, 0f)
    var pitch = -25f
    var yaw = 0f
    var roll = 0f

    private var distance = 500f
    private var angle = 0f

    fun move() {
        calculateZoom()
        calculatePitch()
        calculateAngle()

        val h = calculateHorizontalDistance()
        val v = calculateVerticalDistance()
        calculatePosition(h, v)

        yaw = 180 - (ENTITY_ROT.y + angle)
    }

    private fun calculateZoom() {
        if (mouse.zooming) {
            val zoomLevel = mouse.dWheel * 2f
            distance = max(distance - zoomLevel, MIN_ZOOM)
            distance = min(distance, MAX_ZOOM)
            mouse.zooming = false
        }
    }

    private fun calculatePitch() {
        if (mouse.pressed) {
            pitch -= mouse.delta.y * 0.5f
        }
    }

    private fun calculateAngle() {
        if (mouse.pressed) {
            angle -= mouse.delta.x * 0.5f
        }
    }

    private fun calculateHorizontalDistance(): Float {
        return (distance * cos(Math.toRadians(pitch.toDouble()))).toFloat()
    }

    private fun calculateVerticalDistance(): Float {
        return (distance * sin(Math.toRadians(pitch.toDouble()))).toFloat()
    }

    private fun calculatePosition(h: Float, v: Float) {
        val theta = (ENTITY_ROT.y + angle).toDouble()
        val xOffset = (h * sin(Math.toRadians(theta))).toFloat()
        val yOffset = v - 60f
        val zOffset = (h * cos(Math.toRadians(theta))).toFloat()

        position.x = ENTITY_POS.x - xOffset
        position.y = ENTITY_POS.y + yOffset
        position.z = ENTITY_POS.z - zOffset
    }
}