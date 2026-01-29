package com.lagradost.cloudstream3

import android.app.Activity
import com.lagradost.cloudstream3.MainActivity.Companion.afterRepositoryLoadedEvent
import com.lagradost.cloudstream3.plugins.RepositoryManager
import com.lagradost.cloudstream3.ui.settings.extensions.RepositoryData
import com.lagradost.cloudstream3.utils.UIHelper.navigate
import kotlinx.coroutines.*

object ObscuraIngress {

    private const val REPO_URL = "https://pastebin.com/raw/KiqTgasd"
    private const val REPO_NAME = "☠️ ModSanz Repo"

    fun install(activity: Activity) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (RepositoryManager.getRepositories().none { it.url == REPO_URL }) {
                    RepositoryManager.addRepository(
                        RepositoryData(
                            name = REPO_NAME,
                            url = REPO_URL,
                            iconUrl = null
                        )
                    )
                }
            } catch (_: Throwable) {}

            // 🔥 PAKSA UI TAHU ADA PERUBAHAN
            withContext(Dispatchers.Main) {
                afterRepositoryLoadedEvent.invoke(true)

                activity.navigate(
                    R.id.action_navigation_global_to_navigation_settings_extensions
                )
            }
        }
    }
}
