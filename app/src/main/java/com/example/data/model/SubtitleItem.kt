package com.example.data.model

data class SubtitleItem(
    val index: Int,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val text: String
)

object SrtParser {
    fun parse(content: String): List<SubtitleItem> {
        val result = mutableListOf<SubtitleItem>()
        val blocks = content.replace("\r\n", "\n").replace("\r", "\n").split("\n\n")

        for (block in blocks) {
            val lines = block.trim().lines()
            if (lines.size < 2) continue

            val timeIndex = if (lines[0].toIntOrNull() != null && lines.size >= 2) 1 else 0
            val timeLine = lines[timeIndex]
            val times = timeLine.split("-->")
            if (times.size != 2) continue

            val startMs = parseTime(times[0].trim())
            val endMs = parseTime(times[1].trim())
            if (startMs == -1L || endMs == -1L) continue

            val text = lines.drop(timeIndex + 1).joinToString("\n").trim()
            if (text.isNotEmpty()) {
                result.add(SubtitleItem(result.size + 1, startMs, endMs, text))
            }
        }
        return result
    }

    private fun parseTime(timeStr: String): Long {
        return try {
            // format: 00:01:20,000 or 00:01:20.000
            val parts = timeStr.replace(',', '.').split(':')
            if (parts.size != 3) return -1L
            val hours = parts[0].trim().toLong()
            val minutes = parts[1].trim().toLong()
            val secParts = parts[2].trim().split('.')
            val seconds = secParts[0].trim().toLong()
            val millis = if (secParts.size > 1) {
                secParts[1].padEnd(3, '0').take(3).toLong()
            } else 0L

            (hours * 3600 + minutes * 60 + seconds) * 1000 + millis
        } catch (_: Exception) {
            -1L
        }
    }
}
