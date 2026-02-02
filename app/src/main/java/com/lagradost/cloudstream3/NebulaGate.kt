package com.lagradost.cloudstream3

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Base64
import org.json.JSONObject
import java.net.URL
import kotlin.concurrent.thread

object NebulaGate {

    private const val p1 = "aHR0cHM6Ly9yYXcuZ2l0aHVidXNlcmNv"
    private const val p2 = "bnRlbnQuY29tL0NvZGVTYW56ei9SZUNs"
    private const val p3 = "b3VkUGxheS9idWlsZHMvdXBkYXRlL"
    private const val p4 = "mpzb24="

    private val UPDATE_URL: String
        get() = String(Base64.decode(p1 + p2 + p3 + p4, Base64.DEFAULT))

    private val KEY_VERSIONCODE = intArrayOf(118,101,114,115,105,111,110,67,111,100,101)
    private val KEY_CHANGELOG   = intArrayOf(99,104,97,110,103,101,108,111,103)
    private val KEY_APK         = intArrayOf(97,112,107)

    private fun decodeKey(arr: IntArray): String {
        return arr.map { it.toChar() }.joinToString("")
    }

    fun probe(activity: Activity) {
        thread {
            try {
                val json = URL(UPDATE_URL).readText()
                val data = JSONObject(json)

                val serverCode = data.getInt(decodeKey(KEY_VERSIONCODE))
                val changelog = data.getString(decodeKey(KEY_CHANGELOG))
                val apkUrl = data.getString(decodeKey(KEY_APK))

                if (serverCode > BuildConfig.VERSION_CODE) {
                    Handler(Looper.getMainLooper()).post {
                        AstraFlag.dispatch(activity, changelog, apkUrl)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
