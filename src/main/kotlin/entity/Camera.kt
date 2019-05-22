package entity

import input.Mouse
import org.joml.Vector3f

class Camera(private val mouse: Mouse) {

    val position = Vector3f(0F, 0F, 0F)
    var pitch = 20F
    var yaw = 0F
    var roll = 0F

    private var distance = 50F
    private var angle = 0F

    fun move(entity:Entity) {
        calculateZoom()
        calculatePitch()
        calculateRoll()

        val h = calculateHorizontalDistance()
        val v = calculateVerticalDistance()
        calculatePosition(entity, h, v)

        yaw = 180 - (entity.ry.toFloat() + angle)
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

    private fun calculatePosition(entity: Entity, h: Float, v: Float) {
        val theta = entity.ry + angle
        val xOffset = (h * Math.sin(Math.toRadians(theta))).toFloat()
        val zOffset = (h * Math.cos(Math.toRadians(theta))).toFloat()

        position.x = entity.position.x - xOffset
        position.z = entity.position.z - zOffset

        position.y = entity.position.y + v
    }
}