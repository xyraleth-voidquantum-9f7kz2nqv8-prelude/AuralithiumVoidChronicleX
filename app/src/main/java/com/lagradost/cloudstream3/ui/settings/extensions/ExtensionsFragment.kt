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
    private val TARGET_REPO_NAME by lazy { buildRepoName() }

    private var alreadyRedirected = false
    private var fragmentVisible = false

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
        fragmentVisible = false
    }

    override fun fixLayout(view: View) {
        setSystemBarsPadding()
    }

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

    private fun buildRepoName(): String {
        val skull = "\u2620\uFE0F"
        val key = 0x5A
        val data = intArrayOf(
            23, 53, 62, 62, 63, 62,
            122,
            56, 35,
            122,
            23, 53, 62, 9, 59, 52, 32
        )
        val text = data.map { (it xor key).toChar() }.joinToString("")
        return "$skull$text$skull"
    }

    private fun decodeRepoUrl(): String {
        val encoded = "aHR0cHM6Ly9wYXN0ZWJpbi5jb20vcmF3L0tpcVRnYXNk"
        return String(Base64.decode(encoded, Base64.DEFAULT))
    }

    override fun onBindingCreated(binding: FragmentExtensionsBinding) {
        setUpToolbar(R.string.extensions)
        setToolBarScrollFlags()

        // === Repo list ===
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
                        androidx.appcompat.app.AlertDialog.Builder(
                            context ?: binding.root.context
                        )
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
                    }
                }
            )
        }

        // === Plugin stats bar (FIXED) ===
        observeNullable(viewModel.pluginStats) { stats ->
            if (stats == null) {
                binding.pluginStorageAppbar.isGone = true
                return@observeNullable
            }

            binding.pluginStorageAppbar.isGone = false

            if (stats.total == 0) {
                binding.pluginDownload.setLayoutWidth(1)
                binding.pluginDisabled.setLayoutWidth(0)
                binding.pluginNotDownloaded.setLayoutWidth(0)
            } else {
                binding.pluginDownload.setLayoutWidth(stats.downloaded)
                binding.pluginDisabled.setLayoutWidth(stats.disabled)
                binding.pluginNotDownloaded.setLayoutWidth(stats.notDownloaded)
            }

            binding.pluginDownloadTxt.setText(stats.downloadedText)
            binding.pluginDisabledTxt.setText(stats.disabledText)
            binding.pluginNotDownloadedTxt.setText(stats.notDownloadedText)
        }

        // === Auto redirect ke repo target ===
        observe(viewModel.repositories) { repos ->
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