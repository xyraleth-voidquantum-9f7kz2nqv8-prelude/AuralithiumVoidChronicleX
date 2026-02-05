package com.lagradost.cloudstream3.ui.settings.extensions

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
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
import com.lagradost.cloudstream3.plugins.RepositoryManager.RepositoryData
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
        val p1 = "aHR0cHM6"
        val p2 = "Ly9wYXN0"
        val p3 = "ZWJpbi5j"
        val p4 = "b20vcmF3"
        val p5 = "L0tpcVRn"
        val p6 = "YXNk"
        val encoded = p1 + p2 + p3 + p4 + p5 + p6
        val decoded = String(android.util.Base64.decode(encoded, android.util.Base64.DEFAULT))
        val key = 0x12
        return decoded.map { (it.code xor key).toChar() }
            .map { (it.code xor key).toChar() }
            .joinToString("")
    }

    private fun showToast(message: String) {
        Toast.makeText(context ?: return, message, Toast.LENGTH_SHORT).show()
    }

    private fun showToast(resId: Int) {
        Toast.makeText(context ?: return, resId, Toast.LENGTH_SHORT).show()
    }

    override fun onBindingCreated(binding: FragmentExtensionsBinding) {
        binding.root.isGone = true

        // Redirect ke plugin jika target repo ada
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

        // Update stats UI
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

        // Recycler view repo
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
                                    RepositoryManager.removeRepository(binding.root.context, repo.toRepositoryData())
                                    reloadRepositories()
                                }
                            }
                            .setNegativeButton(R.string.cancel, null)
                            .show()
                    }
                }
            )
        }

        // Tombol update plugin → langsung load .cs3
        binding.pluginStorageAppbar.setOnClickListener {
            ioSafe {
                PluginManager.loadPlugins(activity ?: return@ioSafe)
            }
        }

        // Tambah repository
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
                    val urlStr = bindingDialog.repoUrlInput.text?.toString()
                        ?.let { RepositoryManager.parseRepoUrl(it) }
                    if (urlStr.isNullOrBlank()) {
                        main { showToast(R.string.error_invalid_data) }
                        return@ioSafe
                    }
                    val repo = RepositoryManager.parseRepository(urlStr)
                    if (repo == null) {
                        main { showToast(R.string.no_repository_found_error) }
                        return@ioSafe
                    }
                    val fixedRepo = repo.copy(name = name?.takeIf { it.isNotBlank() } ?: repo.name)
                    RepositoryManager.addRepository(fixedRepo.toRepositoryData())
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
