
const MODES = {
  solve: "Selesaikan soal dengan langkah jelas dan jawaban akhir.",
  verify: "Periksa jawaban dan jelaskan kesalahannya.",
  learn: "Ajarkan materi secara bertahap dengan contoh.",
  auto: "Susun jawaban untuk latihan."
};

export default async function handler(req, res) {
  if (req.method !== "POST") {
    return res.status(405).json({ error: "Method not allowed" });
  }

  const key = process.env.GEMINI_API_KEY;

  if (!key) {
    return res.status(500).json({ error: "API key belum diatur." });
  }

  const { message, mode = "solve", userAnswer = "" } = req.body || {};

  if (!message || !MODES[mode]) {
    return res.status(400).json({ error: "Input tidak valid." });
  }

  try {
    const response = await fetch(
      "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" +
        encodeURIComponent(key),
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          contents: [{
            role: "user",
            parts: [{
              text:
                "Kamu TinoXd Asisten, tutor AI berbahasa Indonesia. " +
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
      return res.status(502).json({ error: "Gemini gagal merespons." });
    }

    const answer = data.candidates?.[0]?.content?.parts
      ?.map(p => p.text || "")
      .join("\n");

    return res.status(200).json({
      answer: answer || "Jawaban kosong."
    });

  } catch (error) {
    return res.status(500).json({ error: "Server bermasalah." });
  }
}
