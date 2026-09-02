package com.universal.calulator

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class HistoryItem(
    val id: Long = UUID.randomUUID().mostSignificantBits,
    val expression: String,
    val result: String,
    val tag: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

object CalculatorHistoryManager {
    private const val PREF_NAME = "calc_history_pref"
    private const val KEY_HISTORY = "history_list"
    const val MAX_HISTORY_LIMIT = 500

    fun saveHistory(context: Context, list: List<HistoryItem>) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val jsonArray = JSONArray()

        // Automatically preserve only the latest 500 entries (oldest dropped)
        for (item in list.take(MAX_HISTORY_LIMIT)) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("expression", item.expression)
                put("result", item.result)
                put("tag", item.tag)
                put("timestamp", item.timestamp)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_HISTORY, jsonArray.toString()).apply()
    }

    fun loadHistory(context: Context): List<HistoryItem> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        val list = mutableListOf<HistoryItem>()
        return try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length().coerceAtMost(MAX_HISTORY_LIMIT)) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    HistoryItem(
                        id = obj.optLong("id", UUID.randomUUID().mostSignificantBits),
                        expression = obj.optString("expression", ""),
                        result = obj.optString("result", ""),
                        tag = obj.optString("tag", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Smart addition: Avoids consecutive duplicate entries and caps at 500
     */
    fun addRecordWithDeduplication(
        context: Context,
        currentList: MutableList<HistoryItem>,
        expression: String,
        result: String
    ) {
        val cleanExpr = expression.trim()
        val cleanRes = result.trim()
        if (cleanExpr.isEmpty() || cleanRes.isEmpty()) return

        val firstItem = currentList.firstOrNull()
        val isDuplicate = firstItem != null &&
                firstItem.expression.trim() == cleanExpr &&
                firstItem.result.trim() == cleanRes

        if (!isDuplicate) {
            val newItem = HistoryItem(
                expression = cleanExpr,
                result = cleanRes
            )
            currentList.add(0, newItem)

            // Trim list if it exceeds 500 items in memory
            if (currentList.size > MAX_HISTORY_LIMIT) {
                currentList.removeAt(currentList.lastIndex)
            }

            saveHistory(context, currentList)
        }
    }

    fun clearHistory(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_HISTORY).apply()
    }
}