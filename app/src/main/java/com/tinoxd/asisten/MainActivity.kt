
package com.tinoxd.asisten

import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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

    private var captureGranted by mutableStateOf(false)
    private var overlayGranted by mutableStateOf(false)
    private var assistantRunning by mutableStateOf(false)

    private var floatingView: TextView? = null

    private val screenCaptureLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            captureGranted = it.resultCode == RESULT_OK
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        overlayGranted = Settings.canDrawOverlays(this)

        setContent {
            MaterialTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF10111A))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "TinoXd Asisten",
                        color = Color.White,
                        fontSize = 30.sp
                    )

                    Spacer(Modifier.height(28.dp))

                    Button(
                        onClick = {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:$packageName")
                            )
                            startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Izinkan Pop-up")
                    }

                    Spacer(Modifier.height(10.dp))

                    Button(
                        onClick = {
                            val manager = getSystemService(
                                MEDIA_PROJECTION_SERVICE
                            ) as android.media.projection.MediaProjectionManager

                            screenCaptureLauncher.launch(
                                manager.createScreenCaptureIntent()
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Izinkan Baca Layar")
                    }

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = { startAssistant() },
                        enabled = overlayGranted && !assistantRunning,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("MULAI ASISTEN")
                    }

                    Spacer(Modifier.height(10.dp))

                    Button(
                        onClick = { stopAssistant() },
                        enabled = assistantRunning,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFB3261E)
                        )
                    ) {
                        Text("BERHENTI")
                    }

                    Spacer(Modifier.height(20.dp))

                    Text(
                        when {
                            assistantRunning -> "Pop-up aktif."
                            captureGranted -> "Izin baca layar diberikan."
                            else -> "Siapkan izin sebelum memulai."
                        },
                        color = Color.White
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        overlayGranted = Settings.canDrawOverlays(this)
    }

    private fun startAssistant() {
        if (!Settings.canDrawOverlays(this)) return

        val windowManager =
            getSystemService(WINDOW_SERVICE) as WindowManager

        val view = TextView(this).apply {
            text = "TinoXd Asisten\nPop-up aktif"
            textSize = 16f
            setTextColor(android.graphics.Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            setPadding(32, 24, 32, 24)
            setBackgroundColor(android.graphics.Color.rgb(75, 45, 130))
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 20
            y = 180
        }

        windowManager.addView(view, params)
        floatingView = view
        assistantRunning = true
    }

    private fun stopAssistant() {
        floatingView?.let {
            val windowManager =
                getSystemService(WINDOW_SERVICE) as WindowManager
            windowManager.removeView(it)
        }
        floatingView = null
        assistantRunning = false
    }

    override fun onDestroy() {
        stopAssistant()
        super.onDestroy()
    }
}
