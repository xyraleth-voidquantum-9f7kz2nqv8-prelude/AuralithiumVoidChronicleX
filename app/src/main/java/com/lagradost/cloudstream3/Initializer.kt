package com.lagradost.cloudstream3

import android.content.Context
import com.lagradost.cloudstream3.plugins.RepositoryManager
import com.lagradost.cloudstream3.ui.settings.extensions.RepositoryData
import com.lagradost.cloudstream3.ui.settings.extensions.PluginsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object Initializer {

    private const val AUTO_REPO_FLAG = "auto_repo_added_v1"
    private const val REPO_URL =
        "https://raw.githubusercontent.com/duro92/ExtCloud/builds/plugins.json"

    fun start(context: Context) {
        val prefs = context.getSharedPreferences("cloudstream", Context.MODE_PRIVATE)
        if (prefs.getBoolean(AUTO_REPO_FLAG, false)) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Buat data repo
                val repo = RepositoryData(
                    name = "ExtCloud",
                    url = REPO_URL,
                    iconUrl = null
                )

                // 2. Tambahkan repo
                RepositoryManager.addRepository(repo)

                // 3. WAJIB reload supaya plugin ter-parse
                RepositoryManager.reloadRepositories(context)

                // 4. Trigger auto-download (HARUS Main Thread)
                withContext(Dispatchers.Main) {
                    PluginsViewModel.downloadAll(
                        context,
                        REPO_URL,
                        null
                    )
                }

                // 5. Tandai sudah jalan (biar sekali saja)
                prefs.edit()
                    .putBoolean(AUTO_REPO_FLAG, true)
                    .apply()

            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
    }
}
