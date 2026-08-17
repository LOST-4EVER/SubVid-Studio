package com.example.parser

import android.util.Log
import com.example.model.SubtitleAlignment
import com.example.model.SubtitleCue
import com.example.model.SubtitleFormat
import com.example.model.SubtitleStyle
import com.example.model.SubtitleTrack
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.charset.Charset
import java.util.UUID
import java.util.regex.Pattern

object SubtitleParser {

    private const val TAG = "SubtitleParser"

    private val MULTIPLE_NEWLINES_REGEX = Regex("\n{2,}")
    private val ASS_OVERRIDE_TAGS_REGEX = Regex("\\{[^}]*\\}")
    private val HTML_TAGS_REGEX = Regex("<[^>]*>")

    private val SRT_TIME_PATTERN = Pattern.compile(
        "(?:(\\d{1,2}):)?(\\d{1,2}):(\\d{2})[,.](\\d{2,3})\\s*-->\\s*(?:(\\d{1,2}):)?(\\d{1,2}):(\\d{2})[,.](\\d{2,3})"
    )
    private val VTT_TIME_PATTERN = Pattern.compile(
        "(?:(\\d{1,2}):)?(\\d{2}):(\\d{2})[,.](\\d{2,3})\\s*-->\\s*(?:(\\d{1,2}):)?(\\d{2}):(\\d{2})[,.](\\d{2,3})(.*)"
    )
    private val ASS_POS_PATTERN = Pattern.compile("\\\\pos\\s*\\(\\s*([\\d.]+)\\s*,\\s*([\\d.]+)\\s*\\)")
    private val ASS_AN_PATTERN = Pattern.compile("\\\\an([1-9])")
    private val SSA_A_PATTERN = Pattern.compile("\\\\a([1-9]|1[0-1])")
    private val ASS_COLOR_PATTERN = Pattern.compile("\\\\(?:1)?c&H([0-9A-Fa-f]{6})&")

    fun parse(inputStream: InputStream, filename: String = ""): SubtitleTrack {
        val bytes = inputStream.use { it.readBytes() }
        val content = decodeBytesToString(bytes)
        return parseString(content, filename)
    }

    /**
     * Robust charset detector supporting UTF-8, UTF-16LE, UTF-16BE with BOMs,
     * and fallback to ISO-8859-1 / Windows-1252 if UTF-8 contains invalid byte sequences.
     */
    fun decodeBytesToString(bytes: ByteArray): String {
        if (bytes.isEmpty()) return ""

        // Check BOM
        if (bytes.size >= 3 && bytes[0] == 0xEF.toByte() && bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte()) {
            return String(bytes, 3, bytes.size - 3, Charsets.UTF_8)
        }
        if (bytes.size >= 2 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xFE.toByte()) {
            return String(bytes, 2, bytes.size - 2, Charsets.UTF_16LE)
        }
        if (bytes.size >= 2 && bytes[0] == 0xFE.toByte() && bytes[1] == 0xFF.toByte()) {
            return String(bytes, 2, bytes.size - 2, Charsets.UTF_16BE)
        }

        // Try decoding as UTF-8
        return try {
            val decoder = Charsets.UTF_8.newDecoder()
            val charBuffer = decoder.decode(java.nio.ByteBuffer.wrap(bytes))
            charBuffer.toString()
        } catch (_: Exception) {
            try {
                String(bytes, Charset.forName("windows-1252"))
            } catch (_: Exception) {
                String(bytes, Charsets.ISO_8859_1)
            }
        }
    }

