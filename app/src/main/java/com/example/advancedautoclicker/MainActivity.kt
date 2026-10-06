package com.example.advancedautoclicker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnAccessibility = findViewById<Button>(R.id.btnAccessibility)
        val btnOverlay = findViewById<Button>(R.id.btnOverlay)
        val btnStartService = findViewById<Button>(R.id.btnStartService)
        val btnStopService = findViewById<Button>(R.id.btnStopService)

        // 1. Accessibility Permission
        btnAccessibility?.setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        }

        // 2. Overlay Permission
        btnOverlay?.setOnClickListener {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }

        // 3. Start Floating Control Service (App khuli rahegi, panel upar aa jayega)
        btnStartService?.setOnClickListener {
            if (Settings.canDrawOverlays(this)) {
                val intent = Intent(this, FloatingControlService::class.java)
                startService(intent)
                Toast.makeText(this, "Floating Panel Started", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please grant Overlay Permission first", Toast.LENGTH_SHORT).show()
            }
        }

        // 4. Stop Floating Control Service
        btnStopService?.setOnClickListener {
            val intent = Intent(this, FloatingControlService::class.java)
            stopService(intent)
            Toast.makeText(this, "Floating Panel Stopped", Toast.LENGTH_SHORT).show()
        }
    }
}
