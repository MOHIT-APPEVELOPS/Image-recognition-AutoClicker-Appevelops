package com.example.advancedautoclicker

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton

class FloatingControlService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private val clickPoints = mutableListOf<Pair<Float, Float>>()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_control_panel, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 100
        }

        // Panel ko screen par drag karne ke liye touch listener
        floatingView.setOnTouchListener(object : View.OnTouchListener {
            var initialX = 0
            var initialY = 0
            var initialTouchX = 0f
            var initialTouchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(floatingView, params)
                        return true
                    }
                }
                return false
            }
        })

        // 1. Add Point Button Action
        floatingView.findViewById<ImageButton>(R.id.btnAddPoint)?.setOnClickListener {
            clickPoints.add(Pair(500f, 800f))
        }

        // 2. Play Button Action
        floatingView.findViewById<ImageButton>(R.id.btnPlay)?.setOnClickListener {
            AutoClickAccessibilityService.instance?.startMultiPointClick(clickPoints, 500L, true, 0)
        }

        // 3. Pause Button Action
        floatingView.findViewById<ImageButton>(R.id.btnPause)?.setOnClickListener {
            AutoClickAccessibilityService.instance?.stopAutomation()
        }

        // 4. Stop Button Action
        floatingView.findViewById<ImageButton>(R.id.btnStop)?.setOnClickListener {
            AutoClickAccessibilityService.instance?.stopAutomation()
            clickPoints.clear()
        }

        // 5. Close Panel Button Action
        floatingView.findViewById<ImageButton>(R.id.btnClose)?.setOnClickListener {
            stopSelf()
        }

        windowManager.addView(floatingView, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::floatingView.isInitialized) {
            windowManager.removeView(floatingView)
        }
    }
}