    fun parseString(content: String, filename: String = ""): SubtitleTrack {
        val cleanContent = content.removePrefix("\uFEFF")
            .replace("\r\n", "\n")
            .replace("\r", "\n")
        val lowerFilename = filename.lowercase()

        val format = when {
            lowerFilename.endsWith(".ass") || cleanContent.contains("[Script Info]", ignoreCase = true) || cleanContent.contains("[V4+ Styles]", ignoreCase = true) -> SubtitleFormat.ASS
            lowerFilename.endsWith(".ssa") || cleanContent.contains("[V4 Styles]", ignoreCase = true) -> SubtitleFormat.SSA
            lowerFilename.endsWith(".vtt") || cleanContent.startsWith("WEBVTT", ignoreCase = true) -> SubtitleFormat.VTT
            else -> SubtitleFormat.SRT
        }

        val cues = when (format) {
            SubtitleFormat.ASS, SubtitleFormat.SSA -> parseAss(cleanContent)
            SubtitleFormat.VTT -> parseVtt(cleanContent)
            SubtitleFormat.SRT -> parseSrt(cleanContent)
        }

        val title = if (filename.isNotBlank()) {
            filename.substringBeforeLast('.')
        } else {
            "Subtitles (${format.extension.uppercase()})"
        }

        return SubtitleTrack(
            title = title,
            format = format,
            cues = cues.sortedBy { it.startTimeMs }
        )
    }

    // -------------------------------------------------------------
    // SRT PARSER
    // -------------------------------------------------------------
    private fun parseSrt(content: String): List<SubtitleCue> {
        val cues = mutableListOf<SubtitleCue>()
        // Normalize multiple blank lines to double newline, then split blocks
        val normalized = content.replace(MULTIPLE_NEWLINES_REGEX, "\n\n")
        val blocks = normalized.split("\n\n")

        for (block in blocks) {
            val lines = block.trim().lines().filter { it.isNotBlank() }
            if (lines.isEmpty()) continue

            var timeLineIndex = -1
            for (i in lines.indices) {
                if (lines[i].contains("-->")) {
                    timeLineIndex = i
                    break
                }
            }

            if (timeLineIndex == -1) continue

            val timeMatcher = SRT_TIME_PATTERN.matcher(lines[timeLineIndex])
            if (timeMatcher.find()) {
                val startH = timeMatcher.group(1)?.toLongOrNull() ?: 0L
                val startM = timeMatcher.group(2)!!.toLong()
                val startS = timeMatcher.group(3)!!.toLong()
                var startMs = timeMatcher.group(4)!!.toLong()
                if (timeMatcher.group(4)!!.length == 2) startMs *= 10

                val endH = timeMatcher.group(5)?.toLongOrNull() ?: 0L
                val endM = timeMatcher.group(6)!!.toLong()
                val endS = timeMatcher.group(7)!!.toLong()
                var endMs = timeMatcher.group(8)!!.toLong()
                if (timeMatcher.group(8)!!.length == 2) endMs *= 10

                val startTotalMs = parseTime(startH, startM, startS, startMs)
                val endTotalMs = parseTime(endH, endM, endS, endMs)

                val textLines = lines.drop(timeLineIndex + 1)
                val rawText = textLines.joinToString("\n")

                val parsedMeta = extractAssPositionAndStyle(rawText)
                val cleanText = stripHtmlAndAssTags(rawText)

                if (cleanText.isNotBlank()) {
                    cues.add(
                        SubtitleCue(
                            id = UUID.randomUUID().toString(),
                            startTimeMs = startTotalMs,
                            endTimeMs = endTotalMs.coerceAtLeast(startTotalMs + 100L),
                            text = cleanText,
                            posX = parsedMeta.posX ?: 0.50f,
                            posY = parsedMeta.posY ?: 0.85f,
                            alignment = parsedMeta.alignment ?: SubtitleAlignment.BOTTOM_CENTER,
                            style = parsedMeta.style ?: SubtitleStyle()
                        )
                    )
                }
            }
        }
        return cues
    }

