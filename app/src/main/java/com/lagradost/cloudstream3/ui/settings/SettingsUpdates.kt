package com.lagradost.cloudstream3.ui.settings

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.lagradost.cloudstream3.AutoDownloadMode
import com.lagradost.cloudstream3.BuildConfig
import com.lagradost.cloudstream3.CloudStreamApp
import com.lagradost.cloudstream3.CommonActivity.showToast
import com.lagradost.cloudstream3.R
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.databinding.LogcatBinding
import com.lagradost.cloudstream3.databinding.SettingsUpdatesBinding
import com.lagradost.cloudstream3.mvvm.logError
import com.lagradost.cloudstream3.mvvm.safe
import com.lagradost.cloudstream3.network.initClient
import com.lagradost.cloudstream3.plugins.PluginManager
import com.lagradost.cloudstream3.services.BackupWorkManager
import com.lagradost.cloudstream3.ui.BasePreferenceFragmentCompat
import com.lagradost.cloudstream3.ui.settings.Globals.EMULATOR
import com.lagradost.cloudstream3.ui.settings.SettingsFragment.Companion.getPref
import com.lagradost.cloudstream3.ui.settings.SettingsFragment.Companion.hideOn
import com.lagradost.cloudstream3.ui.settings.SettingsFragment.Companion.setPaddingBottom
import com.lagradost.cloudstream3.ui.settings.SettingsFragment.Companion.setToolBarScrollFlags
import com.lagradost.cloudstream3.ui.settings.SettingsFragment.Companion.setUpToolbar
import com.lagradost.cloudstream3.ui.settings.extensions.ExtensionsViewModel
import com.lagradost.cloudstream3.ui.settings.extensions.RepositoryData
import com.lagradost.cloudstream3.ui.settings.extensions.RepoAdapter
import com.lagradost.cloudstream3.ui.settings.plugins.PluginsFragment
import com.lagradost.cloudstream3.ui.settings.utils.getChooseFolderLauncher
import com.lagradost.cloudstream3.utils.BackupUtils
import com.lagradost.cloudstream3.utils.BackupUtils.restorePrompt
import com.lagradost.cloudstream3.utils.Coroutines.ioSafe
import com.lagradost.cloudstream3.utils.Coroutines.main
import com.lagradost.cloudstream3.utils.SingleSelectionHelper.showBottomDialog
import com.lagradost.cloudstream3.utils.SingleSelectionHelper.showDialog
import com.lagradost.cloudstream3.utils.UIHelper.clipboardHelper
import com.lagradost.cloudstream3.utils.UIHelper.dismissSafe
import com.lagradost.cloudstream3.utils.UIHelper.hideKeyboard
import com.lagradost.cloudstream3.utils.UIHelper.setText
import com.lagradost.cloudstream3.utils.VideoDownloadManager
import com.lagradost.cloudstream3.utils.txt
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// =======================
// STUBS (EXTENSION — WAJIB)
// =======================
fun Activity.installPreReleaseIfNeeded() { /* no-op */ }
fun Activity.runAutoUpdate(checkOnly: Boolean = false): Boolean = false

// =======================
// SETTINGS UPDATES
// =======================
class SettingsUpdates : BasePreferenceFragmentCompat() {

    private val extensionsViewModel: ExtensionsViewModel by activityViewModels()
    private lateinit var binding: SettingsUpdatesBinding

