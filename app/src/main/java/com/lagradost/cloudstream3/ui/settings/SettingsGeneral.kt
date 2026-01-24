package com.lagradost.cloudstream3.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.core.os.ConfigurationCompat
import androidx.preference.PreferenceManager
import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.APIHolder.allProviders
import com.lagradost.cloudstream3.CloudStreamApp
import com.lagradost.cloudstream3.CloudStreamApp.Companion.getKey
import com.lagradost.cloudstream3.CloudStreamApp.Companion.setKey
import com.lagradost.cloudstream3.CommonActivity
import com.lagradost.cloudstream3.CommonActivity.showToast
import com.lagradost.cloudstream3.MainActivity
import com.lagradost.cloudstream3.R
import com.lagradost.cloudstream3.databinding.AddRemoveSitesBinding
import com.lagradost.cloudstream3.databinding.AddSiteInputBinding
import com.lagradost.cloudstream3.ui.BasePreferenceFragmentCompat
import com.lagradost.cloudstream3.ui.settings.Globals.EMULATOR
import com.lagradost.cloudstream3.ui.settings.Globals.TV
import com.lagradost.cloudstream3.ui.settings.SettingsFragment.Companion.getPref
import com.lagradost.cloudstream3.ui.settings.SettingsFragment.Companion.hideOn
import com.lagradost.cloudstream3.ui.settings.SettingsFragment.Companion.setPaddingBottom
import com.lagradost.cloudstream3.ui.settings.SettingsFragment.Companion.setToolBarScrollFlags
import com.lagradost.cloudstream3.ui.settings.SettingsFragment.Companion.setUpToolbar
import com.lagradost.cloudstream3.ui.settings.utils.getChooseFolderLauncher
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.utils.BatteryOptimizationChecker.isAppRestricted
import com.lagradost.cloudstream3.utils.BatteryOptimizationChecker.showBatteryOptimizationDialog
import com.lagradost.cloudstream3.utils.SingleSelectionHelper.showBottomDialog
import com.lagradost.cloudstream3.utils.SingleSelectionHelper.showDialog
import com.lagradost.cloudstream3.utils.SingleSelectionHelper.showMultiDialog
import com.lagradost.cloudstream3.utils.SubtitleHelper
import com.lagradost.cloudstream3.utils.UIHelper.dismissSafe
import com.lagradost.cloudstream3.utils.UIHelper.hideKeyboard
import com.lagradost.cloudstream3.utils.USER_PROVIDER_API
import com.lagradost.cloudstream3.utils.VideoDownloadManager
import java.util.Locale

/* ================= LOCALE ================= */

fun getCurrentLocale(context: Context): String {
    val conf = context.resources.configuration
    return ConfigurationCompat.getLocales(conf)[0]?.toLanguageTag() ?: "en"
}

val appLanguages = arrayListOf(
    Pair("Afrikaans", "af"),
    Pair("Azərbaycan dili", "az"),
    Pair("Bahasa Indonesia", "in"),
    Pair("Bahasa Melayu", "ms"),
    Pair("Deutsch", "de"),
    Pair("English", "en"),
    Pair("Español", "es"),
    Pair("Esperanto", "eo"),
    Pair("Français", "fr"),
    Pair("Galego", "gl"),
    Pair("hrvatski", "hr"),
    Pair("Italiano", "it"),
    Pair("Latviešu valoda", "lv"),
    Pair("Lietuvių kalba", "lt"),
    Pair("Magyar", "hu"),
    Pair("Malti", "mt"),
    Pair("Nederlands", "nl"),
    Pair("Norsk bokmål", "no"),
    Pair("Norsk nynorsk", "nn"),
    Pair("Polski", "pl"),
    Pair("Português", "pt"),
    Pair("Português (Brasil)", "pt-BR"),
    Pair("Română", "ro"),
    Pair("Slovenčina", "sk"),
    Pair("Soomaaliga", "so"),
    Pair("Svenska", "sv"),
    Pair("Tagalog", "tl"),
    Pair("Tiếng Việt", "vi"),
    Pair("Türkçe", "tr"),
    Pair("Wikang Filipino", "fil"),
    Pair("Čeština", "cs"),
    Pair("Ελληνικά", "el"),
    Pair("български", "bg"),
    Pair("македонски", "mk"),
    Pair("русский", "ru"),
    Pair("українська", "uk"),
    Pair("עברית", "iw"),
    Pair("اردو", "ur"),
    Pair("العربية", "ar"),
    Pair("فارسی", "fa"),
    Pair("नेपाली", "ne"),
    Pair("हिन्दी", "hi"),
    Pair("অসমীয়া", "as"),
    Pair("বাংলা", "bn"),
    Pair("ଓଡ଼ିଆ", "or"),
    Pair("தமிழ்", "ta"),
    Pair("ಕನ್ನಡ", "kn"),
    Pair("മലയാളം", "ml"),
    Pair("ဗမာစာ", "my"),
    Pair("ትግርኛ", "ti"),
    Pair("አማርኛ", "am"),
    Pair("中文", "zh"),
    Pair("日本語", "ja"),
    Pair("正體中文(臺灣)", "zh-TW"),
    Pair("한국어", "ko")
).sortedBy { it.first.lowercase(Locale.ROOT) }

