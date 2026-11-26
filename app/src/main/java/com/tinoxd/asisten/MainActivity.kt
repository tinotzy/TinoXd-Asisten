
package com.tinoxd.asisten

import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {

    private var running by mutableStateOf(false)
    private var overlayAllowed by mutableStateOf(false)
    private var popup: LinearLayout? = null
    private var assistantOn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overlayAllowed = Settings.canDrawOverlays(this)

        setContent {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF10111A))
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "TinoXd Asisten",
                    color = Color.White,
                    fontSize = 30.sp
                )

                Text(
                    "SMART LEARNING ASSISTANT",
                    color = Color(0xFF9B7BFF)
                )

                Spacer(Modifier.height(30.dp))

                Button(
                    onClick = {
                        startActivity(
                            Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:$packageName")
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("IZINKAN POP-UP")
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = { startPopup() },
                    enabled = overlayAllowed && !running,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("MULAI ASISTEN")
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = { stopPopup() },
                    enabled = running,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("TUTUP ASISTEN")
                }

                Spacer(Modifier.height(18.dp))

                Text(
                    if (running) "Pop-up aktif" else "Pop-up mati",
                    color = Color.White
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        overlayAllowed = Settings.canDrawOverlays(this)
    }

    private fun startPopup() {
        if (!Settings.canDrawOverlays(this) || popup != null) return

        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val density = resources.displayMetrics.density
        fun dp(value: Int) = (value * density).toInt()

        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(8), dp(12), dp(12))
            setBackgroundColor(0xFF211B38.toInt())
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val title = TextView(this).apply {
            text = "TinoXd Asisten"
            textSize = 16f
            setTextColor(-1)
        }

        fun control(symbol: String) = TextView(this).apply {
            text = symbol
            textSize = 23f
            gravity = Gravity.CENTER
            setTextColor(-1)
        }

        val minimize = control("−")
        val resize = control("↗")
        val close = control("×")

        header.addView(title, LinearLayout.LayoutParams(0, dp(45), 1f))
        header.addView(minimize, LinearLayout.LayoutParams(dp(38), dp(45)))
        header.addView(resize, LinearLayout.LayoutParams(dp(38), dp(45)))
        header.addView(close, LinearLayout.LayoutParams(dp(32), dp(45)))

        val status = TextView(this).apply {
            textSize = 13f
            setPadding(dp(8), dp(10), dp(8), dp(10))
            setTextColor(-1)
        }

        val toggle = TextView(this).apply {
            textSize = 15f
            gravity = Gravity.CENTER
            setPadding(dp(12), dp(14), dp(12), dp(14))
            setTextColor(-1)
        }

        val menu = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val features = listOf(
            "AI Pemecah Soal",
            "Baca Layar (OCR)",
            "Verifikasi Jawaban",
            "Mode Belajar",
            "Riwayat Belajar"
        )

        features.forEach { feature ->
            val item = TextView(this).apply {
                text = feature
                textSize = 14f
                setTextColor(-1)
                setPadding(dp(12), dp(13), dp(12), dp(13))
                setOnClickListener {
                    Toast.makeText(
                        this@MainActivity,
                        "$feature belum disambungkan",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            menu.addView(item)
        }

        panel.addView(header)
        panel.addView(status)
        panel.addView(toggle)
        panel.addView(menu)

        fun updateStatus() {
            status.text = if (assistantOn) "STATUS: ON" else "STATUS: OFF"
            toggle.text = if (assistantOn) {
                "MATIKAN ASISTEN"
            } else {
                "AKTIFKAN ASISTEN"
            }

            toggle.setBackgroundColor(
                if (assistantOn) 0xFFB3261E.toInt()
                else 0xFF287A49.toInt()
            )
        }

        toggle.setOnClickListener {
            assistantOn = !assistantOn
            updateStatus()
        }

        var expanded = true
        var large = false

        minimize.setOnClickListener {
            expanded = !expanded

            status.visibility = if (expanded) View.VISIBLE else View.GONE
            toggle.visibility = if (expanded) View.VISIBLE else View.GONE
            menu.visibility = if (expanded) View.VISIBLE else View.GONE

            minimize.text = if (expanded) "−" else "+"
        }

        resize.setOnClickListener {
            large = !large
            val params = panel.layoutParams as WindowManager.LayoutParams
            params.width = dp(if (large) 330 else 260)
            wm.updateViewLayout(panel, params)
        }

        close.setOnClickListener {
            stopPopup()
        }

        val params = WindowManager.LayoutParams(
            dp(260),
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = dp(12)
            y = dp(120)
        }

        wm.addView(panel, params)

        popup = panel
        assistantOn = false
        updateStatus()
        running = true
    }

    private fun stopPopup() {
        popup?.let {
            val wm = getSystemService(WINDOW_SERVICE) as WindowManager
            try {
                wm.removeView(it)
            } catch (_: Exception) {
            }
        }

        popup = null
        assistantOn = false
        running = false
    }

    override fun onDestroy() {
        stopPopup()
        super.onDestroy()
    }
}
