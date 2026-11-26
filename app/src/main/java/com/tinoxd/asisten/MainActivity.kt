package com.tinoxd.asisten

import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {

    private val screenCaptureLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            captureGranted = result.resultCode == RESULT_OK
        }

    private var captureGranted by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var overlayGranted by remember {
                mutableStateOf(Settings.canDrawOverlays(this))
            }

            val background = Color(0xFF10111A)
            val purple = Color(0xFF8257E5)

            MaterialTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(background)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "TinoXd Asisten",
                        color = Color.White,
                        fontSize = 30.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "Asisten belajar pintar",
                        color = Color.LightGray,
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(36.dp))

                    Button(
                        onClick = {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:$packageName")
                            )
                            startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = purple
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Izinkan Pop-up")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val manager = getSystemService(
                                MEDIA_PROJECTION_SERVICE
                            ) as MediaProjectionManager

                            screenCaptureLauncher.launch(
                                manager.createScreenCaptureIntent()
                            )
                        },
                        enabled = overlayGranted,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = purple
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Izinkan Baca Layar")
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = when {
                            captureGranted -> "Izin baca layar diberikan."
                            overlayGranted -> "Pop-up diizinkan. Aktifkan izin baca layar."
                            else -> "Aktifkan izin pop-up terlebih dahulu."
                        },
                        color = Color.White,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            overlayGranted = Settings.canDrawOverlays(this@MainActivity)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.DarkGray
                        )
                    ) {
                        Text("Periksa Izin")
                    }
                }
            }
        }
    }
}
