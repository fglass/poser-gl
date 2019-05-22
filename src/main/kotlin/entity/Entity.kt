package entity

import model.Model
import org.joml.Vector3f

class Entity(val model: Model, val position: Vector3f, var rx: Double, var ry: Double, var rz: Double,
             val scale: Float) {

    fun increasePosition(dx: Float, dy: Float, dz: Float) {
        position.x += dx
        position.y += dy
        position.z += dz
    }

    fun increaseRotation(dx: Float, dy: Float, dz: Float) {
        rx += dx
        ry += dy
        rz += dz
    }
}