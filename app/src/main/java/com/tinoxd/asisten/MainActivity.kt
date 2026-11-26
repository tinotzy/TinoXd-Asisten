
package com.tinoxd.asisten

import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TinoXdApp()
        }
    }
}

@Composable
fun TinoXdApp() {
    val purple = Color(0xFF9B7BFF)
    val dark = Color(0xFF10111A)

    var question by remember { mutableStateOf("") }
    var userAnswer by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("") }
    var selectedMode by remember { mutableStateOf("solve") }
    var loading by remember { mutableStateOf(false) }
    var history by remember { mutableStateOf("") }

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

        Spacer(Modifier.height(22.dp))

        Text("PILIH FITUR", color = purple)

        Spacer(Modifier.height(10.dp))

        val modes = listOf(
            "AI Pemecah Soal" to "solve",
            "Verifikasi Jawaban" to "verify",
            "Mode Belajar" to "learn",
            "Jawab Otomatis" to "auto"
        )

        modes.forEach { (label, mode) ->
            Button(
                onClick = { selectedMode = mode },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedMode == mode)
                        purple else Color(0xFF29243A)
                )
            ) {
                Text(label)
            }
            Spacer(Modifier.height(6.dp))
        }

        Spacer(Modifier.height(15.dp))

        Text("PERTANYAAN / MATERI", color = Color.White)

        OutlinedTextField(
            value = question,
            onValueChange = { question = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Tulis soal atau materi di sini...") },
            minLines = 3,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = purple,
                unfocusedBorderColor = Color.Gray
            )
        )

        if (selectedMode == "verify") {
            Spacer(Modifier.height(12.dp))
            Text("JAWABAN KAMU", color = Color.White)

            OutlinedTextField(
                value = userAnswer,
                onValueChange = { userAnswer = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Masukkan jawaban yang ingin diperiksa...") },
                minLines = 2,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = purple,
                    unfocusedBorderColor = Color.Gray
                )
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

                        android.os.Handler(mainLooper).post {
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
            Text(if (loading) "SEDANG MEMPROSES..." else "KIRIM KE TINOXD AI")
        }

        Spacer(Modifier.height(22.dp))

        Text("HASIL JAWABAN", color = purple, fontSize = 17.sp)

        Spacer(Modifier.height(8.dp))

        Text(
            answer.ifBlank { "Jawaban AI akan muncul di sini." },
            color = Color.White,
            fontSize = 15.sp
        )

        Spacer(Modifier.height(25.dp))

        Text("RIWAYAT SESI INI", color = purple, fontSize = 17.sp)

        Spacer(Modifier.height(8.dp))

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
        connection = (URL(
            "https://tinoxd-asisten.vercel.app/api/chat"
        ).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 20000
            readTimeout = 60000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
        }

        val body = JSONObject().apply {
            put("message", message)
            put("mode", mode)
            put("userAnswer", userAnswer)
        }

        connection.outputStream.use {
            it.write(body.toString().toByteArray(Charsets.UTF_8))
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
            json.optString("answer", "AI tidak memberikan jawaban.")
        } else {
            "Server error ($code): " +
                json.optString("error", "Terjadi kesalahan.")
        }

    } catch (e: Exception) {
        "Gagal terhubung: ${e.message ?: "Periksa koneksi internet."}"
    } finally {
        connection?.disconnect()
    }
}