fun Pair<String, String>.nameNextToFlagEmoji(): String {
    val flag = SubtitleHelper.getFlagFromIso(this.second) ?: "\uD83C\uDDE6\uD83C\uDDE6"
    return "$flag ${this.first}"
}

/* ================= SETTINGS GENERAL ================= */

class SettingsGeneral : BasePreferenceFragmentCompat() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar(R.string.category_general)
        setPaddingBottom()
        setToolBarScrollFlags()
    }

    data class CustomSite(
        @JsonProperty("parentJavaClass") val parentJavaClass: String,
        @JsonProperty("name") val name: String,
        @JsonProperty("url") val url: String,
        @JsonProperty("lang") val lang: String
    )

    private val pathPicker = getChooseFolderLauncher { uri, path ->
        val ctx = context ?: CloudStreamApp.context ?: return@getChooseFolderLauncher
        PreferenceManager.getDefaultSharedPreferences(ctx).edit {
            putString(getString(R.string.download_path_key), uri.toString())
            putString(getString(R.string.download_path_key_visual), path ?: uri.toString())
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        hideKeyboard()
        setPreferencesFromResource(R.xml.settings_general, rootKey)

        val settingsManager =
            PreferenceManager.getDefaultSharedPreferences(requireContext())

        /* ================= TELEGRAM ================= */

        getPref(R.string.telegram_key)?.apply {
            summary = getString(R.string.telegram_desc)
            setOnPreferenceClickListener {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://t.me/TeamCloudPlay")
                    )
                )
                true
            }
        }

        /* ================= DONASI ================= */

        getPref(R.string.support_key)?.apply {
            summary = getString(R.string.support_desc)
            setOnPreferenceClickListener {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                            "https://raw.githubusercontent.com/sannafanity-ui/foto/main/Donasi.png"
                        )
                    )
                )
                true
            }
        }

        /* ================= BATTERY ================= */

        getPref(R.string.battery_optimisation_key)
            ?.hideOn(TV or EMULATOR)
            ?.setOnPreferenceClickListener {
                val ctx = context ?: return@setOnPreferenceClickListener false
                if (isAppRestricted(ctx)) {
                    ctx.showBatteryOptimizationDialog()
                } else {
                    showToast(R.string.app_unrestricted_toast)
                }
                true
            }

        /* ================= BAHASA / LOCALE ================= */

        getPref(R.string.locale_key)?.setOnPreferenceClickListener { pref ->
            val current = getCurrentLocale(pref.context)
            val langTags = appLanguages.map { it.second }
            val langNames = appLanguages.map { it.nameNextToFlagEmoji() }
            val currentIndex = langTags.indexOf(current)

            activity?.showDialog(
                langNames,
                currentIndex,
                getString(R.string.app_language),
                true,
                {}
            ) { selectedIndex ->
                val langTag = langTags[selectedIndex]
                CommonActivity.setLocale(activity, langTag)
                settingsManager.edit {
                    putString(getString(R.string.locale_key), langTag)
                }
                activity?.recreate()
            }
            true
        }

        /* ================= DNS ================= */

        getPref(R.string.dns_key)?.setOnPreferenceClickListener {
            val names = resources.getStringArray(R.array.dns_pref)
            val values = resources.getIntArray(R.array.dns_pref_values)
            val current = settingsManager.getInt(getString(R.string.dns_pref), 0)

            activity?.showBottomDialog(
                names.toList(),
                values.indexOf(current),
                getString(R.string.dns_pref),
                true,
                {}
            ) { selectedIndex ->
                settingsManager.edit {
                    putInt(getString(R.string.dns_pref), values[selectedIndex])
                }
            }
            true
        }

        /* ================= DOWNLOAD PATH ================= */

        getPref(R.string.download_path_key)?.setOnPreferenceClickListener {
            val dirs =
                listOfNotNull(VideoDownloadManager.getDefaultDir(requireContext())?.filePath()) +
                        requireContext().getExternalFilesDirs("").mapNotNull { it.path }

            val currentDir = settingsManager.getString(
                getString(R.string.download_path_key_visual),
                dirs.firstOrNull()
            )

            activity?.showBottomDialog(
                dirs + listOf(getString(R.string.custom)),
                dirs.indexOf(currentDir),
                getString(R.string.download_path_pref),
                true,
                {}
            ) {
                if (it == dirs.size) {
                    pathPicker.launch(Uri.EMPTY)
                } else {
                    settingsManager.edit {
                        putString(getString(R.string.download_path_key), dirs[it])
                        putString(getString(R.string.download_path_key_visual), dirs[it])
                    }
                }
            }
            true
        }

        /* ================= JSDELIVR PROXY ================= */

        settingsManager.edit {
            putBoolean(
                getString(R.string.jsdelivr_proxy_key),
                getKey(getString(R.string.jsdelivr_proxy_key), false) ?: false
            )
        }

        getPref(R.string.jsdelivr_proxy_key)?.setOnPreferenceChangeListener { _, newValue ->
            setKey(getString(R.string.jsdelivr_proxy_key), newValue)
            true
        }

        /* ================= CUSTOM SITE ================= */

        fun getCurrent(): MutableList<CustomSite> =
            getKey<Array<CustomSite>>(USER_PROVIDER_API)?.toMutableList()
                ?: mutableListOf()

        fun showAdd() {
            val providers =
                synchronized(allProviders) {
                    allProviders
                        .distinctBy { it.javaClass }
                        .sortedBy { it.name }
                }

            activity?.showDialog(
                providers.map { "${it.name} (${it.mainUrl})" },
                -1,
                getString(R.string.add_site_pref),
                true,
                {}
            ) { selection ->
                val provider = providers.getOrNull(selection) ?: return@showDialog

                val binding =
                    AddSiteInputBinding.inflate(layoutInflater, null, false)

                val dialog =
                    AlertDialog.Builder(
                        context ?: return@showDialog,
                        R.style.AlertDialogCustom
                    )
                        .setView(binding.root)
                        .create()

                dialog.show()

                binding.text2.text = provider.name

                binding.applyBtt.setOnClickListener {
                    val name = binding.siteNameInput.text?.toString()
                    val url = binding.siteUrlInput.text?.toString()
                    val lang = binding.siteLangInput.text?.toString()

                    val realLang =
                        if (lang.isNullOrBlank()) provider.lang else lang

                    if (url.isNullOrBlank() || name.isNullOrBlank()) {
                        showToast(R.string.error_invalid_data)
                        return@setOnClickListener
                    }

                    val current = getCurrent()
                    current.add(
                        CustomSite(
                            provider.javaClass.simpleName,
                            name,
                            url,
                            realLang
                        )
                    )

                    setKey(USER_PROVIDER_API, current.toTypedArray())
                    MainActivity.afterPluginsLoadedEvent.invoke(false)
                    dialog.dismissSafe(activity)
                }

                binding.cancelBtt.setOnClickListener {
                    dialog.dismissSafe(activity)
                }
            }
        }

        fun showDelete() {
            val current = getCurrent()
            activity?.showMultiDialog(
                current.map { it.name },
                listOf(),
                getString(R.string.remove_site_pref),
                {}
            ) { indexes ->
                current.removeAll(indexes.map { current[it] })
                setKey(USER_PROVIDER_API, current.toTypedArray())
            }
        }

        fun showAddOrDelete() {
            val binding =
                AddRemoveSitesBinding.inflate(layoutInflater, null, false)

            val dialog =
                AlertDialog.Builder(
                    context ?: return,
                    R.style.AlertDialogCustom
                )
                    .setView(binding.root)
                    .create()

            dialog.show()

            binding.addSite.setOnClickListener {
                showAdd()
                dialog.dismissSafe(activity)
            }

            binding.removeSite.setOnClickListener {
                showDelete()
                dialog.dismissSafe(activity)
            }
        }

        getPref(R.string.override_site_key)?.setOnPreferenceClickListener {
            if (getCurrent().isEmpty()) showAdd() else showAddOrDelete()
            true
        }
    }
}