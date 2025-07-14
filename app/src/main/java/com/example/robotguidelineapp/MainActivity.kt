package com.example.robotguidelineapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private lateinit var guidelineView: GuidelineView
    private lateinit var seekBarAngle: SeekBar
    private lateinit var seekBarHeight: SeekBar
    private lateinit var valueAngle: TextView
    private lateinit var valueHeight: TextView

    // Define the real-world height range in meters
    private val minCameraHeight = 0.12f // 12 cm
    private val maxCameraHeight = 0.50f // 50 cm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        guidelineView = findViewById(R.id.guidelineView)
        seekBarAngle = findViewById(R.id.seekBar_angle)
        seekBarHeight = findViewById(R.id.seekBar_height)
        valueAngle = findViewById(R.id.value_angle)
        valueHeight = findViewById(R.id.value_height)

        setupListeners()
        updateInitialValues()
    }

    private fun setupListeners() {
        val listener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateGuideline()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        }

        seekBarAngle.setOnSeekBarChangeListener(listener)
        seekBarHeight.setOnSeekBarChangeListener(listener)
    }

    private fun updateGuideline() {
        // Convert SeekBar progress to meaningful values
        // Angle: SeekBar is 0-90, we map it to -45 to 45 degrees
        val angle = (seekBarAngle.progress - 45).toFloat()

        // Map SeekBar progress (0-100) to the real height range (0.12m to 0.50m)
        val heightRange = maxCameraHeight - minCameraHeight
        val height = (seekBarHeight.progress / 100f) * heightRange + minCameraHeight

        // Update the text labels
        valueAngle.text = "${angle.toInt()}°"
        valueHeight.text = String.format("%.2fm", height)

        // Pass the new values to our custom view
        guidelineView.updateParameters(angle, height)
    }

    private fun updateInitialValues() {
        // Set initial text values based on SeekBar's default progress
        updateGuideline()
    }
}
