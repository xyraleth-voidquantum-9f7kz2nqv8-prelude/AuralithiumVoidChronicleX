package com.lagradost.cloudstream3.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.preference.PreferenceManager
import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.CommonActivity.showToast
import com.lagradost.cloudstream3.mvvm.logError
import com.lagradost.cloudstream3.utils.AppUtils.parseJson
import com.lagradost.cloudstream3.utils.Coroutines.ioSafe
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okio.BufferedSink
import okio.buffer
import okio.sink
import java.io.File
import android.text.TextUtils
import com.lagradost.cloudstream3.MainActivity.Companion.deleteFileOnExit
import com.lagradost.cloudstream3.services.PackageInstallerService
import com.lagradost.cloudstream3.utils.AppContextUtils.setDefaultFocus
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class InAppUpdater {
    companion object {

        // Repo tetap, tapi TIDAK DIPAKAI karena update dimatikan
        private const val GITHUB_USER_NAME = "CodeSanzz"
        private const val GITHUB_REPO = "CloudPlay"

        private const val LOG_TAG = "InAppUpdater"

        // ===== DATA CLASS (dibiarkan agar tidak error referensi) =====
        data class GithubAsset(
            @JsonProperty("name") val name: String,
            @JsonProperty("size") val size: Int,
            @JsonProperty("browser_download_url") val browserDownloadUrl: String,
            @JsonProperty("content_type") val contentType: String,
        )

        data class GithubRelease(
            @JsonProperty("tag_name") val tagName: String,
            @JsonProperty("body") val body: String,
            @JsonProperty("assets") val assets: List<GithubAsset>,
            @JsonProperty("target_commitish") val targetCommitish: String,
            @JsonProperty("prerelease") val prerelease: Boolean,
            @JsonProperty("node_id") val nodeId: String
        )

        data class GithubObject(
            @JsonProperty("sha") val sha: String,
            @JsonProperty("type") val type: String,
            @JsonProperty("url") val url: String,
        )

        data class GithubTag(
            @JsonProperty("object") val githubObject: GithubObject,
        )

        data class Update(
            @JsonProperty("shouldUpdate") val shouldUpdate: Boolean,
            @JsonProperty("updateURL") val updateURL: String?,
            @JsonProperty("updateVersion") val updateVersion: String?,
            @JsonProperty("changelog") val changelog: String?,
            @JsonProperty("updateNodeId") val updateNodeId: String?
        )

        // ===== UPDATE DIMATIKAN TOTAL =====

        /**
         * AUTO + MANUAL + STARTUP UPDATE DIMATIKAN
         * Popup "Pembaruan ditemukan" TIDAK AKAN PERNAH MUNCUL
         */
        suspend fun Activity.runAutoUpdate(checkAutoUpdate: Boolean = true): Boolean {
            return false
        }

        // Optional safety: kalau ada yang manggil langsung
        private suspend fun Activity.getAppUpdate(): Update {
            return Update(false, null, null, null, null)
        }

        // ===== SISA FUNGSI DIBIARKAN (TIDAK AKAN TERPAKAI) =====

        private val updateLock = Mutex()

        private suspend fun Activity.downloadUpdate(url: String): Boolean {
            return false
        }

        private fun openApk(context: Context, uri: Uri) {}

        private fun isMiUi(): Boolean {
            return false
        }

        private fun getSystemProperty(propName: String): String? {
            return null
        }
    }
}
