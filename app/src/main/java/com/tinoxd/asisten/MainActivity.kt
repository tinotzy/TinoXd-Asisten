
package com.tinoxd.asisten

import android.content.Intent
import android.graphics.PixelFormat
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Dark = Color(0xFF10111A)
private val Purple = Color(0xFF8257E5)
private val CardColor = Color(0xFF1D1E2B)

class MainActivity : ComponentActivity() {

    private var assistantRunning by mutableStateOf(false)
    private var captureGranted by mutableStateOf(false)
    private var overlayGranted by mutableStateOf(false)
    private var floatingView: TextView? = null

    private val captureLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            captureGranted = it.resultCode == RESULT_OK
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        overlayGranted = Settings.canDrawOverlays(this)

        setContent {
            var selectedMenu by remember { mutableStateOf("Beranda") }

            MaterialTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Dark)
                        .padding(18.dp)
                ) {
                    Text(
                        "TinoXd Asisten",
                        color = Color.White,
                        fontSize = 27.sp
                    )
                    Text(
                        "SMART LEARNING ASSISTANT",
                        color = Purple,
                        fontSize = 11.sp
                    )

                    Spacer(Modifier.height(22.dp))

                    when (selectedMenu) {
                        "Beranda" -> {
                            Text("Dashboard", color = Color.White, fontSize = 21.sp)
                            Spacer(Modifier.height(12.dp))

                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = CardColor
                                ),
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Column(Modifier.padding(18.dp)) {
                                    Text(
                                        if (assistantRunning) "ASISTEN AKTIF" else "ASISTEN NONAKTIF",
                                        color = Purple,
                                        fontSize = 13.sp
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "Siap membantu kegiatan belajarmu.",
                                        color = Color.White
                                    )
                                    Spacer(Modifier.height(15.dp))

                                    Button(
                                        onClick = { startAssistant() },
                                        enabled = !assistantRunning && overlayGranted,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Purple
                                        )
                                    ) {
                                        Text("MULAI ASISTEN")
                                    }

                                    OutlinedButton(
                                        onClick = { stopAssistant() },
                                        enabled = assistantRunning,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("BERHENTI")
                                    }
                                }
                            }

                            Spacer(Modifier.height(20.dp))
                            Text("Menu Fitur", color = Color.White, fontSize = 20.sp)
                            Spacer(Modifier.height(12.dp))

                            val features = listOf(
                                "AI Pemecah Soal" to "Bantuan menjawab dan memahami soal.",
                                "Baca Layar (OCR)" to "Mengenali teks dari layar dengan izin.",
                                "Verifikasi Jawaban" to "Memeriksa kembali jawaban.",
                                "Mode Belajar" to "Latihan soal dan pembahasan.",
                                "Target Nilai" to "Atur target latihan 85–90.",
                                "Riwayat Belajar" to "Melihat hasil latihan."
                            )

                            features.forEach { feature ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 5.dp)
                                        .clickable { selectedMenu = feature.first },
                                    colors = CardDefaults.cardColors(
                                        containerColor = CardColor
                                    ),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(Modifier.weight(1f)) {
                                            Text(feature.first, color = Color.White)
                                            Text(
                                                feature.second,
                                                color = Color.LightGray,
                                                fontSize = 12.sp
                                            )
                                        }
                                        Text("›", color = Purple, fontSize = 25.sp)
                                    }
                                }
                            }
                        }

                        "Pengaturan" -> {
                            Text("Pengaturan", color = Color.White, fontSize = 23.sp)
                            Spacer(Modifier.height(20.dp))

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
                                Text("Izin Pop-up")
                            }

                            Button(
                                onClick = {
                                    val manager = getSystemService(
                                        MEDIA_PROJECTION_SERVICE
                                    ) as android.media.projection.MediaProjectionManager

                                    captureLauncher.launch(
                                        manager.createScreenCaptureIntent()
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Izin Baca Layar")
                            }

                            Text(
                                "Pop-up: ${if (overlayGranted) "Diizinkan" else "Belum diizinkan"}",
                                color = Color.White
                            )
                            Text(
                                "Izin tangkap layar: ${if (captureGranted) "Diberikan" else "Belum diberikan"}",
                                color = Color.White
                            )
                        }

                        else -> {
                            Text(
                                selectedMenu,
                                color = Color.White,
                                fontSize = 24.sp
                            )
                            Spacer(Modifier.height(15.dp))

                            Text(
                                when (selectedMenu) {
                                    "AI Pemecah Soal" ->
                                        "Fitur bantuan AI untuk memahami dan membahas soal."
                                    "Baca Layar (OCR)" ->
                                        "Fitur pembacaan teks layar akan disambungkan setelah izin dan OCR dibuat."
                                    "Verifikasi Jawaban" ->
                                        "Pemeriksaan jawaban akan disambungkan dengan mesin AI."
                                    "Mode Belajar" ->
                                        "Tempat latihan soal dan pembahasan."
                                    "Target Nilai" ->
                                        "Target latihan: 85–90. Nilai ini adalah sasaran, bukan jaminan hasil."
                                    "Riwayat Belajar" ->
                                        "Riwayat hasil latihan akan tersedia setelah penyimpanan dibuat."
                                    else -> "Menu fitur TinoXd Asisten."
                                },
                                color = Color.LightGray,
                                fontSize = 16.sp
                            )

                            Spacer(Modifier.height(20.dp))
                            Text(
                                "Status: Belum tersambung",
                                color = Purple
                            )
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text(
                            "BERANDA",
                            color = if (selectedMenu == "Beranda") Purple else Color.White,
                            modifier = Modifier.clickable { selectedMenu = "Beranda" }
                        )
                        Text(
                            "PENGATURAN",
                            color = if (selectedMenu == "Pengaturan") Purple else Color.White,
                            modifier = Modifier.clickable { selectedMenu = "Pengaturan" }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        overlayGranted = Settings.canDrawOverlays(this)
    }

    private fun startAssistant() {
        if (!Settings.canDrawOverlays(this) || floatingView != null) return

        val windowManager =
            getSystemService(WINDOW_SERVICE) as WindowManager

        val view = TextView(this).apply {
            text = "TinoXd Asisten\nPop-up aktif"
            textSize = 15f
            setTextColor(android.graphics.Color.WHITE)
            setPadding(28, 22, 28, 22)
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
            val manager = getSystemService(WINDOW_SERVICE) as WindowManager
            manager.removeView(it)
        }
        floatingView = null
        assistantRunning = false
    }

    override fun onDestroy() {
        stopAssistant()
        super.onDestroy()
    }
}
