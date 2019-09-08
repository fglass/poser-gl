package entity

import org.joml.*
import util.MouseHandler
import org.liquidengine.legui.input.Mouse
import render.RenderContext
import java.lang.Math
import kotlin.math.*

const val MIN_ZOOM = 100f
const val MAX_ZOOM = 1600f

class Camera(private val lmb: MouseHandler, private val rmb: MouseHandler) {

    val position = Vector3f()
    private val center = Vector3f(ENTITY_POS)

    var pitch = -23f
    var yaw = 0f
    var roll = 0f

    private var distance = 500f
    private var angle = 0f

    fun tick() {
        calculateZoom()
        calculatePitch()
        calculateAngle()

        val h = calculateHorizontalDistance()
        val v = calculateVerticalDistance()
        calculatePosition(h, v)

        yaw = 180 - (ENTITY_ROT.y + angle)
    }

    private fun calculateZoom() {
        if (rmb.zooming) {
            val zoomLevel = rmb.dWheel * 2f
            distance = max(distance - zoomLevel, MIN_ZOOM)
            distance = min(distance, MAX_ZOOM)
            rmb.zooming = false
        }
    }

    private fun calculatePitch() {
        if (rmb.pressed) {
            pitch -= rmb.delta.y * 0.5f
        }
    }

    private fun calculateAngle() {
        if (rmb.pressed) {
            angle -= rmb.delta.x * 0.5f
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
        val yOffset = v - 60
        val zOffset = (h * cos(Math.toRadians(theta))).toFloat()

        position.x = center.x - xOffset
        position.y = center.y + yOffset
        position.z = center.z - zOffset
    }

    fun pan(viewMatrix: Matrix4f) {
        if (lmb.pressed) {
            val right = Vector3f(viewMatrix.m00(), viewMatrix.m10(), viewMatrix.m20()).mul(lmb.delta.x)
            val up = Vector3f(viewMatrix.m01(), viewMatrix.m11(), viewMatrix.m21()).mul(lmb.delta.y)
            val delta = right.add(up)
            center.sub(delta)
        }
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