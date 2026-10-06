package com.example.advancedautoclicker

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Path
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Display
import android.view.accessibility.AccessibilityEvent
import java.io.InputStream
import kotlin.random.Random

class AutoClickAccessibilityService : AccessibilityService() {

    companion object {
        var instance: AutoClickAccessibilityService? = null
    }

    private val handler = Handler(Looper.getMainLooper())
    private var running = false
    
    // Image Recognition variables
    private var templateBitmap: Bitmap? = null
    private var recognitionThreshold = 0.90f
    private var isRecognizing = false

    // Macro variables
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
            val r = Rect()
            node?.getBoundsInScreen(r)
            val x = r.centerX().toFloat()
            val y = r.centerY().toFloat()
            macro.add(MacroAction.Click(x, y, delay.coerceAtMost(10_000)))
            lastRecordedTime = now
        }
    }

    override fun onInterrupt() {}

    // --- 1. Multi-Point Click & Random Delay ---
    fun startMultiPointClick(points: List<Pair<Float, Float>>, baseDelay: Long, useRandomDelay: Boolean, repeatCount: Int) {
        stopAutomation()
        if (points.isEmpty()) return
        running = true
        var currentRepeat = 0

        fun runLoop() {
            if (!running) return
            if (repeatCount > 0 && currentRepeat >= repeatCount) {
                stopAutomation()
                return
            }

            fun clickNext(index: Int) {
                if (!running) return
                if (index >= points.size) {
                    currentRepeat++
                    val finalDelay = if (useRandomDelay) baseDelay + Random.nextLong(0, 150) else baseDelay
                    handler.postDelayed({ runLoop() }, finalDelay)
                    return
                }
                val (x, y) = points[index]
                click(x, y)
                handler.postDelayed({ clickNext(index + 1) }, 80)
            }
            clickNext(0)
        }
        runLoop()
    }

    // --- 2. Swipe / Drag Gesture Support ---
    fun performSwipe(startX: Float, startY: Float, endX: Float, endY: Float, duration: Long) {
        val path = Path().apply {
            moveTo(startX, startY)
            lineTo(endX, endY)
        }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, duration))
            .build()
        dispatchGesture(gesture, null, null)
    }

    // --- 3. Image Recognition & Template Matching ---
    fun setTemplateImage(uri: Uri) {
        try {
            val input: InputStream? = contentResolver.openInputStream(uri)
            templateBitmap = input?.use { BitmapFactory.decodeStream(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun startTemplateRecognition(threshold: Float) {
        stopAutomation()
        recognitionThreshold = threshold.coerceIn(0.1f, 1.0f)
        if (templateBitmap == null) return

        isRecognizing = true
        running = true
        runImageRecognitionLoop()
    }

    private fun runImageRecognitionLoop() {
        if (!running || !isRecognizing) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            takeScreenshot(
                Display.DEFAULT_DISPLAY,
                mainExecutor,
                object : TakeScreenshotCallback {
                    override fun onSuccess(screenshot: android.accessibilityservice.AccessibilityService.ScreenshotResult) {
    val hardwareBitmap = Bitmap.wrapHardwareBuffer(
        screenshot.hardwareBuffer,
        screenshot.colorSpace
    )
    
    val mutableBitmap = hardwareBitmap?.copy(Bitmap.Config.ARGB_8888, true)
    
    // Yahan hardwareBuffer ki jagah screenshot.hardwareBuffer likhna hai
    screenshot.hardwareBuffer?.close()

    if (mutableBitmap != null && templateBitmap != null) {
        val matchPoint = findTemplateMatch(mutableBitmap, templateBitmap!!)
        if (matchPoint != null) {
            // Image milne par wahan click karein
            click(matchPoint.first, matchPoint.second)
        }
    }
    
    // Agle scan ke liye delay
    handler.postDelayed({ runImageRecognitionLoop() }, 1000)
}

                    override fun onFailure(errorCode: Int) {
                        handler.postDelayed({ runImageRecognitionLoop() }, 2000)
                    }
                }
            )
        } else {
            // Android 10 ya usse neeche ke liye fallback
            handler.postDelayed({ runImageRecognitionLoop() }, 1000)
        }
    }

    private fun findTemplateMatch(screen: Bitmap, template: Bitmap): Pair<Float, Float>? {
        // Basic template matching placeholder logic 
        // (Aap yahan OpenCV ya Pixel-based comparison algorithm jod sakte hain)
        // Filhal yeh center coordinate return karta hai jab image processed ho
        return Pair(screen.width / 2f, screen.height / 2f)
    }

    fun stopRecognition() {
        isRecognizing = false
        stopAutomation()
    }

    // --- 4. Macro Recording & Playback ---
    fun startMacroRecording() {
        macro.clear()
        recording = true
        lastRecordedTime = 0L
    }

    fun stopMacroRecording() {
        recording = false
    }

    fun playLastMacro(repeatCount: Int) {
        stopAutomation()
        if (macro.isEmpty()) return
        running = true
        var currentRepeat = 0

        fun playLoop() {
            if (!running) return
            if (repeatCount > 0 && currentRepeat >= repeatCount) {
                stopAutomation()
                return
            }

            fun playMacroAt(index: Int) {
                if (!running || index >= macro.size) {
                    currentRepeat++
                    handler.postDelayed({ playLoop() }, 400)
                    return
                }
                val action = macro[index]
                handler.postDelayed({
                    if (action is MacroAction.Click) {
                        click(action.x, action.y)
                        playMacroAt(index + 1)
                    }
                }, action.delayMs)
            }
            playMacroAt(0)
        }
        playLoop()
    }

    fun stopAutomation() {
        running = false
        isRecognizing = false
        handler.removeCallbacksAndMessages(null)
    }

    private fun click(x: Float, y: Float) {
        val path = Path().apply { moveTo(x, y) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 50))
            .build()
        dispatchGesture(gesture, null, null)
    }
}

sealed class MacroAction(open val delayMs: Long) {
    data class Click(val x: Float, val y: Float, override val delayMs: Long) : MacroAction(delayMs)
}
