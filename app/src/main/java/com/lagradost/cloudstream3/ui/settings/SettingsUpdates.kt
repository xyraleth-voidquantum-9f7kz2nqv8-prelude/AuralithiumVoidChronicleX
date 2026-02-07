package com.lagradost.cloudstream3.ui.settings

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.lagradost.cloudstream3.BuildConfig
import com.lagradost.cloudstream3.CloudStreamApp
import com.lagradost.cloudstream3.R
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.databinding.SettingsUpdatesBinding
import com.lagradost.cloudstream3.plugins.PluginManager
import com.lagradost.cloudstream3.ui.BasePreferenceFragmentCompat
import com.lagradost.cloudstream3.ui.settings.SettingsFragment.Companion.getPref
import com.lagradost.cloudstream3.ui.settings.SettingsFragment.Companion.setPaddingBottom
import com.lagradost.cloudstream3.ui.settings.SettingsFragment.Companion.setToolBarScrollFlags
import com.lagradost.cloudstream3.ui.settings.SettingsFragment.Companion.setUpToolbar
import com.lagradost.cloudstream3.ui.settings.extensions.ExtensionsViewModel
import com.lagradost.cloudstream3.ui.settings.extensions.RepoAdapter
import com.lagradost.cloudstream3.ui.settings.plugins.PluginsFragment
import com.lagradost.cloudstream3.ui.settings.utils.getChooseFolderLauncher
import com.lagradost.cloudstream3.utils.Coroutines.ioSafe
import com.lagradost.cloudstream3.utils.Coroutines.main
import com.lagradost.cloudstream3.utils.UIHelper.setText

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
                            Toast.makeText(context, R.string.no_update_found, Toast.LENGTH_SHORT).show()
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
                PluginManager.reloadPlugins(activity ?: return@ioSafe)
            }
            true
        }

        // Setup RecyclerView
        binding.repoRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.repoRecyclerView.adapter = RepoAdapter(false, { repo ->
            try {
                findNavController().navigate(
                    R.id.navigation_settings_updates_to_navigation_settings_plugins,
                    PluginsFragment.newInstance(repo.name, repo.url, false)
                )
            } catch (_: Exception) {}
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

                fun setWeight(view: View, value: Float) {
                    val lp = view.layoutParams
                    if (lp is LinearLayout.LayoutParams) {
                        lp.weight = value
                        view.layoutParams = lp
                    }
                }

                if (stats.total == 0) {
                    setWeight(pluginDownload, 1f)
                    setWeight(pluginDisabled, 0f)
                    setWeight(pluginNotDownloaded, 0f)
                } else {
                    setWeight(pluginDownload, stats.downloaded.toFloat())
                    setWeight(pluginDisabled, stats.disabled.toFloat())
                    setWeight(pluginNotDownloaded, stats.notDownloaded.toFloat())
                }

                pluginDownloadTxt.setText(stats.downloadedText)
                pluginDisabledTxt.setText(stats.disabledText)
                pluginNotDownloadedTxt.setText(stats.notDownloadedText)
            }
        }

        binding.pluginStorageAppbar.setOnClickListener {
            try {
                findNavController().navigate(
                    R.id.navigation_settings_updates_to_navigation_settings_plugins,
                    PluginsFragment.newInstance(getString(R.string.extensions), "", true)
                )
            } catch (_: Exception) {}
        }
    }
}
