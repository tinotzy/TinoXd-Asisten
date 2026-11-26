
const MODES = {
  solve: "Selesaikan soal langkah demi langkah dan berikan jawaban akhir.",
  verify: "Periksa jawaban pengguna, tentukan benar atau salah, dan jelaskan kesalahannya.",
  learn: "Ajarkan materi secara bertahap dengan contoh sederhana.",
  auto: "Bantu menyusun jawaban latihan secara lengkap dan jelas."
};

export default async function handler(req, res) {
  if (req.method !== "POST") {
    return res.status(405).json({ error: "Method not allowed" });
  }

  const apiKey = process.env.GEMINI_API_KEY;

  if (!apiKey) {
    return res.status(500).json({
      error: "GEMINI_API_KEY belum diatur di Vercel."
    });
  }

  const { message, mode = "solve", userAnswer = "" } = req.body || {};

  if (
    typeof message !== "string" ||
    !message.trim() ||
    !Object.prototype.hasOwnProperty.call(MODES, mode)
  ) {
    return res.status(400).json({ error: "Input tidak valid." });
  }

  try {
    const response = await fetch(
      "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" +
        encodeURIComponent(apiKey),
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          contents: [{
            role: "user",
            parts: [{
              text:
                "Kamu adalah TinoXd Asisten, AI tutor berbahasa Indonesia. " +
                MODES[mode] +
                "\nJawaban pengguna: " + userAnswer +
                "\nPertanyaan: " + message
            }]
          }]
        })
      }
    );

    const data = await response.json();

    if (!response.ok) {
      return res.status(502).json({
        error: data.error?.message || "Gemini gagal merespons."
      });
    }

    const answer = data.candidates?.[0]?.content?.parts
      ?.map(part => part.text || "")
      .join("\n");

    return res.status(200).json({
      answer: answer || "Jawaban kosong."
    });

  } catch (error) {
    return res.status(500).json({
      error: "Server bermasalah."
    });
  }
}
