package com.lagradost.cloudstream3.ui.settings

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.lagradost.cloudstream3.BuildConfig
import com.lagradost.cloudstream3.CloudStreamApp
import com.lagradost.cloudstream3.CommonActivity.showToast
import com.lagradost.cloudstream3.R
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.databinding.LogcatBinding
import com.lagradost.cloudstream3.mvvm.logError
import com.lagradost.cloudstream3.mvvm.safe
import com.lagradost.cloudstream3.network.initClient
import com.lagradost.cloudstream3.plugins.Plugin
import com.lagradost.cloudstream3.plugins.PluginManager
import com.lagradost.cloudstream3.services.BackupWorkManager
import com.lagradost.cloudstream3.ui.BasePreferenceFragmentCompat
import com.lagradost.cloudstream3.ui.settings.Globals.EMULATOR
import com.lagradost.cloudstream3.ui.settings.SettingsFragment.Companion.getPref
import com.lagradost.cloudstream3.ui.settings.SettingsFragment.Companion.hideOn
import com.lagradost.cloudstream3.ui.settings.SettingsFragment.Companion.setPaddingBottom
import com.lagradost.cloudstream3.ui.settings.SettingsFragment.Companion.setToolBarScrollFlags
import com.lagradost.cloudstream3.ui.settings.SettingsFragment.Companion.setUpToolbar
import com.lagradost.cloudstream3.ui.settings.PluginStorageHeaderPreference
import com.lagradost.cloudstream3.ui.settings.utils.getChooseFolderLauncher
import com.lagradost.cloudstream3.utils.BackupUtils
import com.lagradost.cloudstream3.utils.BackupUtils.restorePrompt
import com.lagradost.cloudstream3.utils.Coroutines.ioSafe
import com.lagradost.cloudstream3.utils.SingleSelectionHelper.showBottomDialog
import com.lagradost.cloudstream3.utils.SingleSelectionHelper.showDialog
import com.lagradost.cloudstream3.utils.UIHelper.clipboardHelper
import com.lagradost.cloudstream3.utils.UIHelper.dismissSafe
import com.lagradost.cloudstream3.utils.UIHelper.hideKeyboard
import com.lagradost.cloudstream3.utils.VideoDownloadManager
import com.lagradost.cloudstream3.utils.txt
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// =======================
// STUBS
// =======================
fun Activity.installPreReleaseIfNeeded() { }
fun Activity.runAutoUpdate(checkOnly: Boolean = false): Boolean = false

// =======================
// SAFE REFRESH COUNTS
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar(R.string.category_updates)
        setPaddingBottom()
        setToolBarScrollFlags()
    }

    private val pathPicker = getChooseFolderLauncher { uri, path ->
        val ctx = context ?: CloudStreamApp.context ?: return@getChooseFolderLauncher
        (path ?: uri.toString()).let {
            PreferenceManager.getDefaultSharedPreferences(ctx).edit {
                putString(getString(R.string.backup_path_key), uri.toString())
                putString(getString(R.string.backup_dir_key), it)
            }
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
            ioSafe {
                reloadAndDownloadPlugins()
            }
            true
        }

        // =======================
        // UPDATE HEADER SAAT OPEN FRAGMENT
        // =======================
        ioSafe { reloadAndDownloadPlugins() }
    }

    // =======================
    // RELOAD PLUGIN DAN UPDATE HEADER
    // =======================
    private suspend fun reloadAndDownloadPlugins() {
        val act = activity ?: return
        // Reload semua plugin yang ada (suspend)
        PluginManager.___DO_NOT_CALL_FROM_A_PLUGIN_manuallyReloadAndUpdatePlugins(act)

        // Update header
        act.runOnUiThread { updatePluginStats() }
    }

    // =======================
    // UPDATE HEADER PLUGIN
    // =======================
    private fun updatePluginStats() {
        val header = pluginHeader ?: return
        val plugins: List<Plugin> = safe { PluginManager.getPlugins() } ?: emptyList()

        header.downloadedCount = plugins.count { safe { it.isDownloaded } == true }
        header.disabledCount = plugins.count { safe { it.isDisabled } == true }
        header.notDownloadedCount = plugins.count { safe { it.isDownloaded } == false }

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
