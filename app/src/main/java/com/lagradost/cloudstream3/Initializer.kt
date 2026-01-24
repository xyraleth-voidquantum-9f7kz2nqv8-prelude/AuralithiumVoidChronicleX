package com.lagradost.cloudstream3

import android.content.Context
import com.lagradost.cloudstream3.plugins.RepositoryManager
import com.lagradost.cloudstream3.ui.settings.extensions.RepositoryData

object Initializer {

    private const val AUTO_REPO_FLAG = "auto_repo_added_v1"

    fun start(context: Context) {
        val prefs = context.getSharedPreferences("cloudstream", Context.MODE_PRIVATE)
        if (prefs.getBoolean(AUTO_REPO_FLAG, false)) return

        ioSafe {
            try {
                RepositoryManager.addRepository(
                    RepositoryData(
                        name = "ExtCloud",
                        url = "https://raw.githubusercontent.com/duro92/ExtCloud/main/repo.json",
                        iconUrl = null
                    )
                )

                prefs.edit()
                    .putBoolean(AUTO_REPO_FLAG, true)
                    .apply()

            } catch (e: Throwable) {
                logError(e)
            }
        }
    }
}
