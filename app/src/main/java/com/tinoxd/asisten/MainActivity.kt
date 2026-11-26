package com.tinoxd.asisten

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {

    private val floatingAction = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        floatingAction.value =
            intent?.getStringExtra("TINXD_ACTION")

        setContent {
            TinoXdApp(
                activity = this,
                requestedAction = floatingAction.value
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        floatingAction.value =
            intent.getStringExtra("TINXD_ACTION")
    }
}

@Composable
fun TinoXdApp(
    activity: Activity,
    requestedAction: String? = null
) {
    val purple = Color(0xFF9B7BFF)
    val dark = Color(0xFF10111A)

    var question by remember { mutableStateOf("") }
    var userAnswer by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("") }

    var selectedMode by remember {
        mutableStateOf("solve")
    }

    var loading by remember { mutableStateOf(false) }
    var history by remember { mutableStateOf("") }

    LaunchedEffect(requestedAction) {
        selectedMode = when (requestedAction) {
            "SOLVER" -> "solve"
            "VERIFY" -> "verify"
            "AUTO_ANSWER" -> "auto"
            "HOME" -> "solve"
            else -> selectedMode
        }
    }

    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(dark)
            .verticalScroll(scroll)
            .padding(20.dp)
    ) {
        Text(
            "TinoXd Asisten",
            color = Color.White,
            fontSize = 29.sp
        )

        Text(
            "SMART LEARNING AI",
            color = purple,
            fontSize = 13.sp
        )

        Spacer(Modifier.height(20.dp))

        Text("FLOATING ASSISTANT", color = purple)

        Button(
            onClick = {
                if (Settings.canDrawOverlays(activity)) {
                    activity.startService(
                        Intent(
                            activity,
                            FloatingWidgetService::class.java
                        )
                    )
                } else {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${activity.packageName}")
                    )
                    activity.startActivity(intent)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = purple
            )
        ) {
            Text("AKTIFKAN POP-UP MENGAMBANG")
        }

        Spacer(Modifier.height(22.dp))

        Text("PILIH FITUR", color = purple)

        val modes = listOf(
            "AI Pemecah Soal" to "solve",
            "Verifikasi Jawaban" to "verify",
            "Mode Belajar" to "learn",
            "Jawab Otomatis (Latihan)" to "auto"
        )

        modes.forEach { (label, mode) ->
            Button(
                onClick = {
                    selectedMode = mode
                    answer = ""
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor =
                        if (selectedMode == mode)
                            purple
                        else Color(0xFF29243A)
                )
            ) {
                Text(label)
            }
        }

        Spacer(Modifier.height(15.dp))

        Text("MODE AKTIF: ${selectedMode.uppercase()}", color = purple)

        Spacer(Modifier.height(12.dp))

        Text("PERTANYAAN / MATERI", color = Color.White)

        OutlinedTextField(
            value = question,
            onValueChange = { question = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text("Tulis soal atau materi latihan...")
            },
            minLines = 3
        )

        if (selectedMode == "verify") {
            Spacer(Modifier.height(12.dp))

            Text("JAWABAN KAMU", color = Color.White)

            OutlinedTextField(
                value = userAnswer,
                onValueChange = { userAnswer = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("Masukkan jawaban untuk diperiksa...")
                },
                minLines = 2
            )
        }

        Spacer(Modifier.height(15.dp))

        Button(
            onClick = {
                if (question.isNotBlank() && !loading) {
                    loading = true
                    answer = ""

                    val q = question
                    val a = userAnswer
                    val mode = selectedMode

                    Executors.newSingleThreadExecutor().execute {
                        val result = requestAI(q, mode, a)

                        android.os.Handler(
                            android.os.Looper.getMainLooper()
                        ).post {
                            answer = result
                            loading = false
                            history = "$history\n\n$q\n$result"
                        }
                    }
                }
            },
            enabled = !loading && question.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = purple
            )
        ) {
            Text(
                if (loading)
                    "MEMPROSES..."
                else
                    "KIRIM KE TINOXD AI"
            )
        }

        Spacer(Modifier.height(22.dp))

        Text(
            "HASIL JAWABAN",
            color = purple,
            fontSize = 17.sp
        )

        Text(
            answer.ifBlank {
                "Jawaban AI akan muncul di sini."
            },
            color = Color.White
        )

        Spacer(Modifier.height(20.dp))

        Text(
            "RIWAYAT SESI INI",
            color = purple,
            fontSize = 17.sp
        )

        Text(
            history.ifBlank { "Belum ada riwayat." },
            color = Color.LightGray,
            fontSize = 13.sp
        )
    }
}

fun requestAI(
    message: String,
    mode: String,
    userAnswer: String
): String {
    var connection: HttpURLConnection? = null

    return try {
        connection = (
            URL("https://tinoxd-asisten.vercel.app/api/chat")
                .openConnection() as HttpURLConnection
        ).apply {
            requestMethod = "POST"
            connectTimeout = 20000
            readTimeout = 60000
            doOutput = true
            setRequestProperty(
                "Content-Type",
                "application/json"
            )
        }

        val body = JSONObject().apply {
            put("message", message)
            put("mode", mode)
            put("userAnswer", userAnswer)
        }

        connection.outputStream.use {
            it.write(
                body.toString().toByteArray(Charsets.UTF_8)
            )
        }

        val code = connection.responseCode

        val stream = if (code in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream
        }

        val responseText = stream?.bufferedReader()?.use {
            it.readText()
        } ?: ""

        val json = JSONObject(responseText)

        if (code in 200..299) {
            json.optString(
                "answer",
                "AI tidak memberikan jawaban."
            )
        } else {
            "Server error ($code): " +
                json.optString(
                    "error",
                    "Terjadi kesalahan."
                )
        }

    } catch (e: Exception) {
        "Gagal terhubung: ${
            e.message ?: "Periksa koneksi internet."
        }"
    } finally {
        connection?.disconnect()
    }
}