    // -------------------------------------------------------------
    // WEBVTT PARSER
    // -------------------------------------------------------------
    private fun parseVtt(content: String): List<SubtitleCue> {
        val cues = mutableListOf<SubtitleCue>()
        val normalized = content.replace(MULTIPLE_NEWLINES_REGEX, "\n\n")
        val blocks = normalized.split("\n\n")

        for (block in blocks) {
            val lines = block.trim().lines().filter { it.isNotBlank() }
            if (lines.isEmpty()) continue

            // Skip WEBVTT header or NOTE blocks
            if (lines[0].startsWith("WEBVTT", ignoreCase = true) || lines[0].startsWith("NOTE", ignoreCase = true) || lines[0].startsWith("STYLE", ignoreCase = true)) {
                continue
            }

            var timeLineIndex = -1
            for (i in lines.indices) {
                if (lines[i].contains("-->")) {
                    timeLineIndex = i
                    break
                }
            }

            if (timeLineIndex == -1) continue

            val timeMatcher = VTT_TIME_PATTERN.matcher(lines[timeLineIndex])
            if (timeMatcher.find()) {
                val startH = timeMatcher.group(1)?.toLongOrNull() ?: 0L
                val startM = timeMatcher.group(2)!!.toLong()
                val startS = timeMatcher.group(3)!!.toLong()
                var startMs = timeMatcher.group(4)!!.toLong()
                if (timeMatcher.group(4)!!.length == 2) startMs *= 10

                val endH = timeMatcher.group(5)?.toLongOrNull() ?: 0L
                val endM = timeMatcher.group(6)!!.toLong()
                val endS = timeMatcher.group(7)!!.toLong()
                var endMs = timeMatcher.group(8)!!.toLong()
                if (timeMatcher.group(8)!!.length == 2) endMs *= 10

                val settings = timeMatcher.group(9) ?: ""

                val startTotalMs = parseTime(startH, startM, startS, startMs)
                val endTotalMs = parseTime(endH, endM, endS, endMs)

                // Parse VTT position & alignment settings
                var posX = 0.50f
                var posY = 0.85f
                var align = SubtitleAlignment.BOTTOM_CENTER

                if (settings.isNotBlank()) {
                    if (settings.contains("line:")) {
                        val lineVal = settings.substringAfter("line:").substringBefore(" ").removeSuffix("%").toFloatOrNull()
                        if (lineVal != null) {
                            posY = (lineVal / 100f).coerceIn(0.05f, 0.95f)
                        }
                    }
                    if (settings.contains("position:")) {
                        val posVal = settings.substringAfter("position:").substringBefore(" ").removeSuffix("%").toFloatOrNull()
                        if (posVal != null) {
                            posX = (posVal / 100f).coerceIn(0.05f, 0.95f)
                        }
                    }
                    if (settings.contains("align:start") || settings.contains("align:left")) {
                        align = if (posY < 0.33f) SubtitleAlignment.TOP_START else if (posY < 0.66f) SubtitleAlignment.CENTER_START else SubtitleAlignment.BOTTOM_START
                    } else if (settings.contains("align:end") || settings.contains("align:right")) {
                        align = if (posY < 0.33f) SubtitleAlignment.TOP_END else if (posY < 0.66f) SubtitleAlignment.CENTER_END else SubtitleAlignment.BOTTOM_END
                    } else if (settings.contains("align:center") || settings.contains("align:middle")) {
                        align = if (posY < 0.33f) SubtitleAlignment.TOP_CENTER else if (posY < 0.66f) SubtitleAlignment.CENTER else SubtitleAlignment.BOTTOM_CENTER
                    }
                }

                val textLines = lines.drop(timeLineIndex + 1)
                val rawText = textLines.joinToString("\n")
                val cleanText = stripHtmlAndAssTags(rawText)

                if (cleanText.isNotBlank()) {
                    cues.add(
                        SubtitleCue(
                            id = UUID.randomUUID().toString(),
                            startTimeMs = startTotalMs,
                            endTimeMs = endTotalMs.coerceAtLeast(startTotalMs + 100L),
                            text = cleanText,
                            posX = posX,
                            posY = posY,
                            alignment = align
                        )
                    )
                }
            }
        }
        return cues
    }

