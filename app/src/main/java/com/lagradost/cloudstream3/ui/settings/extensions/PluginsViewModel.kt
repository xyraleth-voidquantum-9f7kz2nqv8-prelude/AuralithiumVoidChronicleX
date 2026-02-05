package com.lagradost.cloudstream3.ui.settings.extensions

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import com.lagradost.cloudstream3.CommonActivity.showToast
import com.lagradost.cloudstream3.PROVIDER_STATUS_DOWN
import com.lagradost.cloudstream3.R
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.amap
import com.lagradost.cloudstream3.mvvm.launchSafe
import com.lagradost.cloudstream3.plugins.PluginManager
import com.lagradost.cloudstream3.plugins.PluginManager.getPluginPath
import com.lagradost.cloudstream3.plugins.RepositoryManager
import com.lagradost.cloudstream3.plugins.SitePlugin
import com.lagradost.cloudstream3.utils.Coroutines.ioSafe
import com.lagradost.cloudstream3.utils.Coroutines.main
import com.lagradost.cloudstream3.utils.Coroutines.runOnMainThread
import com.lagradost.cloudstream3.utils.txt
import me.xdrop.fuzzywuzzy.FuzzySearch
import java.io.File

typealias Plugin = Pair<String, SitePlugin>
typealias PluginViewDataUpdate = Pair<Boolean, List<PluginViewData>>

class PluginsViewModel : ViewModel() {

    private var plugins: List<PluginViewData> = emptyList()
        set(value) {
            pluginLanguages.clear()
            value.forEach {
                pluginLanguages.add(
                    it.plugin.second.language?.lowercase() ?: "none"
                )
            }
            field = value
        }

    val pluginLanguages: MutableSet<String> = mutableSetOf()

    private val _filteredPlugins = MutableLiveData<PluginViewDataUpdate>()
    val filteredPlugins: LiveData<PluginViewDataUpdate> = _filteredPlugins

    val tvTypes = mutableListOf<String>()
    var selectedLanguages = listOf<String>()
    private var currentQuery: String? = null

    companion object {

        private fun isDownloaded(
            context: Context,
            pluginName: String,
            repositoryUrl: String
        ): Boolean {
            return getPluginPath(context, pluginName, repositoryUrl).exists()
        }

        private suspend fun getPlugins(repositoryUrl: String): List<Plugin> {
            return RepositoryManager.getRepoPlugins(repositoryUrl) ?: emptyList()
        }

        fun downloadAll(
            activity: Activity?,
            repositoryUrl: String,
            viewModel: PluginsViewModel?
        ) = ioSafe {
            if (activity == null) return@ioSafe

            val plugins = getPlugins(repositoryUrl)
            val needDownload = plugins.filter {
                !isDownloaded(activity, it.second.internalName, repositoryUrl)
            }

            main {
                when {
                    plugins.isEmpty() -> showToast(
                        txt(R.string.no_plugins_found_error),
                        Toast.LENGTH_SHORT
                    )

                    needDownload.isEmpty() -> showToast(
                        txt(
                            R.string.batch_download_nothing_to_download_format,
                            txt(R.string.plugin)
                        ),
                        Toast.LENGTH_SHORT
                    )

                    else -> showToast(
                        txt(
                            R.string.batch_download_start_format,
                            needDownload.size,
                            txt(
                                if (needDownload.size == 1)
                                    R.string.plugin_singular
                                else
                                    R.string.plugin
                            )
                        ),
                        Toast.LENGTH_SHORT
                    )
                }
            }

            needDownload.amap { (repo, metadata) ->
                PluginManager.downloadPlugin(
                    activity,
                    metadata.url,
                    metadata.internalName,
                    repo,
                    metadata.status != PROVIDER_STATUS_DOWN
                )
            }.main { result ->
                if (result.any { it }) {
                    showToast(
                        txt(
                            R.string.batch_download_finish_format,
                            result.count { it },
                            txt(
                                if (result.size == 1)
                                    R.string.plugin_singular
                                else
                                    R.string.plugin
                            )
                        ),
                        Toast.LENGTH_SHORT
                    )
                    viewModel?.forceReload(activity, repositoryUrl)
                } else if (result.isNotEmpty()) {
                    showToast(R.string.download_failed, Toast.LENGTH_SHORT)
                }
            }
        }
    }

