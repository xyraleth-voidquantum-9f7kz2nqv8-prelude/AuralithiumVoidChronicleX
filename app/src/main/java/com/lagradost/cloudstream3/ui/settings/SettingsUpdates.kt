package com.lagradost.cloudstream3.ui.settings

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.preference.PreferenceManager
import androidx.preference.Preference
import com.lagradost.cloudstream3.BuildConfig
import com.lagradost.cloudstream3.CloudStreamApp
import com.lagradost.cloudstream3.CommonActivity.showToast
import com.lagradost.cloudstream3.R
import com.lagradost.cloudstream3.plugins.PluginManager
import com.lagradost.cloudstream3.ui.BasePreferenceFragmentCompat
import com.lagradost.cloudstream3.ui.settings.SettingsFragment.Companion.getPref
import com.lagradost.cloudstream3.ui.settings.SettingsFragment.Companion.setPaddingBottom
import com.lagradost.cloudstream3.ui.settings.SettingsFragment.Companion.setToolBarScrollFlags
import com.lagradost.cloudstream3.ui.settings.SettingsFragment.Companion.setUpToolbar
import com.lagradost.cloudstream3.ui.settings.extensions.PluginStorageHeaderPreference
import com.lagradost.cloudstream3.utils.Coroutines.ioSafe

// =======================
// STUBS (WAJIB BIAR BUILD)
// =======================
fun Activity.installPreReleaseIfNeeded() {
    // stable build → no-op
}

fun Activity.runAutoUpdate(checkOnly: Boolean = false): Boolean {
    return false
}

// =======================
// SETTINGS UPDATES
// =======================
class SettingsUpdates : BasePreferenceFragmentCompat() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar(R.string.category_updates)
        setPaddingBottom()
        setToolBarScrollFlags()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_updates, rootKey)

        // =======================
        // HEADER (PLUGIN STORAGE)
        // =======================
        val header =
            findPreference<PluginStorageHeaderPreference>(
                getString(R.string.plugin_storage_header_key)
            )

        fun refreshPluginStats() {
            val plugins = PluginManager.getPlugins()
            val disabled = PluginManager.getDisabledPlugins()

            val downloaded = plugins.size
            val disabledCount = disabled.size
            val notDownloaded = 0 // cloudstream juga 0

            header?.apply {
                downloadedCount = downloaded
                disabledCount = disabledCount
                notDownloadedCount = notDownloaded
                notifyChanged()
            }
        }

        refreshPluginStats()

        // =======================
        // MANUAL APP UPDATE
        // =======================
        getPref(R.string.manual_check_update_key)?.let { pref ->
            pref.summary = BuildConfig.VERSION_NAME
            pref.setOnPreferenceClickListener {
                ioSafe {
                    if (activity?.runAutoUpdate(false) == false) {
                        activity?.runOnUiThread {
                            showToast(
                                R.string.no_update_found,
                                Toast.LENGTH_SHORT
                            )
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
        // UPDATE PLUGINS
        // =======================
        getPref(R.string.manual_update_plugins_key)
            ?.setOnPreferenceClickListener {
                ioSafe {
                    PluginManager
                        .___DO_NOT_CALL_FROM_A_PLUGIN_manuallyReloadAndUpdatePlugins(
                            activity ?: return@ioSafe
                        )

                    activity?.runOnUiThread {
                        refreshPluginStats()
                        showToast(
                            R.string.updated_plugins,
                            Toast.LENGTH_SHORT
                        )
                    }
                }
                true
            }
    }
}