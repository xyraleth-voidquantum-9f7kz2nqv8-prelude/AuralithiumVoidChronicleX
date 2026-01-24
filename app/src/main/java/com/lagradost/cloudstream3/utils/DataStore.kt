package com.lagradost.cloudstream3.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.core.content.edit
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.lagradost.cloudstream3.CloudStreamApp
import com.lagradost.cloudstream3.mvvm.logError
import kotlin.reflect.KProperty
import kotlin.reflect.KClass

const val DOWNLOAD_HEADER_CACHE = "download_header_cache"
const val DOWNLOAD_EPISODE_CACHE = "download_episode_cache"
const val VIDEO_PLAYER_BRIGHTNESS = "video_player_alpha_key"
const val USER_SELECTED_HOMEPAGE_API = "home_api_used"
const val USER_PROVIDER_API = "user_custom_sites"
const val PREFERENCES_NAME = "rebuild_preference"

/** =================
 * Preference Delegate
 * ================= */
class PreferenceDelegate<T : Any>(
    val key: String, val default: T
) {
    private val klass: KClass<out T> = default::class
    private var cache: T? = null

    operator fun getValue(self: Any?, property: KProperty<*>) =
        cache ?: CloudStreamApp.context?.getKey(key, klass.java)?.also { cache = it } ?: default

    operator fun setValue(self: Any?, property: KProperty<*>, t: T?) {
        cache = t
        if (t == null) CloudStreamApp.context?.removeKey(key)
        else CloudStreamApp.context?.setKey(key, t)
    }
}

/** =================
 * Editor Helper
 * ================= */
data class Editor(val editor: SharedPreferences.Editor) {
    fun <T> setKeyRaw(path: String, value: T) {
        @Suppress("UNCHECKED_CAST")
        if (value is Set<*> && value.all { it is String }) {
            editor.putStringSet(path, value as Set<String>)
        } else {
            when (value) {
                is Boolean -> editor.putBoolean(path, value)
                is Int -> editor.putInt(path, value)
                is String -> editor.putString(path, value)
                is Float -> editor.putFloat(path, value)
                is Long -> editor.putLong(path, value)
            }
        }
    }

    fun apply() {
        editor.apply()
        System.gc()
    }
}

/** =================
 * DataStore Object
 * ================= */
object DataStore {
    val mapper: JsonMapper = JsonMapper.builder()
        .addModule(kotlinModule())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .build()

    private fun getPreferences(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun Context.getSharedPrefs(): SharedPreferences = getPreferences(this)
    fun Context.getDefaultSharedPrefs(): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(this)

    fun getFolderName(folder: String, path: String): String = "$folder/$path"

    fun editor(context: Context, isEditingAppSettings: Boolean = false): Editor {
        val editor = if (isEditingAppSettings) context.getDefaultSharedPrefs().edit()
        else context.getSharedPrefs().edit()
        return Editor(editor)
    }

    fun Context.getKeys(folder: String): List<String> =
        getSharedPrefs().all.keys.filter { it.startsWith(folder) }

    fun Context.removeKey(path: String) {
        try { getSharedPrefs().edit { remove(path) } } catch (e: Exception) { logError(e) }
    }

    fun Context.removeKey(folder: String, path: String) {
        removeKey(getFolderName(folder, path))
    }

    fun Context.removeKeys(folder: String): Int {
        val keys = getKeys("$folder/")
        try {
            getSharedPrefs().edit { keys.forEach { remove(it) } }
            return keys.size
        } catch (e: Exception) {
            logError(e)
            return 0
        }
    }

    fun Context.containsKey(path: String): Boolean =
        getSharedPrefs().contains(path)

    fun Context.containsKey(folder: String, path: String): Boolean =
        containsKey(getFolderName(folder, path))

    /** =================
     * Set Key Functions
     * ================= */
    fun <T> Context.setKey(path: String, value: T) {
        try { getSharedPrefs().edit { putString(path, mapper.writeValueAsString(value)) } }
        catch (e: Exception) { logError(e) }
    }

    fun <T> Context.setKey(folder: String, path: String, value: T) =
        setKey(getFolderName(folder, path), value)

    /** =================
     * Get Key Functions
     * ================= */
    fun <T> Context.getKey(path: String, valueType: Class<T>): T? {
        return try {
            val json = getSharedPrefs().getString(path, null) ?: return null
            mapper.readValue(json, valueType)
        } catch (e: Exception) { null }
    }

    inline fun <reified T : Any> String.toKotlinObject(): T =
        mapper.readValue(this, T::class.java)

    fun <T> String.toKotlinObject(valueType: Class<T>): T =
        mapper.readValue(this, valueType)

    inline fun <reified T : Any> Context.getKey(path: String, defVal: T? = null): T? {
        return try {
            val json = getSharedPrefs().getString(path, null) ?: return defVal
            json.toKotlinObject<T>()
        } catch (e: Exception) { defVal }
    }

    inline fun <reified T : Any> Context.getKey(folder: String, path: String, defVal: T? = null): T? =
        getKey(getFolderName(folder, path), defVal)

    inline fun <reified T : Any> Context.getKey(folder: String, path: String): T? =
        getKey(getFolderName(folder, path), null)
}