    fun handlePluginAction(
        activity: Activity?,
        repositoryUrl: String,
        plugin: Plugin,
        isLocal: Boolean
    ) = ioSafe {
        if (activity == null) return@ioSafe

        val file = if (isLocal) {
            File(plugin.second.url)
        } else {
            getPluginPath(activity, plugin.second.internalName, plugin.first)
        }

        val (success, message) =
            if (file.exists()) {
                PluginManager.deletePlugin(file) to R.string.plugin_deleted
            } else {
                PluginManager.downloadPlugin(
                    activity,
                    plugin.second.url,
                    plugin.second.internalName,
                    plugin.first,
                    plugin.second.status != PROVIDER_STATUS_DOWN
                ) to R.string.plugin_loaded
            }

        runOnMainThread {
            if (success) showToast(message, Toast.LENGTH_SHORT)
            else showToast(R.string.error, Toast.LENGTH_SHORT)
        }

        if (success) {
            if (isLocal) updatePluginListLocal()
            else forceReload(activity, repositoryUrl)
        }
    }

    private suspend fun forceReload(context: Context, repositoryUrl: String) {
        RepositoryManager.getRepoPlugins(repositoryUrl)
        updatePluginListPrivate(context, repositoryUrl)
    }

    private suspend fun updatePluginListPrivate(
        context: Context,
        repositoryUrl: String
    ) {
        val isAdult =
            PreferenceManager.getDefaultSharedPreferences(context)
                .getStringSet(
                    context.getString(R.string.prefer_media_type_key),
                    emptySet()
                )
                ?.contains(TvType.NSFW.ordinal.toString()) == true

        val list =
            RepositoryManager.getRepoPlugins(repositoryUrl)
                ?.filter {
                    it.second.tvTypes?.contains(TvType.NSFW.name) != true || isAdult
                }
                ?.map {
                    PluginViewData(
                        it,
                        isDownloaded(context, it.second.internalName, it.first)
                    )
                } ?: emptyList()

        plugins = list
        _filteredPlugins.postValue(
            false to list.filterTvTypes().filterLang().sortByQuery(currentQuery)
        )
    }

    private fun List<PluginViewData>.filterTvTypes(): List<PluginViewData> {
        if (tvTypes.isEmpty()) return this
        return filter {
            it.plugin.second.tvTypes?.any { t -> tvTypes.contains(t) } == true ||
                    (tvTypes.contains(TvType.Others.name) &&
                            it.plugin.second.tvTypes.isNullOrEmpty())
        }
    }

    private fun List<PluginViewData>.filterLang(): List<PluginViewData> {
        if (selectedLanguages.isEmpty()) return this
        return filter {
            val lang = it.plugin.second.language?.lowercase() ?: "none"
            selectedLanguages.contains(lang)
        }
    }

    private fun List<PluginViewData>.sortByQuery(query: String?): List<PluginViewData> {
        return if (query == null) {
            sortedBy { it.plugin.second.name }
        } else {
            sortedBy {
                -FuzzySearch.partialRatio(
                    it.plugin.second.name.lowercase(),
                    query.lowercase()
                )
            }
        }
    }

    fun updateFilteredPlugins() {
        _filteredPlugins.postValue(
            false to plugins.filterTvTypes().filterLang().sortByQuery(currentQuery)
        )
    }

    fun clear() {
        currentQuery = null
        _filteredPlugins.postValue(false to emptyList())
    }

    fun updatePluginList(context: Context?, repositoryUrl: String) =
        viewModelScope.launchSafe {
            if (context == null) return@launchSafe
            updatePluginListPrivate(context, repositoryUrl)
        }

    fun search(query: String?) {
        currentQuery = query
        _filteredPlugins.postValue(
            true to plugins.sortByQuery(query)
        )
    }

    fun updatePluginListLocal() = viewModelScope.launchSafe {
        val downloaded =
            (PluginManager.getPluginsOnline() + PluginManager.getPluginsLocal())
                .distinctBy { it.filePath }
                .map {
                    PluginViewData("" to it.toSitePlugin(), true)
                }

        plugins = downloaded
        _filteredPlugins.postValue(
            false to downloaded.filterTvTypes().filterLang().sortByQuery(currentQuery)
        )
    }
}
