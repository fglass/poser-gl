package entity

import model.RawModel
import org.joml.Vector3f

class Entity(var rawModel: RawModel, val position: Vector3f, val rotation: Vector3f,
             val scale: Float) {

    fun increasePosition(dx: Float, dy: Float, dz: Float) {
        position.x += dx
        position.y += dy
        position.z += dz
    }

    fun increaseRotation(dx: Float, dy: Float, dz: Float) {
        rotation.x += dx
        rotation.y += dy
        rotation.z += dz
    }
}