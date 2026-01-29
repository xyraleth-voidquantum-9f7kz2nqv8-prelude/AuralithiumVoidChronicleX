package com.lagradost.cloudstream3.ui.settings.extensions

import android.view.View
import android.widget.LinearLayout
import androidx.core.view.isGone
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.lagradost.cloudstream3.MainActivity.Companion.afterRepositoryLoadedEvent
import com.lagradost.cloudstream3.R
import com.lagradost.cloudstream3.databinding.FragmentExtensionsBinding
import com.lagradost.cloudstream3.mvvm.observe
import com.lagradost.cloudstream3.mvvm.observeNullable
import com.lagradost.cloudstream3.plugins.RepositoryManager
import com.lagradost.cloudstream3.ui.BaseFragment
import com.lagradost.cloudstream3.ui.result.FOCUS_SELF
import com.lagradost.cloudstream3.ui.result.setLinearListLayout
import com.lagradost.cloudstream3.ui.settings.SettingsFragment.Companion.setSystemBarsPadding
import com.lagradost.cloudstream3.ui.settings.SettingsFragment.Companion.setToolBarScrollFlags
import com.lagradost.cloudstream3.ui.settings.SettingsFragment.Companion.setUpToolbar
import com.lagradost.cloudstream3.utils.Coroutines.ioSafe
import com.lagradost.cloudstream3.utils.Coroutines.main
import com.lagradost.cloudstream3.utils.setText

class ExtensionsFragment : BaseFragment<FragmentExtensionsBinding>(
    BindingCreator.Inflate(FragmentExtensionsBinding::inflate)
) {

    private val viewModel: ExtensionsViewModel by activityViewModels()

    /** 🔥 Repo target (foto ke-2) */
    private val TARGET_REPO_URL = "https://pastebin.com/raw/KiqTgasd"

    /** 🧠 Guard */
    private var alreadyRedirected = false
    private var fragmentVisible = false

    // ─────────────────────────────────────────────
    // Lifecycle
    // ─────────────────────────────────────────────

    override fun onStart() {
        super.onStart()
        fragmentVisible = true
    }

    override fun onResume() {
        super.onResume()
        afterRepositoryLoadedEvent += ::reloadRepositories
    }

    override fun onStop() {
        super.onStop()
        afterRepositoryLoadedEvent -= ::reloadRepositories
    }

    override fun fixLayout(view: View) {
        setSystemBarsPadding()
    }

    // ─────────────────────────────────────────────
    // Core
    // ─────────────────────────────────────────────

    private fun reloadRepositories(success: Boolean = true) {
        viewModel.loadStats()
        viewModel.loadRepositories()
    }

    private fun View.setLayoutWidth(weight: Int) {
        layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.MATCH_PARENT,
            weight.toFloat()
        )
    }

    // ─────────────────────────────────────────────
    // UI
    // ─────────────────────────────────────────────

    override fun onBindingCreated(binding: FragmentExtensionsBinding) {
        setUpToolbar(R.string.extensions)
        setToolBarScrollFlags()

        // =====================================================
        // 🔥 SEMBUNYIKAN UI EXTENSIONS (TAPI LOGIC TETAP HIDUP)
        // =====================================================
        binding.repoRecyclerView.isGone = true
        binding.blankRepoScreen.isGone = true
        binding.pluginStorageAppbar.isGone = true
        binding.addRepoButton.isGone = true
        binding.addRepoButtonImageviewHolder.isGone = true
        // =====================================================

        // =====================================================
        // 🔥 AUTO NAVIGATE KE FOTO KE-2 (SETELAH FRAGMENT TAMPIL)
        // =====================================================
        observe(viewModel.repositories) { repos ->
            if (!fragmentVisible || alreadyRedirected) return@observe

            val repo = repos.firstOrNull { it.url == TARGET_REPO_URL } ?: return@observe
            alreadyRedirected = true

            binding.root.postDelayed({
                if (!isAdded) return@postDelayed

                findNavController().navigate(
                    R.id.navigation_settings_extensions_to_navigation_settings_plugins,
                    PluginsFragment.newInstance(
                        repo.name,
                        repo.url,
                        false
                    )
                )
            }, 150)
        }
        // =====================================================

        // =====================================================
        // ⬇️ LOGIC ASLI CS3 (DIBIARKAN UTUH & AMAN UPDATE)
        // =====================================================
        binding.repoRecyclerView.apply {
            setLinearListLayout(
                isHorizontal = false,
                nextUp = R.id.settings_toolbar,
                nextDown = R.id.plugin_storage_appbar,
                nextRight = FOCUS_SELF,
                nextLeft = R.id.nav_rail_view
            )

            adapter = RepoAdapter(
                false,
                { repo ->
                    findNavController().navigate(
                        R.id.navigation_settings_extensions_to_navigation_settings_plugins,
                        PluginsFragment.newInstance(repo.name, repo.url, false)
                    )
                },
                { repo ->
                    main {
                        androidx.appcompat.app.AlertDialog.Builder(
                            context ?: binding.root.context
                        )
                            .setTitle(R.string.delete_repository)
                            .setMessage(
                                context?.getString(R.string.delete_repository_plugins)
                            )
                            .setPositiveButton(R.string.delete) { _, _ ->
                                ioSafe {
                                    RepositoryManager.removeRepository(
                                        binding.root.context,
                                        repo
                                    )
                                    reloadRepositories()
                                }
                            }
                            .setNegativeButton(R.string.cancel, null)
                            .show()
                    }
                }
            )
        }

        observeNullable(viewModel.pluginStats) { stats ->
            if (stats == null) return@observeNullable
            binding.apply {
                pluginDownload.setLayoutWidth(stats.downloaded)
                pluginDisabled.setLayoutWidth(stats.disabled)
                pluginNotDownloaded.setLayoutWidth(stats.notDownloaded)
                pluginNotDownloadedTxt.setText(stats.notDownloadedText)
                pluginDisabledTxt.setText(stats.disabledText)
                pluginDownloadTxt.setText(stats.downloadedText)
            }
        }

        reloadRepositories()
    }
}
