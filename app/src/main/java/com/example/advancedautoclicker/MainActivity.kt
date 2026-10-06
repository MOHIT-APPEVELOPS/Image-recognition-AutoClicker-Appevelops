package com.example.advancedautoclicker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.example.advancedautoclicker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.accessibilityButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        binding.overlayButton.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                startActivity(Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                ))
            }
        }

        binding.startClickButton.setOnClickListener {
            val x = binding.xInput.text.toString().toFloatOrNull() ?: return@setOnClickListener
            val y = binding.yInput.text.toString().toFloatOrNull() ?: return@setOnClickListener
            val interval = binding.intervalInput.text.toString().toLongOrNull() ?: 500L
            AutoClickAccessibilityService.instance?.startAutoClick(x, y, interval)
        }

        binding.stopClickButton.setOnClickListener {
            AutoClickAccessibilityService.instance?.stopAutomation()
        }

        binding.selectTemplateButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "image/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            startActivityForResult(intent, 42)
        }

        binding.startRecognitionButton.setOnClickListener {
            val threshold = binding.thresholdInput.text.toString().toFloatOrNull() ?: 0.90f
            AutoClickAccessibilityService.instance?.startTemplateRecognition(threshold)
        }

        binding.stopRecognitionButton.setOnClickListener {
            AutoClickAccessibilityService.instance?.stopAutomation()
        }

        binding.startRecordButton.setOnClickListener {
            AutoClickAccessibilityService.instance?.startMacroRecording()
            binding.macroStatus.text = "Recording accessibility click events..."
        }

        binding.stopRecordButton.setOnClickListener {
            AutoClickAccessibilityService.instance?.stopMacroRecording()
            binding.macroStatus.text = "Macro recording stopped."
        }

        binding.playMacroButton.setOnClickListener {
            AutoClickAccessibilityService.instance?.playLastMacro()
        }
    }

    override fun onResume() {
        super.onResume()
        val enabled = AutoClickAccessibilityService.instance != null
        binding.statusText.text = if (enabled) "Accessibility: ON" else "Accessibility: OFF"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 42 && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                AutoClickAccessibilityService.instance?.setTemplate(uri)
            }
        }
    }
}
