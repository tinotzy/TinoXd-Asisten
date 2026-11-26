
package com.tinoxd.asisten

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import kotlin.math.abs

class FloatingWidgetService : Service() {

    private lateinit var wm: WindowManager
    private var panel: LinearLayout? = null
    private var menu: LinearLayout? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        showWidget()
        return START_NOT_STICKY
    }

    private fun showWidget() {
        if (panel != null) return

        if (Build.VERSION.SDK_INT >= 23 &&
            !Settings.canDrawOverlays(this)
        ) {
            Toast.makeText(
                this,
                "Aktifkan izin tampil di atas aplikasi lain",
                Toast.LENGTH_LONG
            ).show()
            stopSelf()
            return
        }

        try {
            wm = getSystemService(WINDOW_SERVICE) as WindowManager

            val root = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL
            }

            val bubble = TextView(this).apply {
                text = "TX"
                textSize = 20f
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.rgb(125, 83, 220))
                    setStroke(3, Color.WHITE)
                }
                elevation = 12f
            }

            val featureMenu = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(12, 12, 12, 12)
                background = GradientDrawable().apply {
                    setColor(Color.rgb(25, 25, 40))
                    cornerRadius = 22f
                    setStroke(2, Color.rgb(125, 83, 220))
                }
                visibility = android.view.View.GONE
            }

            fun addMenuButton(
                label: String,
                action: String
            ) {
                val button = TextView(this).apply {
                    text = label
                    textSize = 15f
                    setTextColor(Color.WHITE)
                    gravity = Gravity.CENTER
                    setPadding(12, 15, 12, 15)
                    background = GradientDrawable().apply {
                        setColor(Color.rgb(65, 52, 105))
                        cornerRadius = 14f
                    }
                    setOnClickListener {
                        val i = Intent(
                            this@FloatingWidgetService,
                            MainActivity::class.java
                        ).apply {
                            putExtra("TINXD_ACTION", action)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        startActivity(i)
                    }
                }

                featureMenu.addView(
                    button,
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        bottomMargin = 8
                    }
                )
            }

            addMenuButton("🧠 Pemecah Soal", "SOLVER")
            addMenuButton("✅ Verifikasi Jawaban", "VERIFY")
            addMenuButton("⚡ Jawab Otomatis", "AUTO_ANSWER")
            addMenuButton("🏠 Buka Aplikasi", "HOME")

            val offButton = TextView(this).apply {
                text = "MATIKAN TX"
                textSize = 15f
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                setPadding(12, 15, 12, 15)
                background = GradientDrawable().apply {
                    setColor(Color.rgb(180, 45, 55))
                    cornerRadius = 14f
                }
                setOnClickListener {
                    stopSelf()
                }
            }

            featureMenu.addView(
                offButton,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )

            root.addView(
                bubble,
                LinearLayout.LayoutParams(60, 60)
            )

            root.addView(
                featureMenu,
                LinearLayout.LayoutParams(
                    230,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 10
                }
            )

            bubble.setOnClickListener {
                featureMenu.visibility =
                    if (featureMenu.visibility ==
                        android.view.View.VISIBLE
                    ) {
                        android.view.View.GONE
                    } else {
                        android.view.View.VISIBLE
                    }
            }

            val type =
                if (Build.VERSION.SDK_INT >= 26)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
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

            bubble.setOnTouchListener { _, event ->
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

                        wm.updateViewLayout(root, params)
                        true
                    }

                    MotionEvent.ACTION_UP -> {
                        val moved =
                            abs(event.rawX - touchX) > 8 ||
                            abs(event.rawY - touchY) > 8

                        if (!moved) {
                            bubble.performClick()
                        }
                        true
                    }

                    else -> false
                }
            }

            wm.addView(root, params)
            panel = root
            menu = featureMenu

            Toast.makeText(
                this,
                "TinoXd Floating Assistant aktif",
                Toast.LENGTH_SHORT
            ).show()

        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Gagal: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            stopSelf()
        }
    }

    override fun onDestroy() {
        panel?.let {
            try {
                if (::wm.isInitialized) {
                    wm.removeView(it)
                }
            } catch (_: Exception) {
            }
        }

        panel = null
        menu = null
        super.onDestroy()
    }
}
