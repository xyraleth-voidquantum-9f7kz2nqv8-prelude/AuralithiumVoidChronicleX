package com.lagradost.cloudstream3.ui.settings.extensions

import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.lagradost.cloudstream3.MainActivity.Companion.afterRepositoryLoadedEvent
import com.lagradost.cloudstream3.R
import com.lagradost.cloudstream3.databinding.FragmentExtensionsBinding
import com.lagradost.cloudstream3.databinding.AddRepoInputBinding
import com.lagradost.cloudstream3.mvvm.observe
import com.lagradost.cloudstream3.mvvm.observeNullable
import com.lagradost.cloudstream3.plugins.PluginManager
import com.lagradost.cloudstream3.plugins.RepositoryManager
import com.lagradost.cloudstream3.ui.BaseFragment
import com.lagradost.cloudstream3.ui.result.FOCUS_SELF
import com.lagradost.cloudstream3.ui.result.setLinearListLayout
import com.lagradost.cloudstream3.ui.settings.SettingsFragment.Companion.setSystemBarsPadding
import com.lagradost.cloudstream3.utils.Coroutines.ioSafe
import com.lagradost.cloudstream3.utils.Coroutines.main
import com.lagradost.cloudstream3.utils.UIHelper.dismissSafe
import com.lagradost.cloudstream3.utils.setText

class ExtensionsFragment : BaseFragment<FragmentExtensionsBinding>(
    BaseFragment.BindingCreator.Inflate(FragmentExtensionsBinding::inflate)
) {

    private val viewModel: ExtensionsViewModel by activityViewModels()
    private var fragmentVisible = false
    private var alreadyRedirected = false

    private val TARGET_REPO_URL by lazy { decodeRepoUrl() }
    private val TARGET_REPO_NAME by lazy { buildRepoName() }

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
        return "$skull${data.map { (it xor key).toChar() }.joinToString("")}$skull"
    }

    private fun decodeRepoUrl(): String {
        val encoded = "aHR0cHM6Ly9wYXN0ZWJpbi5jb20vcmF3L0tpcVRnYXNk"
        val decoded = String(Base64.decode(encoded, Base64.DEFAULT))
        val key = 0x12
        return decoded.map { (it.code xor key).toChar() }.map { (it.code xor key).toChar() }.joinToString("")
    }

    override fun onBindingCreated(binding: FragmentExtensionsBinding) {
        binding.root.isGone = true

        // =========================
        // Optional Auto-Redirect
        // =========================
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

        // =========================
        // Update Plugin Stats
        // =========================
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

        // =========================
        // RecyclerView Repo List
        // =========================
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
                        androidx.appcompat.app.AlertDialog.Builder(context ?: binding.root.context)
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

        // =========================
        // Plugin Storage Bar Click (Fix Pembaruan Plugins²)
        // =========================
        binding.pluginStorageAppbar.setOnClickListener {
            ioSafe {
                PluginManager.___DO_NOT_CALL_FROM_A_PLUGIN_manuallyReloadAndUpdatePlugins(activity ?: return@ioSafe)
            }
        }

        // =========================
        // Add Repository Dialog
        // =========================
        val addRepoClick = View.OnClickListener {
            val ctx = context ?: return@OnClickListener
            val bindingDialog = AddRepoInputBinding.inflate(LayoutInflater.from(ctx), null, false)
            val builder = androidx.appcompat.app.AlertDialog.Builder(ctx, R.style.AlertDialogCustom)
                .setView(bindingDialog.root)
            val dialog = builder.create()
            dialog.show()

            bindingDialog.applyBtt.setOnClickListener {
                val name = bindingDialog.repoNameInput.text?.toString()
                ioSafe {
                    val url = bindingDialog.repoUrlInput.text?.toString()?.let { RepositoryManager.parseRepoUrl(it) }
                    if (url.isNullOrBlank()) {
                        main { showToast(R.string.error_invalid_data) }
                        return@ioSafe
                    }
                    val repo = RepositoryManager.parseRepository(url)
                    if (repo == null) {
                        main { showToast(R.string.no_repository_found_error) }
                        return@ioSafe
                    }
                    val fixedName = name?.takeIf { it.isNotBlank() } ?: repo.name
                    RepositoryManager.addRepository(fixedName, url)
                    viewModel.loadStats()
                    viewModel.loadRepositories()
                }
                dialog.dismissSafe(activity)
            }
            bindingDialog.cancelBtt.setOnClickListener {
                dialog.dismissSafe(activity)
            }
        }

        binding.addRepoButton.setOnClickListener(addRepoClick)
        binding.addRepoButtonImageviewHolder.setOnClickListener(addRepoClick)

        reloadRepositories()
    }
}
