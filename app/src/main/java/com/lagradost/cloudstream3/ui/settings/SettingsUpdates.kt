package com.lagradost.cloudstream3.ui.settings

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.lagradost.cloudstream3.BuildConfig
import com.lagradost.cloudstream3.CloudStreamApp
import com.lagradost.cloudstream3.CommonActivity.showToast
import com.lagradost.cloudstream3.R
import com.lagradost.cloudstream3.databinding.LogcatBinding
import com.lagradost.cloudstream3.plugins.BasePlugin
import com.lagradost.cloudstream3.plugins.PluginManager
import com.lagradost.cloudstream3.ui.BasePreferenceFragmentCompat
import com.lagradost.cloudstream3.ui.settings.PluginStorageHeaderPreference
import com.lagradost.cloudstream3.ui.settings.SettingsFragment.Companion.getPref
import com.lagradost.cloudstream3.ui.settings.SettingsFragment.Companion.setPaddingBottom
import com.lagradost.cloudstream3.ui.settings.SettingsFragment.Companion.setToolBarScrollFlags
import com.lagradost.cloudstream3.ui.settings.SettingsFragment.Companion.setUpToolbar
import com.lagradost.cloudstream3.utils.Coroutines
import com.lagradost.cloudstream3.utils.Coroutines.ioSafe
import com.lagradost.cloudstream3.utils.getChooseFolderLauncher
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

// =======================
// STUBS
// =======================
fun Activity.installPreReleaseIfNeeded() { }
fun Activity.runAutoUpdate(checkOnly: Boolean = false): Boolean = false

// =======================
// EXTENSION SAFE REFRESH COUNTS
// =======================
fun PluginStorageHeaderPreference.safeRefreshCounts() {
    try {
        val method = this.javaClass.superclass.getDeclaredMethod("notifyChanged")
        method.isAccessible = true
        method.invoke(this)
    } catch (_: Exception) {}
}

// =======================
// SETTINGS UPDATES
// =======================
class SettingsUpdates : BasePreferenceFragmentCompat() {

    private var pluginHeader: PluginStorageHeaderPreference? = null
    private val mainScope = MainScope()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar(R.string.category_updates)
        setPaddingBottom()
        setToolBarScrollFlags()
    }

    // =======================
    // FOLDER PICKER
    // =======================
    private val pathPicker = getChooseFolderLauncher { uri: Uri?, path: String? ->
        val ctx = context ?: CloudStreamApp.context ?: return@getChooseFolderLauncher
        (path ?: uri.toString()).let {
            PreferenceManager.getDefaultSharedPreferences(ctx).edit {
                putString(getString(R.string.backup_path_key), uri.toString())
                putString(getString(R.string.backup_dir_key), it)
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_updates, rootKey)

        pluginHeader = findPreference(getString(R.string.plugin_storage_header_key))

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
        // AUTO UPDATE PLUGINS
        // =======================
        getPref(R.string.manual_update_plugins_key)?.setOnPreferenceClickListener {
            activity?.let { act ->
                mainScope.launch { reloadPlugins(act) }
            }
            true
        }

        // =======================
        // UPDATE HEADER SAAT OPEN FRAGMENT
        // =======================
        activity?.let { act ->
            mainScope.launch { reloadPlugins(act) }
        }
    }

    // =======================
    // RELOAD PLUGINS SUSPEND
    // =======================
    private suspend fun reloadPlugins(activity: Activity) {
        PluginManager.plugins?.values?.forEach { plugin ->
            PluginManager.unloadPlugin(plugin)
            PluginManager.loadPlugin(plugin) // gunakan versi publik
        }
        activity.runOnUiThread { updatePluginStats() }
    }

    // =======================
    // UPDATE HEADER PLUGIN
    // =======================
    private fun updatePluginStats() {
        val header = pluginHeader ?: return
        val plugins: List<BasePlugin> = PluginManager.plugins?.values?.toList() ?: emptyList()

        header.downloadedCount = plugins.count { it.downloaded }
        header.disabledCount = plugins.count { !it.enabled }
        header.notDownloadedCount = plugins.count { !it.downloaded }

        header.safeRefreshCounts()
    }
}