    // -------------------------------------------------------------
    // ASS / SSA PARSER
    // -------------------------------------------------------------
    private fun parseAss(content: String): List<SubtitleCue> {
        val cues = mutableListOf<SubtitleCue>()
        var playResX = 1920f
        var playResY = 1080f
        var isSsaV4 = content.contains("[V4 Styles]", ignoreCase = true)

        val lines = content.lines()
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("PlayResX:", ignoreCase = true)) {
                trimmed.substringAfter(":").trim().toFloatOrNull()?.let { if (it > 0) playResX = it }
            } else if (trimmed.startsWith("PlayResY:", ignoreCase = true)) {
                trimmed.substringAfter(":").trim().toFloatOrNull()?.let { if (it > 0) playResY = it }
            } else if (trimmed.startsWith("Dialogue:", ignoreCase = true)) {
                val dialogueBody = trimmed.substringAfter("Dialogue:").trim()
                val parts = dialogueBody.split(",", limit = 10)
                if (parts.size >= 10) {
                    val startStr = parts[1].trim()
                    val endStr = parts[2].trim()
                    val rawText = parts[9]

                    val startMs = parseAssTimestamp(startStr)
                    val endMs = parseAssTimestamp(endStr)

                    val meta = extractAssPositionAndStyle(rawText, playResX, playResY, isSsaV4)
                    val cleanText = stripHtmlAndAssTags(rawText)

                    if (cleanText.isNotBlank()) {
                        cues.add(
                            SubtitleCue(
                                id = UUID.randomUUID().toString(),
                                startTimeMs = startMs,
                                endTimeMs = endMs.coerceAtLeast(startMs + 100L),
                                text = cleanText,
                                posX = meta.posX ?: 0.50f,
                                posY = meta.posY ?: 0.85f,
                                alignment = meta.alignment ?: SubtitleAlignment.BOTTOM_CENTER,
                                style = meta.style ?: SubtitleStyle()
                            )
                        )
                    }
                }
            }
        }
        return cues
    }

    private fun parseAssTimestamp(ts: String): Long {
        try {
            val parts = ts.split(":")
            if (parts.size == 3) {
                val h = parts[0].toLong()
                val m = parts[1].toLong()
                val secParts = parts[2].split(".")
                val s = secParts[0].toLong()
                val centiOrMilli = secParts.getOrNull(1)?.toLong() ?: 0L
                val ms = if (centiOrMilli < 100) centiOrMilli * 10 else centiOrMilli
                return parseTime(h, m, s, ms)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing ASS timestamp: $ts", e)
        }
        return 0L
    }

    private fun parseTime(hours: Long, minutes: Long, seconds: Long, millis: Long): Long {
        return (hours * 3600 + minutes * 60 + seconds) * 1000 + millis
    }

    private data class ParsedAssMeta(
        val posX: Float? = null,
        val posY: Float? = null,
        val alignment: SubtitleAlignment? = null,
        val style: SubtitleStyle? = null
    )

    private fun extractAssPositionAndStyle(
        text: String,
        playResX: Float = 1920f,
        playResY: Float = 1080f,
        isSsaV4: Boolean = false
    ): ParsedAssMeta {
        var posX: Float? = null
        var posY: Float? = null
        var align: SubtitleAlignment? = null
        var textColor: Long? = null
        var isBold: Boolean? = null
        var isItalic: Boolean? = null
        var isUnderline: Boolean? = null

        // Parse \pos(X,Y)
        val posMatcher = ASS_POS_PATTERN.matcher(text)
        if (posMatcher.find()) {
            val px = posMatcher.group(1)!!.toFloat()
            val py = posMatcher.group(2)!!.toFloat()
            posX = (px / playResX).coerceIn(0.02f, 0.98f)
            posY = (py / playResY).coerceIn(0.02f, 0.98f)
            align = SubtitleAlignment.CUSTOM
        }

        // Parse \an1 - \an9 (ASS numpad alignment)
        val anMatcher = ASS_AN_PATTERN.matcher(text)
        if (anMatcher.find()) {
            val num = anMatcher.group(1)!!.toInt()
            align = when (num) {
                1 -> SubtitleAlignment.BOTTOM_START
                2 -> SubtitleAlignment.BOTTOM_CENTER
                3 -> SubtitleAlignment.BOTTOM_END
                4 -> SubtitleAlignment.CENTER_START
                5 -> SubtitleAlignment.CENTER
                6 -> SubtitleAlignment.CENTER_END
                7 -> SubtitleAlignment.TOP_START
                8 -> SubtitleAlignment.TOP_CENTER
                9 -> SubtitleAlignment.TOP_END
                else -> SubtitleAlignment.BOTTOM_CENTER
            }
            if (posY == null) {
                posY = when (num) {
                    7, 8, 9 -> 0.12f
                    4, 5, 6 -> 0.50f
                    else -> 0.85f
                }
            }
            if (posX == null) {
                posX = when (num) {
                    1, 4, 7 -> 0.15f
                    2, 5, 8 -> 0.50f
                    3, 6, 9 -> 0.85f
                    else -> 0.50f
                }
            }
        }

        // Parse SSA \a1 - \a11 (legacy SSA alignment)
        if (align == null) {
            val aMatcher = SSA_A_PATTERN.matcher(text)
            if (aMatcher.find()) {
                val num = aMatcher.group(1)!!.toInt()
                align = when (num) {
                    1 -> SubtitleAlignment.BOTTOM_START
                    2 -> SubtitleAlignment.BOTTOM_CENTER
                    3 -> SubtitleAlignment.BOTTOM_END
                    5 -> SubtitleAlignment.TOP_START
                    6 -> SubtitleAlignment.TOP_CENTER
                    7 -> SubtitleAlignment.TOP_END
                    9 -> SubtitleAlignment.CENTER_START
                    10 -> SubtitleAlignment.CENTER
                    11 -> SubtitleAlignment.CENTER_END
                    else -> SubtitleAlignment.BOTTOM_CENTER
                }
                if (posY == null) {
                    posY = when (num) {
                        5, 6, 7 -> 0.12f
                        9, 10, 11 -> 0.50f
                        else -> 0.85f
                    }
                }
                if (posX == null) {
                    posX = when (num) {
                        1, 5, 9 -> 0.15f
                        2, 6, 10 -> 0.50f
                        3, 7, 11 -> 0.85f
                        else -> 0.50f
                    }
                }
            }
        }

        // Parse color \c&HBBGGRR& or \1c&HBBGGRR&
        val colorMatcher = ASS_COLOR_PATTERN.matcher(text)
        if (colorMatcher.find()) {
            val bgrHex = colorMatcher.group(1)!!
            val b = bgrHex.substring(0, 2).toLong(16)
            val g = bgrHex.substring(2, 4).toLong(16)
            val r = bgrHex.substring(4, 6).toLong(16)
            textColor = 0xFF000000L or (r shl 16) or (g shl 8) or b
        }

        // Parse tags
        if (text.contains("\\b1") || text.contains("<b>", ignoreCase = true)) isBold = true
        if (text.contains("\\b0")) isBold = false
        if (text.contains("\\i1") || text.contains("<i>", ignoreCase = true)) isItalic = true
        if (text.contains("\\i0")) isItalic = false
        if (text.contains("\\u1") || text.contains("<u>", ignoreCase = true)) isUnderline = true
        if (text.contains("\\u0")) isUnderline = false

        val style = if (textColor != null || isBold != null || isItalic != null || isUnderline != null) {
            SubtitleStyle(
                textColorArgb = textColor ?: 0xFFFFFFFF,
                isBold = isBold ?: true,
                isItalic = isItalic ?: false,
                isUnderline = isUnderline ?: false
            )
        } else null

        return ParsedAssMeta(posX = posX, posY = posY, alignment = align, style = style)
    }

    private fun stripHtmlAndAssTags(input: String): String {
        var res = input
        // Replace ASS linebreaks \N and \n
        res = res.replace("\\N", "\n").replace("\\n", "\n").replace("\\h", " ")
        // Remove ASS override tags { ... }
        res = res.replace(ASS_OVERRIDE_TAGS_REGEX, "")
        // Remove HTML formatting tags (<b>, <i>, <u>, <font...>, </font>)
        res = res.replace(HTML_TAGS_REGEX, "")
        return res.trim()
    }
}
