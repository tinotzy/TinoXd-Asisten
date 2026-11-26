package com.tinoxd.asisten

import android.graphics.PixelFormat
import android.os.Bundle
import android.provider.Settings
import android.content.Intent
import android.net.Uri
import android.view.Gravity
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
                    color = Color(0xFF9B7BFF),
                    fontSize = 12.sp
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
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFB3261E)
                    )
                ) {
                    Text("BERHENTI")
                }

                Spacer(Modifier.height(18.dp))

                Text(
                    if (running) "Asisten sedang aktif"
                    else "Asisten belum aktif",
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
        fun dp(n: Int) = (n * density).toInt()

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
            setPadding(dp(5), dp(10), dp(5), dp(10))
        }

        fun control(label: String): TextView {
            return TextView(this).apply {
                text = label
                textSize = 23f
                gravity = Gravity.CENTER
                setTextColor(-1)
            }
        }

        val minimize = control("−")
        val resize = control("↗")
        val close = control("×")

        header.addView(
            title,
            LinearLayout.LayoutParams(0, dp(45), 1f)
        )
        header.addView(minimize, LinearLayout.LayoutParams(dp(38), dp(45)))
        header.addView(resize, LinearLayout.LayoutParams(dp(38), dp(45)))
        header.addView(close, LinearLayout.LayoutParams(dp(32), dp(45)))

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
                        "$feature belum aktif. Fitur akan disambungkan bertahap.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            menu.addView(item)
        }

        panel.addView(header)
        panel.addView(menu)

        var expanded = true
        var large = false

        minimize.setOnClickListener {
            expanded = !expanded
            menu.visibility =
                if (expanded) android.view.View.VISIBLE
                else android.view.View.GONE
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
        running = false
    }

    override fun onDestroy() {
        stopPopup()
        super.onDestroy()
    }
}
