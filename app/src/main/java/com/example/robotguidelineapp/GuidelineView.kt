package com.example.robotguidelineapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

class GuidelineView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private var steeringAngle = 0f
    private var cameraHeight = 0.12f // Default height, will be updated from SeekBar

    private val vehicleWidth = 1.8f
    private val wheelBase = 2.8f

    private val distances = floatArrayOf(3.0f, 1.5f, 0.7f)
    private val distanceColors = intArrayOf(Color.GREEN, Color.YELLOW, Color.RED)

    private val trajectoryPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFA500") // Orange
        style = Paint.Style.STROKE
        strokeWidth = 12f
    }

    private val staticLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }

    fun updateParameters(angle: Float, height: Float) {
        this.steeringAngle = angle
        this.cameraHeight = height
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawStaticDistanceMarkers(canvas)
        drawDynamicTrajectory(canvas)
    }

    private fun drawStaticDistanceMarkers(canvas: Canvas) {
        for (i in distances.indices) {
            val distance = distances[i]
            staticLinePaint.color = distanceColors[i]

            val p1 = project(PointF(-vehicleWidth / 2, distance))
            val p2 = project(PointF(vehicleWidth / 2, distance))
            canvas.drawLine(p1.x, p1.y, p2.x, p2.y, staticLinePaint)

            val tickLength = 0.3f
            val p3 = project(PointF(-vehicleWidth / 2, distance - tickLength))
            val p4 = project(PointF(vehicleWidth / 2, distance - tickLength))
            canvas.drawLine(p1.x, p1.y, p3.x, p3.y, staticLinePaint)
            canvas.drawLine(p2.x, p2.y, p4.x, p4.y, staticLinePaint)
        }
    }

    private fun drawDynamicTrajectory(canvas: Canvas) {
        val pathLeft = Path()
        val pathRight = Path()

        if (abs(steeringAngle) < 0.5f) {
            val leftStart = project(PointF(-vehicleWidth / 2, 0f))
            val leftEnd = project(PointF(-vehicleWidth / 2, distances.first() + 1f))
            pathLeft.moveTo(leftStart.x, leftStart.y)
            pathLeft.lineTo(leftEnd.x, leftEnd.y)

            val rightStart = project(PointF(vehicleWidth / 2, 0f))
            val rightEnd = project(PointF(vehicleWidth / 2, distances.first() + 1f))
            pathRight.moveTo(rightStart.x, rightStart.y)
            pathRight.lineTo(rightEnd.x, rightEnd.y)

        } else {
            val angleRad = Math.toRadians(steeringAngle.toDouble())
            val turningRadius = (wheelBase / tan(angleRad)).toFloat()
            val turnCenterX = turningRadius
            val radiusLeft = turnCenterX + vehicleWidth / 2
            val radiusRight = turnCenterX - vehicleWidth / 2

            val pathPoints = 50
            for (i in 0..pathPoints) {
                val d = i.toFloat() / pathPoints * (distances.first() + 1f)
                val theta = d / turningRadius

                val pLeft = getPointOnArc(turnCenterX, radiusLeft, theta)
                val projLeft = project(pLeft)

                val pRight = getPointOnArc(turnCenterX, radiusRight, theta)
                val projRight = project(pRight)

                if (i == 0) {
                    pathLeft.moveTo(projLeft.x, projLeft.y)
                    pathRight.moveTo(projRight.x, projRight.y)
                } else {
                    pathLeft.lineTo(projLeft.x, projLeft.y)
                    pathRight.lineTo(projRight.x, projRight.y)
                }
            }
        }

        canvas.drawPath(pathLeft, trajectoryPaint)
        canvas.drawPath(pathRight, trajectoryPaint)
    }

    private fun getPointOnArc(turnCenterX: Float, trackRadius: Float, theta: Float): PointF {
        val x = turnCenterX - trackRadius * cos(theta)
        val y = trackRadius * sin(theta)
        return PointF(x, y)
    }

    private fun project(point: PointF): PointF {
        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()

        // FINAL CORRECTED LOGIC FOR CAMERA HEIGHT
        val minHeight = 0.12f
        val maxHeight = 0.50f
        val heightRatio = (cameraHeight - minHeight) / (maxHeight - minHeight)

        // Define the virtual horizon's position on screen.
        // To make points move DOWN when height increases, the horizon must also move DOWN (Y value increases).
        val horizonStartPct = 0.2f // Horizon Y for the lowest camera height
        val horizonEndPct = 0.4f   // Horizon Y for the highest camera height
        val horizonRange = horizonEndPct - horizonStartPct
        val horizonY = viewHeight * (horizonStartPct + heightRatio * horizonRange)

        // Project the world Y-coordinate to the screen Y-coordinate.
        val screenY = horizonY + (viewHeight - horizonY) * (2f / (point.y + 2f))

        // Calculate the perspective effect on the X-coordinate based on the new screenY.
        val perspectiveRatio = (screenY - horizonY) / (viewHeight - horizonY)
        val screenX = viewWidth / 2 + point.x * perspectiveRatio * (viewWidth / (vehicleWidth * 1.5f))

        return PointF(screenX, screenY)
    }
}