    private val pathPicker = getChooseFolderLauncher { uri, path ->
        val ctx = context ?: CloudStreamApp.context ?: return@getChooseFolderLauncher
        (path ?: uri.toString()).let {
            PreferenceManager.getDefaultSharedPreferences(ctx).edit {
                putString(getString(R.string.backup_path_key), uri.toString())
                putString(getString(R.string.backup_dir_key), it)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = SettingsUpdatesBinding.bind(view)
        setUpToolbar(R.string.category_updates)
        setPaddingBottom()
        setToolBarScrollFlags()
        setupUI()
        observeViewModel()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        hideKeyboard()
        setPreferencesFromResource(R.xml.settings_updates, rootKey)
    }

    private fun setupUI() {
        // =======================
        // MANUAL UPDATE
        // =======================
        getPref(R.string.manual_check_update_key)?.let { pref ->
            pref.summary = BuildConfig.VERSION_NAME
            pref.setOnPreferenceClickListener {
                ioSafe {
                    if (activity?.runAutoUpdate(false) == false) {
                        activity?.runOnUiThread {
                            showToast(R.string.no_update_found, Toast.LENGTH_SHORT)
                        }
                    }
                }
                true
            }
        }

        getPref(R.string.install_prerelease_key)?.let { pref ->
            pref.isVisible = BuildConfig.FLAVOR == "stable"
            pref.setOnPreferenceClickListener {
                activity?.installPreReleaseIfNeeded()
                true
            }
        }

        // =======================
        // MANUAL UPDATE PLUGINS
        // =======================
        getPref(R.string.manual_update_plugins_key)?.setOnPreferenceClickListener {
            ioSafe {
                PluginManager.___DO_NOT_CALL_FROM_A_PLUGIN_manuallyReloadAndUpdatePlugins(
                    activity ?: return@ioSafe
                )
            }
            true
        }

        // Setup RecyclerView
        binding.repoRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.repoRecyclerView.adapter = RepoAdapter(false, { repo ->
            findNavController().navigate(
                R.id.navigation_settings_updates_to_navigation_settings_plugins,
                PluginsFragment.newInstance(repo.name, repo.url, false)
            )
        }, { repo ->
            main {
                ioSafe {
                    PluginManager.removeRepository(repo)
                    extensionsViewModel.loadRepositories()
                    extensionsViewModel.loadStats()
                }
            }
        })
    }

    private fun observeViewModel() {
        extensionsViewModel.loadRepositories()
        extensionsViewModel.loadStats()

        extensionsViewModel.repositories.observe(viewLifecycleOwner) { list ->
            (binding.repoRecyclerView.adapter as? RepoAdapter)?.submitList(list.toList())
            binding.repoRecyclerView.isVisible = list.isNotEmpty()
        }

        extensionsViewModel.pluginStats.observe(viewLifecycleOwner) { stats ->
            if (stats == null) return@observeViewModel
            binding.apply {
                pluginStorageAppbar.isVisible = true
                if (stats.total == 0) {
                    pluginDownload.layoutParams.weight = 1f
                    pluginDisabled.layoutParams.weight = 0f
                    pluginNotDownloaded.layoutParams.weight = 0f
                } else {
                    pluginDownload.layoutParams.weight = stats.downloaded.toFloat()
                    pluginDisabled.layoutParams.weight = stats.disabled.toFloat()
                    pluginNotDownloaded.layoutParams.weight = stats.notDownloaded.toFloat()
                }
                pluginDownloadTxt.setText(stats.downloadedText)
                pluginDisabledTxt.setText(stats.disabledText)
                pluginNotDownloadedTxt.setText(stats.notDownloadedText)
            }
        }

        binding.pluginStorageAppbar.setOnClickListener {
            findNavController().navigate(
                R.id.navigation_settings_updates_to_navigation_settings_plugins,
                PluginsFragment.newInstance(getString(R.string.extensions), "", true)
            )
        }
    }

    private fun getBackupDirsForDisplay(): List<String> {
        return safe {
            context?.let { ctx ->
                val defaultDir =
                    BackupUtils.getDefaultBackupDir(ctx)?.filePath()
                val first = listOf(defaultDir)
                (
                    runCatching {
                        first + BackupUtils.getCurrentBackupDir(ctx).let {
                            it.first?.filePath() ?: it.second
                        }
                    }.getOrNull() ?: first
                )
                    .filterNotNull()
                    .distinct()
            }
        } ?: emptyList()
    }
}
