package com.example.model

import android.content.Context
import android.content.SharedPreferences
import com.example.model.StudioProject
import com.example.model.SubtitleFormat
import org.json.JSONArray
import org.json.JSONObject

class ProjectRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("subvid_projects_store", Context.MODE_PRIVATE)

    fun getAllProjects(): List<StudioProject> {
        val raw = prefs.getString("projects_list", "[]") ?: "[]"
        return try {
            val jsonArray = JSONArray(raw)
            val list = mutableListOf<StudioProject>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    StudioProject(
                        id = obj.getString("id"),
                        name = obj.optString("name", "Untitled Project"),
                        videoUriString = obj.optString("videoUriString", ""),
                        videoFileName = obj.optString("videoFileName", "No Video"),
                        videoDurationMs = obj.optLong("videoDurationMs", 0L),
                        subtitleFileName = obj.optString("subtitleFileName", "No Subtitles"),
                        subtitleFormat = SubtitleFormat.fromExtension(obj.optString("subtitleFormat", "srt")),
                        cueCount = obj.optInt("cueCount", 0),
                        lastModifiedMs = obj.optLong("lastModifiedMs", System.currentTimeMillis())
                    )
                )
            }
            list.sortedByDescending { it.lastModifiedMs }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveProject(project: StudioProject) {
        val current = getAllProjects().toMutableList()
        val index = current.indexOfFirst { it.id == project.id }
        if (index >= 0) {
            current[index] = project.copy(lastModifiedMs = System.currentTimeMillis())
        } else {
            current.add(0, project.copy(lastModifiedMs = System.currentTimeMillis()))
        }
        saveList(current)
    }

    fun deleteProject(projectId: String) {
        val current = getAllProjects().filter { it.id != projectId }
        saveList(current)
    }

    private fun saveList(list: List<StudioProject>) {
        val jsonArray = JSONArray()
        for (p in list) {
            val obj = JSONObject().apply {
                put("id", p.id)
                put("name", p.name)
                put("videoUriString", p.videoUriString)
                put("videoFileName", p.videoFileName)
                put("videoDurationMs", p.videoDurationMs)
                put("subtitleFileName", p.subtitleFileName)
                put("subtitleFormat", p.subtitleFormat.extension)
                put("cueCount", p.cueCount)
                put("lastModifiedMs", p.lastModifiedMs)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString("projects_list", jsonArray.toString()).apply()
    }
}
