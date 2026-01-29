package com.lagradost.cloudstream3.ui.settings.extensions

import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.lagradost.cloudstream3.CommonActivity.showToast
import com.lagradost.cloudstream3.MainActivity.Companion.afterRepositoryLoadedEvent
import com.lagradost.cloudstream3.R
import com.lagradost.cloudstream3.databinding.AddRepoInputBinding
import com.lagradost.cloudstream3.databinding.FragmentExtensionsBinding
import com.lagradost.cloudstream3.mvvm.observe
import com.lagradost.cloudstream3.mvvm.observeNullable
import com.lagradost.cloudstream3.plugins.RepositoryManager
import com.lagradost.cloudstream3.ui.BaseFragment
import com.lagradost.cloudstream3.ui.result.FOCUS_SELF
import com.lagradost.cloudstream3.ui.result.setLinearListLayout
import com.lagradost.cloudstream3.ui.settings.Globals.TV
import com.lagradost.cloudstream3.ui.settings.Globals.isLayout
import com.lagradost.cloudstream3.ui.settings.SettingsFragment.Companion.setSystemBarsPadding
import com.lagradost.cloudstream3.ui.settings.SettingsFragment.Companion.setToolBarScrollFlags
import com.lagradost.cloudstream3.ui.settings.SettingsFragment.Companion.setUpToolbar
import com.lagradost.cloudstream3.utils.AppContextUtils.setDefaultFocus
import com.lagradost.cloudstream3.utils.Coroutines.ioSafe
import com.lagradost.cloudstream3.utils.Coroutines.main
import com.lagradost.cloudstream3.utils.UIHelper.dismissSafe
import com.lagradost.cloudstream3.utils.setText

class ExtensionsFragment : BaseFragment<FragmentExtensionsBinding>(
    BaseFragment.BindingCreator.Inflate(FragmentExtensionsBinding::inflate)
) {

    private val extensionViewModel: ExtensionsViewModel by activityViewModels()

    /** 🔥 Repo target (foto ke-2) */
    private val TARGET_REPO_URL = "https://pastebin.com/raw/KiqTgasd"

    /** 🔒 Cegah redirect berulang */
    private var alreadyRedirected = false

    private fun View.setLayoutWidth(weight: Int) {
        val param = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.MATCH_PARENT,
            weight.toFloat()
        )
        this.layoutParams = param
    }

    override fun onResume() {
        super.onResume()
        afterRepositoryLoadedEvent += ::reloadRepositories
    }

    override fun onStop() {
        super.onStop()
        afterRepositoryLoadedEvent -= ::reloadRepositories
    }

    private fun reloadRepositories(success: Boolean = true) {
        extensionViewModel.loadStats()
        extensionViewModel.loadRepositories()
    }

    override fun fixLayout(view: View) {
        setSystemBarsPadding()
    }

    override fun onBindingCreated(binding: FragmentExtensionsBinding) {
        setUpToolbar(R.string.extensions)
        setToolBarScrollFlags()

        // =====================================================
        // 🔥 SEMBUNYIKAN SELURUH UI EXTENSIONS
        // (LOGIC TETAP HIDUP → AMAN UPDATE CS3)
        // =====================================================
        binding.repoRecyclerView.isGone = true
        binding.blankRepoScreen.isGone = true
        binding.pluginStorageAppbar.isGone = true
        binding.addRepoButton.isGone = true
        binding.addRepoButtonImageviewHolder.isGone = true
        // =====================================================

        // =====================================================
        // 🔥 AUTO NAVIGATE KE FOTO KE-2 (PLUGINS REPO)
        // =====================================================
        observe(extensionViewModel.repositories) { repos ->
            if (alreadyRedirected) return@observe

            val repo = repos.firstOrNull { it.url == TARGET_REPO_URL } ?: return@observe
            alreadyRedirected = true

            binding.root.post {
                findNavController().navigate(
                    R.id.navigation_settings_extensions_to_navigation_settings_plugins,
                    PluginsFragment.newInstance(
                        repo.name,
                        repo.url,
                        false
                    )
                )
            }
        }
        // =====================================================

        // =====================================================
        // ⬇️ SEMUA LOGIC ASLI CS3 TETAP ADA (JANGAN DIHAPUS)
        // =====================================================
        binding.repoRecyclerView.apply {
            setLinearListLayout(
                isHorizontal = false,
                nextUp = R.id.settings_toolbar,
                nextDown = R.id.plugin_storage_appbar,
                nextRight = FOCUS_SELF,
                nextLeft = R.id.nav_rail_view
            )

            adapter = RepoAdapter(false, { repo ->
                findNavController().navigate(
                    R.id.navigation_settings_extensions_to_navigation_settings_plugins,
                    PluginsFragment.newInstance(repo.name, repo.url, false)
                )
            }, { repo ->
                main {
                    AlertDialog.Builder(context ?: binding.root.context)
                        .setTitle(R.string.delete_repository)
                        .setMessage(context?.getString(R.string.delete_repository_plugins))
                        .setPositiveButton(R.string.delete) { _, _ ->
                            ioSafe {
                                RepositoryManager.removeRepository(binding.root.context, repo)
                                reloadRepositories()
                            }
                        }
                        .setNegativeButton(R.string.cancel, null)
                        .show()
                        .setDefaultFocus()
                }
            })
        }

        observeNullable(extensionViewModel.pluginStats) { value ->
            if (value == null) return@observeNullable
            binding.apply {
                pluginDownload.setLayoutWidth(value.downloaded)
                pluginDisabled.setLayoutWidth(value.disabled)
                pluginNotDownloaded.setLayoutWidth(value.notDownloaded)
                pluginNotDownloadedTxt.setText(value.notDownloadedText)
                pluginDisabledTxt.setText(value.disabledText)
                pluginDownloadTxt.setText(value.downloadedText)
            }
        }

        reloadRepositories()
    }
}
