package com.example.advancedautoclicker

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Path
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import java.io.InputStream
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class AutoClickAccessibilityService : AccessibilityService() {

    companion object {
        var instance: AutoClickAccessibilityService? = null
    }

    private val handler = Handler(Looper.getMainLooper())
    private var running = false
    private var template: Bitmap? = null
    private var recognitionThreshold = 0.90f

    private val macro = mutableListOf<MacroAction>()
    private var recording = false
    private var lastRecordedTime = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onDestroy() {
        stopAutomation()
        instance = null
        super.onDestroy()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!recording || event == null) return

        if (event.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            val now = System.currentTimeMillis()
            val delay = if (lastRecordedTime == 0L) 0L else now - lastRecordedTime
            val node = event.source
            val r = android.graphics.Rect()
            node?.getBoundsInScreen(r)
            val x = r.centerX().toFloat()
            val y = r.centerY().toFloat()
            macro.add(MacroAction.Click(x, y, delay.coerceAtMost(10_000)))
            lastRecordedTime = now
        }
    }

    fun startAutoClick(x: Float, y: Float, intervalMs: Long) {
        stopAutomation()
        running = true
        fun tick() {
            if (!running) return
            click(x, y)
            handler.postDelayed({ tick() }, intervalMs.coerceAtLeast(30))
        }
        tick()
    }

    fun startMacroRecording() {
        macro.clear()
        recording = true
        lastRecordedTime = 0L
    }

    fun stopMacroRecording() {
        recording = false
    }

    fun playLastMacro() {
        stopAutomation()
        if (macro.isEmpty()) return
        running = true
        playMacroAt(0)
    }

    private fun playMacroAt(index: Int) {
        if (!running || index >= macro.size) {
            running = false
            return
        }
        val action = macro[index]
        handler.postDelayed({
            if (action is MacroAction.Click) click(action.x, action.y)
            playMacroAt(index + 1)
        }, action.delayMs)
    }

    fun setTemplate(uri: Uri) {
        val input: InputStream? = contentResolver.openInputStream(uri)
        template = input?.use { BitmapFactory.decodeStream(it) }
    }

    fun startTemplateRecognition(threshold: Float) {
        stopAutomation()
        recognitionThreshold = threshold.coerceIn(0.50f, 0.99f)
        if (template == null) return

        running = true
        recognitionLoop()
    }

    private fun recognitionLoop() {
        if (!running) return
        takeScreenshotCompat { screenshot ->
            val t = template
            if (screenshot != null && t != null) {
                val match = TemplateMatcher.findBestMatch(screenshot, t)
                if (match.score >= recognitionThreshold) {
                    click(match.centerX, match.centerY)
                    handler.postDelayed({ recognitionLoop() }, 350)
                } else {
                    handler.postDelayed({ recognitionLoop() }, 150)
                }
            } else {
                handler.postDelayed({ recognitionLoop() }, 250)
            }
        }
    }

    private fun takeScreenshotCompat(callback: (Bitmap?) -> Unit) {
        takeScreenshot(
            android.view.Display.DEFAULT_DISPLAY,
            mainExecutor,
            object : TakeScreenshotCallback {
                override fun onSuccess(result: ScreenshotResult) {
                    val buffer = result.hardwareBuffer
                    val bitmap = Bitmap.wrapHardwareBuffer(buffer, result.colorSpace)
                    buffer.close()
                    callback(bitmap?.copy(Bitmap.Config.ARGB_8888, false))
                }

                override fun onFailure(errorCode: Int) {
                    callback(null)
                }
            }
        )
    }

    fun stopAutomation() {
        running = false
        handler.removeCallbacksAndMessages(null)
    }

    private fun click(x: Float, y: Float) {
        val path = Path().apply { moveTo(x, y) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 40))
            .build()
        dispatchGesture(gesture, null, null)
    }
}

sealed class MacroAction {
    abstract val delayMs: Long
    data class Click(val x: Float, val y: Float, override val delayMs: Long) : MacroAction()
}
