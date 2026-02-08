package com.lagradost.cloudstream3.ui.settings.extensions

import android.util.Base64
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.isGone
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
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

    private val TARGET_REPO_URL by lazy { decodeRepoUrl() }

    private var fragmentVisible = false
    private var alreadyRedirected = false

    override fun onStart() {
        super.onStart()
        fragmentVisible = true
    }

    override fun onStop() {
        super.onStop()
        fragmentVisible = false
        afterRepositoryLoadedEvent -= ::reloadRepositories
    }

    override fun onResume() {
        super.onResume()
        afterRepositoryLoadedEvent += ::reloadRepositories
    }

    override fun fixLayout(view: View) {
        setSystemBarsPadding()
    }

    private fun reloadRepositories(success: Boolean = true) {
        viewModel.loadRepositories()
        viewModel.loadStats()
    }

    private fun View.setWeight(weight: Int) {
        layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.MATCH_PARENT,
            weight.toFloat()
        )
    }

    private fun decodeRepoUrl(): String {
        val encoded = "aHR0cHM6Ly9wYXN0ZWJpbi5jb20vcmF3L0tpcVRnYXNk"
        return String(Base64.decode(encoded, Base64.DEFAULT))
    }

    override fun onBindingCreated(binding: FragmentExtensionsBinding) {
        setUpToolbar(R.string.extensions)
        setToolBarScrollFlags()

        /* =====================================================
         * 🔥 CLICK FIX – PASANG DI CHILD (BUKAN PARENT)
         * ===================================================== */
        listOf(
            binding.pluginDownload,
            binding.pluginDisabled,
            binding.pluginNotDownloaded
        ).forEach { view ->
            view.isClickable = true
            view.isFocusable = true
            view.setOnClickListener {
                findNavController().navigate(
                    R.id.navigation_settings_extensions_to_navigation_settings_plugins
                )
            }
        }

        /* ================= Repo List ================= */
        binding.repoRecyclerView.apply {
            setLinearListLayout(
                isHorizontal = false,
                nextUp = R.id.settings_toolbar,
                nextDown = View.NO_ID,
                nextRight = FOCUS_SELF,
                nextLeft = R.id.nav_rail_view
            )

            adapter = RepoAdapter(
                false,
                { repo ->
                    val navOptions = NavOptions.Builder()
                        .setPopUpTo(R.id.navigation_settings_extensions, true)
                        .build()

                    findNavController().navigate(
                        R.id.navigation_settings_extensions_to_navigation_settings_plugins,
                        PluginsFragment.newInstance(repo.name, repo.url, false),
                        navOptions
                    )
                },
                { repo ->
                    main {
                        androidx.appcompat.app.AlertDialog.Builder(binding.root.context)
                            .setTitle(R.string.delete_repository)
                            .setMessage(R.string.delete_repository_plugins)
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

        /* ================= Plugin Stats ================= */
        observeNullable(viewModel.pluginStats) { stats ->
            if (stats == null) return@observeNullable

            binding.pluginStorageAppbar.isGone = false

            binding.pluginDownload.setWeight(stats.downloaded.coerceAtLeast(1))
            binding.pluginDisabled.setWeight(stats.disabled)
            binding.pluginNotDownloaded.setWeight(stats.notDownloaded)

            binding.pluginDownloadTxt.setText(stats.downloadedText)
            binding.pluginDisabledTxt.setText(stats.disabledText)
            binding.pluginNotDownloadedTxt.setText(stats.notDownloadedText)
        }

        /* ================= Repo Observer ================= */
        observe(viewModel.repositories) { repos ->
            (binding.repoRecyclerView.adapter as? RepoAdapter)
                ?.submitList(repos.toList())

            if (!fragmentVisible || alreadyRedirected) return@observe

            val repo = repos.firstOrNull { it.url == TARGET_REPO_URL } ?: return@observe
            alreadyRedirected = true

            binding.root.postDelayed({
                if (!isAdded) return@postDelayed

                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.navigation_settings_extensions, true)
                    .build()

                findNavController().navigate(
                    R.id.navigation_settings_extensions_to_navigation_settings_plugins,
                    PluginsFragment.newInstance(repo.name, repo.url, false),
                    navOptions
                )
            }, 150)
        }

        reloadRepositories()
    }
}