package com.universal.calulator

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class HabitStatus {
    DONE,
    HALF_DONE,
    MISSED,
    NO_DATA,
    PENDING
}

data class HabitRecord(
    val id: String,
    val dateStr: String, // "yyyy-MM-dd"
    val status: HabitStatus,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

object HabitTrackerManager {
    private const val PREF_NAME = "toolator_habit_prefs"
    private const val KEY_HABITS = "saved_habit_records"
    private const val KEY_INSTALL_DATE = "habit_install_date"
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun getInstallDate(context: Context): LocalDate {
        val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val saved = sp.getString(KEY_INSTALL_DATE, null)
        return if (saved != null) {
            try { LocalDate.parse(saved, formatter) } catch (e: Exception) { LocalDate.now() }
        } else {
            val today = LocalDate.now()
            sp.edit().putString(KEY_INSTALL_DATE, today.format(formatter)).apply()
            today
        }
    }

    fun loadRecords(context: Context): Map<String, HabitRecord> {
        val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val jsonStr = sp.getString(KEY_HABITS, null) ?: return emptyMap()
        val map = mutableMapOf<String, HabitRecord>()

        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val statusStr = obj.optString("status", HabitStatus.PENDING.name)
                val status = try { HabitStatus.valueOf(statusStr) } catch (e: Exception) { HabitStatus.PENDING }

                val record = HabitRecord(
                    id = obj.optString("id", UUIDGenerator.get()),
                    dateStr = obj.getString("dateStr"),
                    status = status,
                    note = obj.optString("note", ""),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                    updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                )
                map[record.dateStr] = record
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return map
    }

    fun saveRecord(context: Context, record: HabitRecord) {
        val currentMap = loadRecords(context).toMutableMap()
        currentMap[record.dateStr] = record
        saveAll(context, currentMap.values.toList())
    }

    private fun saveAll(context: Context, list: List<HabitRecord>) {
        val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val jsonArray = JSONArray()
        for (item in list) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("dateStr", item.dateStr)
                put("status", item.status.name)
                put("note", item.note)
                put("createdAt", item.createdAt)
                put("updatedAt", item.updatedAt)
            }
            jsonArray.put(obj)
        }
        sp.edit().putString(KEY_HABITS, jsonArray.toString()).apply()
    }
}

object UUIDGenerator {
    fun get(): String = java.util.UUID.randomUUID().toString()
}