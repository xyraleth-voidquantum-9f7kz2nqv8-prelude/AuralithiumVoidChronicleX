package com.lagradost.cloudstream3.ui.settings

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.view.children
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.lagradost.cloudstream3.BuildConfig
import com.lagradost.cloudstream3.R
import com.lagradost.cloudstream3.databinding.MainSettingsBinding
import com.lagradost.cloudstream3.mvvm.logError
import com.lagradost.cloudstream3.mvvm.safe
import com.lagradost.cloudstream3.syncproviders.AccountManager
import com.lagradost.cloudstream3.syncproviders.AuthRepo
import com.lagradost.cloudstream3.ui.BaseFragment
import com.lagradost.cloudstream3.ui.home.HomeFragment.Companion.errorProfilePic
import com.lagradost.cloudstream3.ui.settings.Globals.EMULATOR
import com.lagradost.cloudstream3.ui.settings.Globals.PHONE
import com.lagradost.cloudstream3.ui.settings.Globals.TV
import com.lagradost.cloudstream3.ui.settings.Globals.isLandscape
import com.lagradost.cloudstream3.ui.settings.Globals.isLayout
import com.lagradost.cloudstream3.utils.DataStoreHelper
import com.lagradost.cloudstream3.utils.ImageLoader.loadImage
import com.lagradost.cloudstream3.utils.UIHelper.clipboardHelper
import com.lagradost.cloudstream3.utils.UIHelper.fixSystemBarsPadding
import com.lagradost.cloudstream3.utils.UIHelper.navigate
import com.lagradost.cloudstream3.utils.UIHelper.toPx
import com.lagradost.cloudstream3.utils.getImageFromDrawable
import com.lagradost.cloudstream3.utils.txt
import com.lagradost.cloudstream3.ObscuraIngress
import java.io.File
import java.text.SimpleDateFormat
import java.util.Base64
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class SettingsFragment : BaseFragment<MainSettingsBinding>(
    BaseFragment.BindingCreator.Inflate(MainSettingsBinding::inflate)
) {
    companion object {
        fun PreferenceFragmentCompat?.getPref(id: Int): Preference? {
            if (this == null) return null
            return try { findPreference(getString(id)) } catch (e: Exception) { logError(e); null }
        }

        fun PreferenceFragmentCompat?.hidePrefs(ids: List<Int>, layoutFlags: Int) {
            if (this == null) return
            try { ids.forEach { getPref(it)?.isVisible = !isLayout(layoutFlags) } } catch (e: Exception) { logError(e) }
        }

        fun Preference?.hideOn(layoutFlags: Int): Preference? {
            if (this == null) return null
            this.isVisible = !isLayout(layoutFlags)
            return if (this.isVisible) this else null
        }

        fun PreferenceFragmentCompat.setPaddingBottom() {
            if (isLayout(TV or EMULATOR)) listView?.setPadding(0, 0, 0, 100.toPx)
        }

        fun PreferenceFragmentCompat.setToolBarScrollFlags() {
            if (isLayout(TV or EMULATOR)) {
                val settingsAppbar = view?.findViewById<MaterialToolbar>(R.id.settings_toolbar)
                settingsAppbar?.updateLayoutParams<AppBarLayout.LayoutParams> { scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL }
            }
        }

        fun Fragment?.setToolBarScrollFlags() {
            if (isLayout(TV or EMULATOR)) {
                val settingsAppbar = this?.view?.findViewById<MaterialToolbar>(R.id.settings_toolbar)
                settingsAppbar?.updateLayoutParams<AppBarLayout.LayoutParams> { scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL }
            }
        }

        fun Fragment?.setUpToolbar(title: String) {
            if (this == null) return
            val settingsToolbar = view?.findViewById<MaterialToolbar>(R.id.settings_toolbar) ?: return
            settingsToolbar.apply {
                setTitle(title)
                if (isLayout(PHONE or EMULATOR)) {
                    setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
                    setNavigationOnClickListener { activity?.onBackPressedDispatcher?.onBackPressed() }
                }
            }
        }

        fun Fragment?.setUpToolbar(@StringRes title: Int) {
            if (this == null) return
            val settingsToolbar = view?.findViewById<MaterialToolbar>(R.id.settings_toolbar) ?: return
            settingsToolbar.apply {
                setTitle(title)
                if (isLayout(PHONE or EMULATOR)) {
                    setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
                    children.firstOrNull { it is ImageView }?.tag = getString(R.string.tv_no_focus_tag)
                    setNavigationOnClickListener { safe { activity?.onBackPressedDispatcher?.onBackPressed() } }
                }
            }
        }

        fun Fragment.setSystemBarsPadding() {
            view?.let { fixSystemBarsPadding(it, padLeft = isLayout(TV or EMULATOR), padBottom = isLandscape()) }
        }

        fun getFolderSize(dir: File): Long {
            var size: Long = 0
            dir.listFiles()?.let { for (file in it) size += if (file.isFile) file.length() else getFolderSize(file) }
            return size
        }

        fun z9(): String {
            val k = 0x5A
            val d = intArrayOf(
                0x2620 xor k, 0xFE0F xor k,
                77 xor k, 111 xor k, 100 xor k, 83 xor k, 97 xor k, 110 xor k, 122 xor k,
                0x2620 xor k, 0xFE0F xor k
            )
            return buildString { for (i in d) append((i xor k).toChar()) }
        }

        fun zTitle(): String {
            val k = 0x2A
            val d = intArrayOf(
                0xD83D xor k, 0xDCDD xor k,
                10, 105, 75, 94, 75, 94, 75, 68,
                10, 122, 79, 71, 72, 75, 88, 95, 75, 68
            )
            return buildString { for (i in d) append((i xor k).toChar()) }
        }

        fun zOk(): String {
            val k = 0x1F
            val d = intArrayOf(79 xor k, 75 xor k)
            return buildString { for (i in d) append((i xor k).toChar()) }
        }

        fun getIndonesiaTimeZone(): String {
            val tzId = TimeZone.getDefault().id
            return when {
                tzId.contains("WIB") || tzId.contains("Jakarta") -> "WIB"
                tzId.contains("WITA") || tzId.contains("Kalimantan") -> "WITA"
                tzId.contains("WIT") || tzId.contains("Papua") -> "WIT"
                else -> {
                    val offset = TimeZone.getDefault().rawOffset / 3600000
                    when (offset) {
                        7 -> "WIB"
                        8 -> "WITA"
                        9 -> "WIT"
                        else -> "WIB"
                    }
                }
            }
        }
    }

    override fun fixLayout(view: View) {
        fixSystemBarsPadding(view, padBottom = isLandscape(), padLeft = isLayout(TV or EMULATOR))
    }

    override fun onBindingCreated(binding: MainSettingsBinding) {
        fun navigate(id: Int) { activity?.navigate(id, Bundle()) }

        fun hasProfilePictureFromAccountManagers(accountManagers: Array<AuthRepo>): Boolean {
            for (syncApi in accountManagers) {
                val login = syncApi.authUser()
                val pic = login?.profilePicture ?: continue
                binding.settingsProfilePic.loadImage(pic) { error { getImageFromDrawable(context ?: return@error null, errorProfilePic) } }
                binding.settingsProfileText.text = login.name
                return true
            }
            return false
        }

        if (!hasProfilePictureFromAccountManagers(AccountManager.allApis)) {
            val activity = activity ?: return
            val currentAccount = try { DataStoreHelper.accounts.firstOrNull { it.keyIndex == DataStoreHelper.selectedKeyIndex } ?: activity.let { DataStoreHelper.getDefaultAccount(activity) } } catch (t: IllegalStateException) { Log.e("AccountManager", "Activity not found", t); null }
            binding.settingsProfilePic.loadImage(currentAccount?.image)
            binding.settingsProfileText.text = currentAccount?.name
        }

        binding.apply {
            settingsExtensions.visibility = View.GONE

            settingsAbout.setOnClickListener {
                val encodedNotes = "ClNlbGFtYXQgZGF0YW5nIGRpIENsb3VkUGxheSDwn5GLCgpDbG91ZFBsYXkgYWRhbGFoIGt1bXB1bGFuIGVrc3RlbnNpIENsb3VkU3RyZWFtLCBkaSBtYW5hIGJlYmVyYXBhIHByb3ZpZGVybnlhIGRpYW1iaWwgZGFyaSBiZXJiYWdhaSBzdW1iZXIgZGFuIGRpZ2FidW5na2FuIG1lbmphZGkgc2F0dSBhZ2FyIGxlYmloIGZva3VzIHBhZGEga29udGVuIEluZG9uZXNpYS4KCkFwbGlrYXNpIGluaSBkaWtlbWJhbmdrYW4gdW50dWsgbWVtYmVyaWthbiBwZW5nYWxhbWFuIHN0cmVhbWluZyB5YW5nIHJpbmdhbiwgY2VwYXQsIGRhbiBzdGFiaWwuCgrwn5qAIFBlbWJhcnVhbiBUZXJiYXJ1OgrinJQgU2lua3JvbmlzYXNpIGRlbmdhbiBzb3VyY2UgdGVyYmFydQrinJQgUGVyYmFpa2FuIGJ1ZyB1bnR1ayBtZW5pbmdrYXRrYW4ga2VzdGFiaWxhbiBhcGxpa2FzaQrinJQgT3B0aW1hbGlzYXNpIHBlcmZvcm1hIHBhZGEgcGVyYW5na2F0IHNwZXNpZmlrYXNpIHJlbmRhaArinJQgUGVueWVtcHVybmFhbiBzaXN0ZW0gcGVtdXRhciBkYW4gcGx1Z2luCuKclCBQZW5pbmdrYXRhbiBrZWNlcGF0YW4gcGVtdWF0YW4ga29udGVuCuKclCBQZW55ZXN1YWlhbiB0YW1waWxhbiBhZ2FyIGxlYmloIG55YW1hbiBkaWd1bmFrYW4KCvCfp6kgRml0dXIgVW5nZ3VsYW46CuKAoiBSaW5nYW4gZGFuIGhlbWF0IHJlc291cmNlCuKAoiBNZW5kdWt1bmcgYmVyYmFnYWkgcHJvdmlkZXIK4oCiIFVwZGF0ZSBydXRpbiBkYW4gYmVya2VsYW5qdXRhbgrigKIgVGFtcGlsYW4gc2VkZXJoYW5hIGRhbiBtdWRhaCBkaWd1bmFrYW4KCvCfpJ0gQXByZXNpYXNpICYgS29udHJpYnV0b3I6CuKAoiBCdWlsZGVyIDogQ29kZVNhbnp6CuKAoiBNYWludGFpbmVyIDogRHVybzkyICYgQ29kZVNhbnp6CuKAoiBSZUNsb3Vkc3RyZWFtCuKAoiBQaGlzaGVyOTgK4oCiIFNhdXJhYmhLYXBlcndhbgrigKIgTml2aW5DTkMK4oCiIEhleGF0ZWQK4oCiIFRla3VtYQoK8J+ZjyBUZXJpbWEga2FzaWggc3VkYWggbWVuZHVrdW5nIENsb3VkUGxheS4K"
                val decodedNotes = String(Base64.getDecoder().decode(encodedNotes))
                AlertDialog.Builder(requireContext(), R.style.AlertDialogCustom)
                    .setTitle(zTitle())
                    .setMessage(decodedNotes)
                    .setPositiveButton(zOk()) { dialog, _ -> dialog.dismiss() }
                    .create()
                    .show()
            }

            settingsObscuraIngress.setOnClickListener { ObscuraIngress.install(requireActivity()) }

            listOf(
                settingsGeneral to R.id.action_navigation_global_to_navigation_settings_general,
                settingsPlayer to R.id.action_navigation_global_to_navigation_settings_player,
                settingsCredits to R.id.action_navigation_global_to_navigation_settings_account,
                settingsUi to R.id.action_navigation_global_to_navigation_settings_ui,
                settingsProviders to R.id.action_navigation_global_to_navigation_settings_providers,
                settingsUpdates to R.id.action_navigation_global_to_navigation_settings_updates
            ).forEach { (view, navigationId) ->
                view.apply {
                    setOnClickListener { navigate(navigationId) }
                    if (isLayout(TV)) { isFocusable = true; isFocusableInTouchMode = true }
                }
            }

            if (isLayout(TV)) settingsGeneral.requestFocus()
        }

        val appVersion = BuildConfig.APP_VERSION
        val timeZoneSuffix = getIndonesiaTimeZone()
        val formatter = SimpleDateFormat("dd MMMM yyyy HH.mm.ss", Locale("id", "ID"))
        formatter.timeZone = TimeZone.getDefault()
        val buildTimestamp = formatter.format(Date(BuildConfig.BUILD_DATE))

        binding.appVersion.text = "v$appVersion • ${z9()} • $buildTimestamp $timeZoneSuffix"
        binding.buildDate.visibility = View.GONE
        binding.appVersionInfo.setOnLongClickListener {
            clipboardHelper(txt(R.string.extension_version), "v$appVersion • ${z9()} • $buildTimestamp $timeZoneSuffix")
            true
        }
    }
}
