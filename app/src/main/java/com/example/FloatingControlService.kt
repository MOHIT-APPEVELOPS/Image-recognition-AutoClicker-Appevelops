package com.example.advancedautoclicker

import android.app.*
import android.content.Intent
import android.graphics.Color
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout

class FloatingControlService : Service() {

    private var windowManager: WindowManager? = null
    private var panel: View? = null

    override fun onCreate() {
        super.onCreate()
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }

        val notification = Notification.Builder(this, createChannel())
            .setContentTitle("Advanced Auto Clicker")
            .setContentText("Floating controls active")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .build()
        startForeground(7, notification)

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(10, 10, 10, 10)
            setBackgroundColor(Color.argb(225, 25, 25, 30))
        }

        val stop = Button(this).apply {
            text = "STOP"
            setOnClickListener {
                AutoClickAccessibilityService.instance?.stopAutomation()
            }
        }
        layout.addView(stop)

        val close = Button(this).apply {
            text = "×"
            setOnClickListener { stopSelf() }
        }
        layout.addView(close)

        panel = layout

        val type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            android.graphics.PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.END
        params.x = 20
        params.y = 150

        windowManager?.addView(panel, params)
    }

    private fun createChannel(): String {
        val id = "auto_clicker"
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(id, "Auto Clicker", NotificationManager.IMPORTANCE_LOW)
        )
        return id
    }

    override fun onDestroy() {
        panel?.let { windowManager?.removeView(it) }
        panel = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
