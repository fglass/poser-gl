package entity

import org.joml.Vector3f

class Camera {

    val position = Vector3f(0F, 0F, 0F)
    var pitch = 20F
    var yaw = 0F
    var roll = 0F

    fun move() {
        /*if (Keyboard.Key.KEY_W.isPressed) {
            pitch += 0.1f
        }
        if (Keyboard.Key.KEY_S.isPressed) {
            pitch -= 0.1f
        }*/
    }
}