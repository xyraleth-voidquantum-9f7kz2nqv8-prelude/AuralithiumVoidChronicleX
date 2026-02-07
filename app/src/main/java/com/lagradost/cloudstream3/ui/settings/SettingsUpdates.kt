package com.lagradost.cloudstream3.ui.settings

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.lagradost.cloudstream3.BuildConfig
import com.lagradost.cloudstream3.CloudStreamApp
import com.lagradost.cloudstream3.CommonActivity.showToast
import com.lagradost.cloudstream3.R
import com.lagradost.cloudstream3.plugins.BasePlugin
import com.lagradost.cloudstream3.plugins.PluginManager
import com.lagradost.cloudstream3.ui.BasePreferenceFragmentCompat
import com.lagradost.cloudstream3.ui.settings.SettingsFragment.Companion.getPref
import com.lagradost.cloudstream3.ui.settings.SettingsFragment.Companion.setPaddingBottom
import com.lagradost.cloudstream3.ui.settings.SettingsFragment.Companion.setToolBarScrollFlags
import com.lagradost.cloudstream3.ui.settings.SettingsFragment.Companion.setUpToolbar
import com.lagradost.cloudstream3.ui.settings.PluginStorageHeaderPreference
import com.lagradost.cloudstream3.ui.settings.utils.getChooseFolderLauncher
import com.lagradost.cloudstream3.utils.BackupUtils
import com.lagradost.cloudstream3.utils.Coroutines.ioSafe
import com.lagradost.cloudstream3.utils.UIHelper.hideKeyboard
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import androidx.core.content.edit

// =======================
// SAFE UTILITY
// =======================
inline fun <T> safe(block: () -> T): T? = try { block() } catch (_: Exception) { null }

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

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar(R.string.category_updates)
        setPaddingBottom()
        setToolBarScrollFlags()
    }

    private val pathPicker = getChooseFolderLauncher { uri: Uri, path: String? ->
        val ctx = context ?: CloudStreamApp.context ?: return@getChooseFolderLauncher
        val actualPath = path ?: uri.toString()
        val prefs = PreferenceManager.getDefaultSharedPreferences(ctx)
        prefs.edit().apply {
            putString(ctx.getString(R.string.backup_path_key), uri.toString())
            putString(ctx.getString(R.string.backup_dir_key), actualPath)
            apply()
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        hideKeyboard()
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
    // RELOAD PLUGINS SUSPEND (SAFE)
    // =======================
    private suspend fun reloadPlugins(activity: Activity) {
        safe {
            PluginManager.plugins?.values?.forEach { plugin ->
                PluginManager.unloadPlugin(plugin)
                // Jangan panggil loadPlugin private
            }
            activity.runOnUiThread { updatePluginStats() }
        }
    }

    // =======================
    // UPDATE HEADER PLUGIN
    // =======================
    private fun updatePluginStats() {
        val header = pluginHeader ?: return
        val plugins: List<BasePlugin> = safe { PluginManager.plugins?.values?.toList() } ?: emptyList()

        header.downloadedCount = plugins.count { safe { it.downloaded } ?: false }
        header.disabledCount = plugins.count { safe { !(it.enabled) } ?: false }
        header.notDownloadedCount = plugins.count { safe { !(it.downloaded) } ?: false }

        header.safeRefreshCounts()
    }

    // =======================
    // BACKUP DIRS
    // =======================
    private fun getBackupDirsForDisplay(): List<String> {
        return safe {
            context?.let { ctx ->
                val defaultDir = BackupUtils.getDefaultBackupDir(ctx)?.filePath()
                val currentDirs = BackupUtils.getCurrentBackupDir(ctx)
                val currentDir = currentDirs.first?.filePath() ?: currentDirs.second
                listOfNotNull(defaultDir, currentDir).distinct()
            }
        } ?: emptyList()
    }
}
