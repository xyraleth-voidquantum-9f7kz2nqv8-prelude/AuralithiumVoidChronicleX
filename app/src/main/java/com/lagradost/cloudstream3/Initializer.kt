package com.lagradost.cloudstream3

import android.content.Context
import com.lagradost.cloudstream3.plugins.RepositoryManager
import com.lagradost.cloudstream3.ui.settings.extensions.RepositoryData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object Initializer {

    private const val AUTO_REPO_FLAG = "auto_repo_added_v1"

    /**
     * Tambahkan repository otomatis **sekali saja** saat pertama kali install
     * @param context Context untuk SharedPreferences
     * @param onComplete Callback dipanggil setelah repo selesai ditambahkan (atau sudah pernah ditambahkan)
     */
    fun runAutoDownloadIfNeeded(context: Context, onComplete: () -> Unit) {
        val prefs = context.getSharedPreferences("cloudstream", Context.MODE_PRIVATE)
        
        if (prefs.getBoolean(AUTO_REPO_FLAG, false)) {
            onComplete() // langsung panggil callback kalau sudah pernah dijalankan
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
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

                onComplete() // callback setelah selesai menambahkan repo
            } catch (e: Throwable) {
                e.printStackTrace()
                onComplete() // tetap panggil callback walau error
            }
        }
    }
}