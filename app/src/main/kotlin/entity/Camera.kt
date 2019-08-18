package entity

import org.joml.Matrix4f
import org.joml.Rayf
import util.MouseHandler
import org.joml.Vector3f
import org.liquidengine.legui.input.Mouse
import render.RenderContext
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

const val MIN_ZOOM = 100f
const val MAX_ZOOM = 1600f

class Camera(private val mouse: MouseHandler) {

    val position = Vector3f(0f, 0f, 0f)
    var pitch = -23f
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

    fun calculateRay(context: RenderContext, viewMatrix: Matrix4f): Rayf {
        val mousePosition = Mouse.getCursorPosition()
        mousePosition.sub(context.framebuffer.position)

        val origin = Vector3f()
        val dir = Vector3f()
        Matrix4f(context.entityRenderer.projectionMatrix)
            .mul(viewMatrix)
            .unprojectRay(mousePosition.x, mousePosition.y, intArrayOf(0, 0,
                context.framebuffer.size.x.toInt(), context.framebuffer.size.y.toInt()), origin, dir
            )
        return Rayf(origin, dir)
    }
}