package com.lagradost.cloudstream3

import android.content.Context
import com.lagradost.cloudstream3.plugins.RepositoryManager
import com.lagradost.cloudstream3.ui.settings.extensions.RepositoryData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Initializer
 * - Jalan 1x saat app pertama kali start
 * - Tambah repo default
 * - Set flag untuk auto download plugin
 *
 * DIPANGGIL DARI: CloudStreamApp (Application)
 * JANGAN dipanggil dari Activity
 */
object Initializer {

    // Sinyal ke FirstInstallManager
    const val NEED_AUTO_DOWNLOAD = "need_auto_download_v1"

    // Flag internal agar tidak double init
    private const val AUTO_REPO_FLAG = "auto_repo_added_v1"

    fun start(context: Context) {
        val appContext = context.applicationContext
        val prefs = appContext.getSharedPreferences(
            "cloudstream",
            Context.MODE_PRIVATE
        )

        // Sudah pernah jalan → STOP
        if (prefs.getBoolean(AUTO_REPO_FLAG, false)) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Tambah repository default
                RepositoryManager.addRepository(
                    RepositoryData(
                        name = "ExtCloud",
                        url = "https://raw.githubusercontent.com/duro92/ExtCloud/builds/plugins.json",
                        iconUrl = null
                    )
                )

                // Simpan flag
                prefs.edit()
                    .putBoolean(AUTO_REPO_FLAG, true)
                    .putBoolean(NEED_AUTO_DOWNLOAD, true)
                    .apply()

            } catch (_: Throwable) {
                // sengaja di-silent
                // supaya app tidak crash saat first install
            }
        }
    }
}
