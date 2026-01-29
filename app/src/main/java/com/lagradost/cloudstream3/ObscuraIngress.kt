package com.lagradost.cloudstream3

import android.app.Activity
import android.os.Handler
import android.os.Looper
import com.lagradost.cloudstream3.plugins.RepositoryManager
import com.lagradost.cloudstream3.ui.settings.extensions.ExtensionsFragment
import com.lagradost.cloudstream3.ui.settings.extensions.RepositoryData
import com.lagradost.cloudstream3.utils.UIHelper.navigate
import kotlinx.coroutines.*

object ObscuraIngress {

    private const val REPO_URL = "https://pastebin.com/raw/KiqTgasd"
    private const val REPO_NAME = "☠️ ModSanz Repo"

    fun install(activity: Activity) {
        CoroutineScope(Dispatchers.IO).launch {

            val repo = RepositoryData(
                name = REPO_NAME,
                url = REPO_URL,
                iconUrl = null
            )

            if (RepositoryManager.getRepositories().none { it.url == REPO_URL }) {
                RepositoryManager.addRepository(repo)
            }

            withContext(Dispatchers.Main) {
                activity.navigate(
                    R.id.action_navigation_global_to_navigation_settings_extensions
                )

                // ⏱️ tunggu fragment ready
                Handler(Looper.getMainLooper()).postDelayed({
                    ExtensionsFragment.selectRepository(repo)
                }, 350)
            }
        }
    }
}
