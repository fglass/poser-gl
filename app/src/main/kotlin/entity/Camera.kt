package entity

import org.joml.*
import util.MouseButtonHandler
import org.liquidengine.legui.input.Mouse
import render.RenderContext
import util.SettingsManager
import java.lang.Math
import kotlin.math.*

const val MIN_ZOOM = 100f
const val MAX_ZOOM = 1600f
const val ZOOM_SENSITIVITY = 8f
const val CAMERA_SENSITIVITY = 0.5f

class Camera(private val settingsManager: SettingsManager, private val lmb: MouseButtonHandler,
             private val mmb: MouseButtonHandler, private val rmb: MouseButtonHandler) {

    var pitch = -23f
    var yaw = 0f
    var roll = 0f

    private var distance = 500f
    private var angle = 0f
    private var zooming = false
    private var dWheel = 0f

    var position = Vector3f()
    private val center = Vector3f(ENTITY_POS)

    fun tick() {
        calculateZoom()
        arrayOf(mmb, rmb).forEach {
            calculatePitch(it)
            calculateAngle(it)
        }

        calculatePosition()
        yaw = 180 - (ENTITY_ROT.y + angle)
    }

    private fun calculateZoom() {
        if (zooming) {
            val zoomLevel = dWheel * getZoomMultiplier()
            distance = max(distance - zoomLevel, MIN_ZOOM)
            distance = min(distance, MAX_ZOOM)
            zooming = false
        }
    }

    fun handleScroll(dWheel: Double) {
        zooming = true
        this.dWheel = dWheel.toFloat()
    }

    private fun calculatePitch(button: MouseButtonHandler) {
        if (button.pressed) {
            pitch -= button.delta.y * getCameraMultiplier()
        }
    }

    private fun calculateAngle(button: MouseButtonHandler) {
        if (button.pressed) {
            angle -= button.delta.x * getCameraMultiplier()
        }
    }

    private fun calculateHorizontalDistance(): Float {
        return (distance * cos(Math.toRadians(pitch.toDouble()))).toFloat()
    }

    private fun calculateVerticalDistance(): Float {
        return (distance * sin(Math.toRadians(pitch.toDouble()))).toFloat()
    }

    private fun calculatePosition() {
        val h = calculateHorizontalDistance()
        val v = calculateVerticalDistance()
        val theta = (ENTITY_ROT.y + angle).toDouble()

        val offset = getCenterOffset(h, v, theta)
        position = Vector3f(center).sub(offset)
    }

    private fun getCenterOffset(h: Float, v: Float, theta: Double) =
        Vector3f((h * sin(Math.toRadians(theta))).toFloat(), -v + 60, (h * cos(Math.toRadians(theta))).toFloat())


    fun pan(viewMatrix: Matrix4f) {
        if (lmb.pressed) {
            val right = Vector3f(viewMatrix.m00(), viewMatrix.m10(), viewMatrix.m20()).mul(lmb.delta.x)
            val up = Vector3f(viewMatrix.m01(), viewMatrix.m11(), viewMatrix.m21()).mul(lmb.delta.y)
            val delta = right.add(up).mul(getCameraMultiplier()).mul(2f)
            center.sub(delta)
        }
    }

    fun calculateRay(context: RenderContext, viewMatrix: Matrix4f): Rayf {
        val mousePosition = Mouse.getCursorPosition()
        mousePosition.sub(context.framebuffer.position)

        val origin = Vector3f()
        val dir = Vector3f()
        Matrix4f(context.projectionMatrix)
            .mul(viewMatrix)
            .unprojectRay(mousePosition.x, mousePosition.y, intArrayOf(0, 0,
                context.framebuffer.size.x.toInt(), context.framebuffer.size.y.toInt()), origin, dir
            )
        return Rayf(origin, dir)
    }

    private fun getZoomMultiplier() = ZOOM_SENSITIVITY * settingsManager.sensitivityMultiplier

    private fun getCameraMultiplier() = CAMERA_SENSITIVITY * settingsManager.sensitivityMultiplier
}