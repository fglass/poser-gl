package entity

import ENTITY_POS
import ENTITY_ROT
import input.Mouse
import org.joml.Vector3f

class Camera(private val mouse: Mouse) {

    val position = Vector3f(0F, 0F, 0F)
    var pitch = 20F
    var yaw = 0F
    var roll = 0F

    private var distance = 50F
    private var angle = 0F

    fun move() {
        calculateZoom()
        calculatePitch()
        calculateRoll()

        val h = calculateHorizontalDistance()
        val v = calculateVerticalDistance()
        calculatePosition(h, v)

        yaw = 180 - (ENTITY_ROT.y + angle)
    }

    private fun calculateZoom() {
        val zoomLevel = mouse.dWheel * 0.1F
        distance = Math.max(distance - zoomLevel, 20F)
        distance = Math.min(distance, 200F)
    }

    private fun calculatePitch() {
        if (mouse.pressed) {
            pitch -= mouse.getDY() * 0.1F
        }
    }

    private fun calculateRoll() {
        if (mouse.pressed) {
            roll -= mouse.getDX() * 0.1F
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