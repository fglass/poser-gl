package entity

import input.Mouse
import org.joml.Vector3f

const val MIN_ZOOM = 5f
const val MAX_ZOOM = 80f

class Camera(private val mouse: Mouse) {

    val position = Vector3f(0f, 0f, 0f)
    var pitch = -20f
    var yaw = 0f
    var roll = 0f

    private var distance = 15f
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
            val zoomLevel = mouse.dWheel * 0.1f
            distance = Math.max(distance - zoomLevel, MIN_ZOOM)
            distance = Math.min(distance, MAX_ZOOM)
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
        return (distance * Math.cos(Math.toRadians(pitch.toDouble()))).toFloat()
    }

    private fun calculateVerticalDistance(): Float {
        return (distance * Math.sin(Math.toRadians(pitch.toDouble()))).toFloat()
    }

    private fun calculatePosition(h: Float, v: Float) {
        val theta = (ENTITY_ROT.y + angle).toDouble()
        val xOffset = (h * Math.sin(Math.toRadians(theta))).toFloat()
        val yOffset = v - 3f
        val zOffset = (h * Math.cos(Math.toRadians(theta))).toFloat()

        position.x = ENTITY_POS.x - xOffset
        position.y = ENTITY_POS.y + yOffset
        position.z = ENTITY_POS.z - zOffset
    }
}