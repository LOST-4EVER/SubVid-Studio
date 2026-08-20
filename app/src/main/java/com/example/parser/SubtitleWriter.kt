package com.example.parser

import com.example.model.SubtitleCue
import com.example.model.SubtitleFormat
import com.example.model.SubtitleTrack
import java.util.Locale

object SubtitleWriter {

    fun generate(track: SubtitleTrack, format: SubtitleFormat, videoWidth: Int = 1920, videoHeight: Int = 1080): String {
        return when (format) {
            SubtitleFormat.SRT -> generateSrt(track)
            SubtitleFormat.VTT -> generateVtt(track)
            SubtitleFormat.ASS, SubtitleFormat.SSA -> generateAss(track, videoWidth, videoHeight)
        }
    }

    fun generateSrt(track: SubtitleTrack): String {
        val sb = StringBuilder()
        track.cues.forEachIndexed { index, cue ->
            sb.append(index + 1).append("\n")
            sb.append(formatSrtTime(cue.startTimeMs))
                .append(" --> ")
                .append(formatSrtTime(cue.endTimeMs))
                .append("\n")

            // Embed styling/formatting if customized
            var text = cue.text
            if (cue.style.isBold) text = "<b>$text</b>"
            if (cue.style.isItalic) text = "<i>$text</i>"
            if (cue.style.isUnderline) text = "<u>$text</u>"

            sb.append(text).append("\n\n")
        }
        return sb.toString().trimEnd() + "\n"
    }

    fun generateVtt(track: SubtitleTrack): String {
        val sb = StringBuilder()
        sb.append("WEBVTT - ").append(track.title).append("\n\n")

        track.cues.forEachIndexed { index, cue ->
            sb.append(index + 1).append("\n")
            val linePercent = (cue.posY * 100).toInt().coerceIn(5, 95)
            val posPercent = (cue.posX * 100).toInt().coerceIn(5, 95)

            sb.append(formatVttTime(cue.startTimeMs))
                .append(" --> ")
                .append(formatVttTime(cue.endTimeMs))
                .append(" line:").append(linePercent).append("%")
                .append(" position:").append(posPercent).append("%")
                .append(" align:center\n")

            var text = cue.text
            if (cue.style.isBold) text = "<b>$text</b>"
            if (cue.style.isItalic) text = "<i>$text</i>"
            sb.append(text).append("\n\n")
        }
        return sb.toString().trimEnd() + "\n"
    }

    fun generateAss(track: SubtitleTrack, videoWidth: Int = 1920, videoHeight: Int = 1080): String {
        val sb = StringBuilder()
        sb.append("[Script Info]\n")
        sb.append("Title: ").append(track.title).append("\n")
        sb.append("ScriptType: v4.00+\n")
        sb.append("WrapStyle: 0\n")
        sb.append("PlayResX: ").append(videoWidth).append("\n")
        sb.append("PlayResY: ").append(videoHeight).append("\n")
        sb.append("ScaledBorderAndShadow: yes\n\n")

        sb.append("[V4+ Styles]\n")
        sb.append("Format: Name, Fontname, Fontsize, PrimaryColour, SecondaryColour, OutlineColour, BackColour, Bold, Italic, Underline, StrikeOut, ScaleX, ScaleY, Spacing, Angle, BorderStyle, Outline, Shadow, Alignment, MarginL, MarginR, MarginV, Encoding\n")
        sb.append("Style: Default,Arial,52,&H00FFFFFF,&H000000FF,&H00000000,&H80000000,-1,0,0,0,100,100,0,0,1,3,2,2,10,10,20,1\n\n")

        sb.append("[Events]\n")
        sb.append("Format: Layer, Start, End, Style, Name, MarginL, MarginR, MarginV, Effect, Text\n")

        track.cues.forEach { cue ->
            val startStr = formatAssTime(cue.startTimeMs)
            val endStr = formatAssTime(cue.endTimeMs)

            val px = (cue.posX * videoWidth).toInt().coerceIn(10, videoWidth - 10)
            val py = (cue.posY * videoHeight).toInt().coerceIn(10, videoHeight - 10)

            // Convert ARGB to BGR hex for ASS format (&HBBGGRR&)
            val argb = cue.style.textColorArgb
            val r = ((argb shr 16) and 0xFF).toString(16).padStart(2, '0').uppercase()
            val g = ((argb shr 8) and 0xFF).toString(16).padStart(2, '0').uppercase()
            val b = (argb and 0xFF).toString(16).padStart(2, '0').uppercase()
            val bgrHex = "$b$g$r"

            val boldTag = if (cue.style.isBold) "\\b1" else "\\b0"
            val italicTag = if (cue.style.isItalic) "\\i1" else ""
            val posTag = "\\pos($px,$py)"
            val colorTag = "\\c&H$bgrHex&"

            val escapedText = cue.text.replace("\n", "\\N")
            val formattedDialogue = "{$posTag$colorTag$boldTag$italicTag}$escapedText"

            sb.append("Dialogue: 0,$startStr,$endStr,Default,,0,0,0,,")
                .append(formattedDialogue)
                .append("\n")
        }

        return sb.toString()
    }

    private fun formatSrtTime(ms: Long): String {
        val totalSec = ms / 1000
        val millis = ms % 1000
        val s = totalSec % 60
        val m = (totalSec / 60) % 60
        val h = totalSec / 3600
        return String.format(Locale.US, "%02d:%02d:%02d,%03d", h, m, s, millis)
    }

    private fun formatVttTime(ms: Long): String {
        val totalSec = ms / 1000
        val millis = ms % 1000
        val s = totalSec % 60
        val m = (totalSec / 60) % 60
        val h = totalSec / 3600
        return String.format(Locale.US, "%02d:%02d:%02d.%03d", h, m, s, millis)
    }

    private fun formatAssTime(ms: Long): String {
        val totalSec = ms / 1000
        val centis = (ms % 1000) / 10
        val s = totalSec % 60
        val m = (totalSec / 60) % 60
        val h = totalSec / 3600
        return String.format(Locale.US, "%d:%02d:%02d.%02d", h, m, s, centis)
    }
}
