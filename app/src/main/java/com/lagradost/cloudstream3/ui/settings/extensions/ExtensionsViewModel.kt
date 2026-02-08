package com.lagradost.cloudstream3.ui.settings.extensions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.CloudStreamApp.Companion.getKey
import com.lagradost.cloudstream3.R
import com.lagradost.cloudstream3.amap
import com.lagradost.cloudstream3.mvvm.debugAssert
import com.lagradost.cloudstream3.plugins.PluginManager
import com.lagradost.cloudstream3.plugins.PluginManager.getPluginsOnline
import com.lagradost.cloudstream3.plugins.RepositoryManager
import com.lagradost.cloudstream3.plugins.RepositoryManager.PREBUILT_REPOSITORIES
import com.lagradost.cloudstream3.utils.UiText
import com.lagradost.cloudstream3.utils.txt
import com.lagradost.cloudstream3.utils.Coroutines.ioSafe

data class RepositoryData(
    @JsonProperty("iconUrl") val iconUrl: String?,
    @JsonProperty("name") val name: String,
    @JsonProperty("url") val url: String
) {
    constructor(name: String, url: String) : this(null, name, url)
}

const val REPOSITORIES_KEY = "REPOSITORIES_KEY"

class ExtensionsViewModel : ViewModel() {

    data class PluginStats(
        val total: Int,
        val downloaded: Int,
        val disabled: Int,
        val notDownloaded: Int,
        val downloadedText: UiText,
        val disabledText: UiText,
        val notDownloadedText: UiText,
    )

    private val _repositories = MutableLiveData<Array<RepositoryData>>()
    val repositories: LiveData<Array<RepositoryData>> = _repositories

    private val _pluginStats = MutableLiveData<PluginStats?>(null)
    val pluginStats: LiveData<PluginStats?> = _pluginStats

    /**
     * ============================
     * ✅ FINAL & STABLE LOGIC
     * ============================
     * - null = data belum siap / offline
     * - tidak memalsukan angka 0
     * - aman untuk repo kosong
     */
    fun loadStats() = ioSafe {

        val repos =
            (getKey<Array<RepositoryData>>(REPOSITORIES_KEY) ?: emptyArray()) +
                    PREBUILT_REPOSITORIES

        if (repos.isEmpty()) {
            _pluginStats.postValue(null)
            return@ioSafe
        }

        // Ambil semua plugin online dari repo
        val onlinePlugins = repos.toList().amap { repo ->
            RepositoryManager.getRepoPlugins(repo.url)?.toList() ?: emptyList()
        }.flatten()
            .distinctBy { it.second.url }

        if (onlinePlugins.isEmpty()) {
            _pluginStats.postValue(null)
            return@ioSafe
        }

        // Plugin lokal (terpasang)
        val installedPlugins = getPluginsOnline()

        // Cocokkan lokal vs online
        val matchedPlugins = installedPlugins.mapNotNull { local ->
            onlinePlugins.firstOrNull {
                it.second.internalName == local.internalName
            }?.let { online ->
                PluginManager.OnlinePluginData(local, online)
            }
        }

        val total = onlinePlugins.size
        val disabled = matchedPlugins.count { it.isDisabled }
        val downloaded = matchedPlugins.count { !it.isDisabled }
        val notDownloaded = total - matchedPlugins.size

        val stats = PluginStats(
            total = total,
            downloaded = downloaded,
            disabled = disabled,
            notDownloaded = notDownloaded,
            downloadedText = txt(R.string.plugins_downloaded, downloaded),
            disabledText = txt(R.string.plugins_disabled, disabled),
            notDownloadedText = txt(R.string.plugins_not_downloaded, notDownloaded)
        )

        // ✅ ASSERT BENAR (bukan kebalik)
        debugAssert({
            stats.downloaded + stats.disabled + stats.notDownloaded == stats.total
        }) {
            "Invalid plugin stats: " +
                    "${stats.downloaded}+${stats.disabled}+${stats.notDownloaded} != ${stats.total}"
        }

        _pluginStats.postValue(stats)
    }

    fun loadRepositories() {
        val repos =
            (getKey<Array<RepositoryData>>(REPOSITORIES_KEY) ?: emptyArray()) +
                    PREBUILT_REPOSITORIES
        _repositories.postValue(repos)
    }
}