package com.mitashi.ht4440bt.remote.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Local persistent backend for storing the user's active Mitashi HT4440BT preset,
 * custom calibrated IR codes, haptic preference, and theme preference.
 */
class RemoteProfileRepository(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(
        "mitashi_ht4440bt_prefs",
        Context.MODE_PRIVATE
    )

    fun loadActivePresetId(): String {
        return prefs.getString(KEY_ACTIVE_PRESET, PresetProfiles.allPresets.first().id)
            ?: PresetProfiles.allPresets.first().id
    }

    fun saveActivePresetId(presetId: String) {
        prefs.edit().putString(KEY_ACTIVE_PRESET, presetId).apply()
    }

    fun isDarkTheme(): Boolean = prefs.getBoolean(KEY_DARK_THEME, true)

    fun saveDarkTheme(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_THEME, enabled).apply()
    }

    fun isHapticEnabled(): Boolean = prefs.getBoolean(KEY_HAPTIC, true)

    fun saveHapticEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTIC, enabled).apply()
    }

    fun loadCustomOverrides(): Map<RemoteButton, IrButtonCode> {
        val raw = prefs.getString(KEY_CUSTOM_CODES, null) ?: return emptyMap()
        return deserializeCodeMap(raw)
    }

    fun saveCustomOverrides(overrides: Map<RemoteButton, IrButtonCode>) {
        prefs.edit().putString(KEY_CUSTOM_CODES, serializeCodeMap(overrides)).apply()
    }

    fun clearCustomOverrides() {
        prefs.edit().remove(KEY_CUSTOM_CODES).apply()
    }

    fun exportProfileJson(codes: Map<RemoteButton, IrButtonCode>): String {
        val root = JSONObject()
        root.put("device", "Mitashi HT4440BT")
        root.put("version", 1)
        root.put("buttons", JSONObject(serializeCodeMap(codes)))
        return root.toString(2)
    }

    fun importProfileJson(jsonText: String): Map<RemoteButton, IrButtonCode> {
        val root = JSONObject(jsonText)
        val buttonsObj = if (root.has("buttons")) root.getJSONObject("buttons") else root
        return deserializeCodeMap(buttonsObj.toString())
    }

    private fun serializeCodeMap(map: Map<RemoteButton, IrButtonCode>): String {
        val obj = JSONObject()
        for ((button, code) in map) {
            val item = JSONObject()
            item.put("protocol", code.protocol.name)
            item.put("frequencyHz", code.frequencyHz)
            item.put("address", code.address)
            item.put("command", code.command)
            item.put("prontoHex", code.prontoHex)
            val arr = JSONArray()
            code.rawPatternUs.forEach { arr.put(it) }
            item.put("rawPatternUs", arr)
            obj.put(button.id, item)
        }
        return obj.toString()
    }

    private fun deserializeCodeMap(json: String): Map<RemoteButton, IrButtonCode> {
        val result = linkedMapOf<RemoteButton, IrButtonCode>()
        return try {
            val obj = JSONObject(json)
            for (button in RemoteButton.entries) {
                if (!obj.has(button.id)) continue
                val item = obj.getJSONObject(button.id)
                val proto = runCatching {
                    IrProtocolType.valueOf(item.optString("protocol", IrProtocolType.NEC.name))
                }.getOrDefault(IrProtocolType.NEC)
                val rawArr = item.optJSONArray("rawPatternUs")
                val rawUs = if (rawArr != null) {
                    IntArray(rawArr.length()) { i -> rawArr.optInt(i, 562) }
                } else {
                    intArrayOf()
                }
                result[button] = IrButtonCode(
                    protocol = proto,
                    frequencyHz = item.optInt("frequencyHz", 38_000),
                    address = item.optInt("address", 0),
                    command = item.optInt("command", 0),
                    prontoHex = item.optString("prontoHex", ""),
                    rawPatternUs = rawUs
                )
            }
            result
        } catch (_: Throwable) {
            emptyMap()
        }
    }

    companion object {
        private const val KEY_ACTIVE_PRESET = "active_preset_id"
        private const val KEY_CUSTOM_CODES = "custom_overrides_json"
        private const val KEY_DARK_THEME = "dark_theme"
        private const val KEY_HAPTIC = "haptic_enabled"
    }
}
