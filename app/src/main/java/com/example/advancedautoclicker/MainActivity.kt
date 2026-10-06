package com.example.advancedautoclicker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnAccessibility = findViewById<Button>(R.id.btnEnableAccessibility)
        val btnOverlay = findViewById<Button>(R.id.btnOverlayPermission)
        val btnToggleFloating = findViewById<Button>(R.id.btnToggleFloating)
        val etRepeatCount = findViewById<EditText>(R.id.etRepeatCount)
        val etDelay = findViewById<EditText>(R.id.etDelay)
        val btnStart = findViewById<Button>(R.id.btnStartAutomation)
        val btnStop = findViewById<Button>(R.id.btnStopAutomation)

        val selectImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                AutoClickAccessibilityService.instance?.setTemplateImage(it)
            }
        }

        btnAccessibility.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        btnOverlay.setOnClickListener {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivity(intent)
        }

        btnToggleFloating.setOnClickListener {
            startService(Intent(this, FloatingService::class.java))
        }

        btnStart.setOnClickListener {
            val repeat = etRepeatCount.text.toString().toIntOrNull() ?: 0
            val delay = etDelay.text.toString().toLongOrNull() ?: 500L
            val samplePoints = listOf(Pair(300f, 500f), Pair(600f, 900f))
            AutoClickAccessibilityService.instance?.startMultiPointClick(samplePoints, delay, true, repeat)
        }

        btnStop.setOnClickListener {
            AutoClickAccessibilityService.instance?.stopAutomation()
            stopService(Intent(this, FloatingService::class.java))
        }
    }
}
