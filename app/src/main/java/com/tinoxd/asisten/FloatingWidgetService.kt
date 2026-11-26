package com.tinoxd.asisten

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.TextView

class FloatingWidgetService : Service() {

    private lateinit var windowManager: WindowManager
    private var bubble: TextView? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        showBubble()
        return START_STICKY
    }

    private fun showBubble() {
        if (bubble != null) return

        windowManager =
            getSystemService(WINDOW_SERVICE) as WindowManager

        val view = TextView(this).apply {
            text = "TX"
            textSize = 17f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.rgb(125, 83, 220))
                setStroke(3, Color.WHITE)
            }
        }

        val type = if (Build.VERSION.SDK_INT >= 26) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            58,
            58,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 30
            y = 250
        }

        var startX = 0
        var startY = 0
        var touchX = 0f
        var touchY = 0f

        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = params.x
                    startY = params.y
                    touchX = event.rawX
                    touchY = event.rawY
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    params.x =
                        startX + (event.rawX - touchX).toInt()
                    params.y =
                        startY + (event.rawY - touchY).toInt()

                    windowManager.updateViewLayout(view, params)
                    true
                }

                MotionEvent.ACTION_UP -> {
                    val moved =
                        kotlin.math.abs(event.rawX - touchX) > 8 ||
                        kotlin.math.abs(event.rawY - touchY) > 8

                    if (!moved) {
                        val intent = Intent(
                            this,
                            MainActivity::class.java
                        ).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        startActivity(intent)
                    }
                    true
                }

                else -> false
            }
        }

        bubble = view
        windowManager.addView(view, params)
    }

    override fun onDestroy() {
        bubble?.let {
            if (::windowManager.isInitialized) {
                windowManager.removeView(it)
            }
        }
        bubble = null
        super.onDestroy()
    }
}
