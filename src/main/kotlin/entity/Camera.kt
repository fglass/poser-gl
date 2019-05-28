package entity

import ENTITY_POS
import ENTITY_ROT
import input.Mouse
import org.joml.Vector3f

class Camera(private val mouse: Mouse) {

    val position = Vector3f(0f, 0f, 0f)
    var pitch = 20f
    var yaw = 0f
    var roll = 0f

    private val clipX = 155
    private var distance = 50f
    private var angle = 0f

    fun move() {
        if (mouse.position.x <= clipX) {
            return
        }

        calculateZoom()
        calculatePitch()
        calculateRoll()

        val h = calculateHorizontalDistance()
        val v = calculateVerticalDistance()
        calculatePosition(h, v)

        yaw = 180 - (ENTITY_ROT.y + angle)
    }

    private fun calculateZoom() {
        val zoomLevel = mouse.dWheel * 0.1f
        distance = Math.max(distance - zoomLevel, 20f)
        distance = Math.min(distance, 200f)
    }

    private fun calculatePitch() {
        if (mouse.pressed) {
            pitch -= mouse.getDY() * 0.1f
        }
    }

    private fun calculateRoll() {
        if (mouse.pressed) {
            roll -= mouse.getDX() * 0.1f
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
        val zOffset = (h * Math.cos(Math.toRadians(theta))).toFloat()

        position.x = ENTITY_POS.x - xOffset
        position.z = ENTITY_POS.z - zOffset

        position.y = ENTITY_POS.y + v
    }
}