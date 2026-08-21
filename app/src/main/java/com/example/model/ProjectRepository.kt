package com.example.model

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class ProjectRepository(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("subvid_projects_store", Context.MODE_PRIVATE)
    private val projectsDir: File = File(context.filesDir, "projects").apply { if (!exists()) mkdirs() }

    fun getAllProjects(): List<StudioProject> {
        val raw = prefs.getString("projects_list", "[]") ?: "[]"
        return try {
            val jsonArray = JSONArray(raw)
            val list = mutableListOf<StudioProject>()
            for (i in 0 until jsonArray.length()) {
                val summaryObj = jsonArray.getJSONObject(i)
                val id = summaryObj.getString("id")
                // Load full project from file if available, otherwise reconstruct from summary
                val fullProject = loadProjectFromFile(id) ?: parseSummary(summaryObj)
                list.add(fullProject)
            }
            list.sortedByDescending { it.lastModifiedMs }
        } catch (e: Exception) {
            Log.e("ProjectRepository", "Error loading projects", e)
            emptyList()
        }
    }

    fun getProjectById(id: String): StudioProject? {
        return loadProjectFromFile(id) ?: getAllProjects().firstOrNull { it.id == id }
    }

    fun saveProject(project: StudioProject) {
        val updatedProject = project.copy(
            cueCount = project.subtitleTrack.cues.size,
            subtitleFileName = project.subtitleTrack.title.ifEmpty { project.subtitleFileName },
            subtitleFormat = project.subtitleTrack.format,
            lastModifiedMs = System.currentTimeMillis()
        )

        // 1. Write full project with all cues and styling to a dedicated JSON file
        saveProjectToFile(updatedProject)

        // 2. Update the projects summary list in SharedPreferences
        val current = getAllProjects().toMutableList()
        val index = current.indexOfFirst { it.id == updatedProject.id }
        if (index >= 0) {
            current[index] = updatedProject
        } else {
            current.add(0, updatedProject)
        }
        saveSummaries(current)
    }

    fun deleteProject(projectId: String) {
        try {
            val file = File(projectsDir, "$projectId.json")
            if (file.exists()) file.delete()
        } catch (e: Exception) {
            Log.e("ProjectRepository", "Failed to delete project file", e)
        }
        val current = getAllProjects().filter { it.id != projectId }
        saveSummaries(current)
    }

    private fun saveProjectToFile(project: StudioProject) {
        try {
            val file = File(projectsDir, "${project.id}.json")
            val json = serializeProject(project)
            file.writeText(json.toString(), Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e("ProjectRepository", "Error saving project to file: ${project.id}", e)
        }
    }

    private fun loadProjectFromFile(id: String): StudioProject? {
        return try {
            val file = File(projectsDir, "$id.json")
            if (!file.exists()) return null
            val raw = file.readText(Charsets.UTF_8)
            deserializeProject(JSONObject(raw))
        } catch (e: Exception) {
            Log.e("ProjectRepository", "Error loading project from file: $id", e)
            null
        }
    }

    private fun saveSummaries(list: List<StudioProject>) {
        val jsonArray = JSONArray()
        for (p in list) {
            val obj = JSONObject().apply {
                put("id", p.id)
                put("name", p.name)
                put("videoUriString", p.videoUriString)
                put("videoFileName", p.videoFileName)
                put("videoDurationMs", p.videoDurationMs)
                put("currentPositionMs", p.currentPositionMs)
                put("subtitleFileName", p.subtitleFileName)
                put("subtitleFormat", p.subtitleFormat.extension)
                put("cueCount", p.subtitleTrack.cues.size.takeIf { it > 0 } ?: p.cueCount)
                put("lastModifiedMs", p.lastModifiedMs)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString("projects_list", jsonArray.toString()).apply()
    }

    private fun parseSummary(obj: JSONObject): StudioProject {
        val name = obj.optString("name", "Untitled Project")
        val format = SubtitleFormat.fromExtension(obj.optString("subtitleFormat", "srt"))
        val cueCount = obj.optInt("cueCount", 0)
        return StudioProject(
            id = obj.getString("id"),
            name = name,
            videoUriString = obj.optString("videoUriString", ""),
            videoFileName = obj.optString("videoFileName", "No Video"),
            videoDurationMs = obj.optLong("videoDurationMs", 0L),
            currentPositionMs = obj.optLong("currentPositionMs", 0L),
            subtitleFileName = obj.optString("subtitleFileName", "No Subtitles"),
            subtitleFormat = format,
            cueCount = cueCount,
            subtitleTrack = SubtitleTrack(title = name, format = format, cues = emptyList()),
            lastModifiedMs = obj.optLong("lastModifiedMs", System.currentTimeMillis())
        )
    }

    private fun serializeProject(project: StudioProject): JSONObject {
        return JSONObject().apply {
            put("id", project.id)
            put("name", project.name)
            put("videoUriString", project.videoUriString)
            put("videoFileName", project.videoFileName)
            put("videoDurationMs", project.videoDurationMs)
            put("currentPositionMs", project.currentPositionMs)
            put("subtitleFileName", project.subtitleFileName)
            put("subtitleFormat", project.subtitleFormat.extension)
            put("cueCount", project.subtitleTrack.cues.size)
            put("lastModifiedMs", project.lastModifiedMs)

            // Subtitle Track Serialization
            val trackObj = JSONObject().apply {
                put("title", project.subtitleTrack.title)
                put("format", project.subtitleTrack.format.extension)
                put("language", project.subtitleTrack.language)
                put("defaultStyle", serializeStyle(project.subtitleTrack.defaultStyle))

                val cuesArray = JSONArray()
                for (cue in project.subtitleTrack.cues) {
                    val cueObj = JSONObject().apply {
                        put("id", cue.id)
                        put("startTimeMs", cue.startTimeMs)
                        put("endTimeMs", cue.endTimeMs)
                        put("text", cue.text)
                        put("posX", cue.posX.toDouble())
                        put("posY", cue.posY.toDouble())
                        put("alignment", cue.alignment.name)
                        put("style", serializeStyle(cue.style))
                    }
                    cuesArray.put(cueObj)
                }
                put("cues", cuesArray)
            }
            put("subtitleTrack", trackObj)
        }
    }

    private fun deserializeProject(obj: JSONObject): StudioProject {
        val id = obj.getString("id")
        val name = obj.optString("name", "Untitled Project")
        val videoUriString = obj.optString("videoUriString", "")
        val videoFileName = obj.optString("videoFileName", "No Video")
        val videoDurationMs = obj.optLong("videoDurationMs", 0L)
        val currentPositionMs = obj.optLong("currentPositionMs", 0L)
        val subtitleFileName = obj.optString("subtitleFileName", "No Subtitles")
        val subtitleFormat = SubtitleFormat.fromExtension(obj.optString("subtitleFormat", "srt"))
        val lastModifiedMs = obj.optLong("lastModifiedMs", System.currentTimeMillis())

        val trackObj = obj.optJSONObject("subtitleTrack")
        val subtitleTrack = if (trackObj != null) {
            val trackTitle = trackObj.optString("title", name)
            val trackFormat = SubtitleFormat.fromExtension(trackObj.optString("format", subtitleFormat.extension))
            val language = trackObj.optString("language", "en")
            val defaultStyle = deserializeStyle(trackObj.optJSONObject("defaultStyle"))

            val cuesList = mutableListOf<SubtitleCue>()
            val cuesArray = trackObj.optJSONArray("cues")
            if (cuesArray != null) {
                for (i in 0 until cuesArray.length()) {
                    val cObj = cuesArray.getJSONObject(i)
                    val cue = SubtitleCue(
                        id = cObj.optString("id", java.util.UUID.randomUUID().toString()),
                        startTimeMs = cObj.optLong("startTimeMs", 0L),
                        endTimeMs = cObj.optLong("endTimeMs", 2000L),
                        text = cObj.optString("text", ""),
                        posX = cObj.optDouble("posX", 0.50).toFloat(),
                        posY = cObj.optDouble("posY", 0.88).toFloat(),
                        alignment = try {
                            SubtitleAlignment.valueOf(cObj.optString("alignment", SubtitleAlignment.BOTTOM_CENTER.name))
                        } catch (_: Exception) {
                            SubtitleAlignment.BOTTOM_CENTER
                        },
                        style = deserializeStyle(cObj.optJSONObject("style"))
                    )
                    cuesList.add(cue)
                }
            }
            SubtitleTrack(
                title = trackTitle,
                format = trackFormat,
                language = language,
                cues = cuesList,
                defaultStyle = defaultStyle
            )
        } else {
            SubtitleTrack(title = name, format = subtitleFormat, cues = emptyList())
        }

        return StudioProject(
            id = id,
            name = name,
            videoUriString = videoUriString,
            videoFileName = videoFileName,
            videoDurationMs = videoDurationMs,
            currentPositionMs = currentPositionMs,
            subtitleFileName = subtitleFileName,
            subtitleFormat = subtitleFormat,
            cueCount = subtitleTrack.cues.size,
            subtitleTrack = subtitleTrack,
            lastModifiedMs = lastModifiedMs
        )
    }

    private fun serializeStyle(style: SubtitleStyle): JSONObject {
        return JSONObject().apply {
            put("fontSizeSp", style.fontSizeSp.toDouble())
            put("textColorArgb", style.textColorArgb)
            put("strokeColorArgb", style.strokeColorArgb)
            put("strokeWidthDp", style.strokeWidthDp.toDouble())
            put("backgroundColorArgb", style.backgroundColorArgb)
            put("cornerRadiusDp", style.cornerRadiusDp.toDouble())
            put("paddingHorizontalDp", style.paddingHorizontalDp.toDouble())
            put("paddingVerticalDp", style.paddingVerticalDp.toDouble())
            put("isBold", style.isBold)
            put("isItalic", style.isItalic)
            put("isUnderline", style.isUnderline)
            put("shadowRadiusDp", style.shadowRadiusDp.toDouble())
            put("shadowColorArgb", style.shadowColorArgb)
        }
    }

    private fun deserializeStyle(obj: JSONObject?): SubtitleStyle {
        if (obj == null) return SubtitleStyle()
        return SubtitleStyle(
            fontSizeSp = obj.optDouble("fontSizeSp", 22.0).toFloat(),
            textColorArgb = obj.optLong("textColorArgb", 0xFFFFFFFF),
            strokeColorArgb = obj.optLong("strokeColorArgb", 0xFF000000),
            strokeWidthDp = obj.optDouble("strokeWidthDp", 2.5).toFloat(),
            backgroundColorArgb = obj.optLong("backgroundColorArgb", 0x00000000),
            cornerRadiusDp = obj.optDouble("cornerRadiusDp", 6.0).toFloat(),
            paddingHorizontalDp = obj.optDouble("paddingHorizontalDp", 10.0).toFloat(),
            paddingVerticalDp = obj.optDouble("paddingVerticalDp", 4.0).toFloat(),
            isBold = obj.optBoolean("isBold", true),
            isItalic = obj.optBoolean("isItalic", false),
            isUnderline = obj.optBoolean("isUnderline", false),
            shadowRadiusDp = obj.optDouble("shadowRadiusDp", 4.0).toFloat(),
            shadowColorArgb = obj.optLong("shadowColorArgb", 0xCC000000)
        )
    }
}
