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

    // ====== TETAP ADA (WALAUPUN SEBAGIAN TIDAK DIPAKAI) ======
    private val TARGET_REPO_URL by lazy { NvKl() }
    private val TARGET_REPO_NAME by lazy { buildRepoName() }

    override fun onStart() {
        super.onStart()
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

    // ====== TIDAK DIUBAH ======
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

    // ====== FIX BASE64 REPO URL ======
    private const val URL_A1 = "aHR0cHM6Ly9wYXN0"
    private const val URL_A2 = "ZWJpbi5jb20vcmF3"
    private const val URL_A3 = "L0tpcVRnYXNk"

    private fun NvKl(): String {
        val combined = URL_A1 + URL_A2 + URL_A3
        return String(Base64.decode(combined, Base64.DEFAULT))
    }

    override fun onBindingCreated(binding: FragmentExtensionsBinding) {
        setUpToolbar(R.string.extensions)
        setToolBarScrollFlags()

        // ===============================
        // ✅ FIX: BAR HARUS BISA DI KLIK
        // ===============================
        binding.pluginStorageAppbar.isClickable = true
        binding.pluginStorageAppbar.isFocusable = true
        binding.pluginStorageAppbar.setOnClickListener {
            findNavController().navigate(
                R.id.navigation_settings_extensions_to_navigation_settings_plugins
            )
        }

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

        // ===============================
        // 🔥 FIX INTI: JANGAN GONE SAAT NULL
        // ===============================
        observeNullable(viewModel.pluginStats) { stats ->
            binding.pluginStorageAppbar.isGone = false

            if (stats == null || stats.total == 0) {
                binding.pluginDownload.setLayoutWidth(1)
                binding.pluginDisabled.setLayoutWidth(0)
                binding.pluginNotDownloaded.setLayoutWidth(0)

                binding.pluginDownloadTxt.setText("0")
                binding.pluginDisabledTxt.setText("0")
                binding.pluginNotDownloadedTxt.setText("0")
                return@observeNullable
            }

            binding.pluginDownload.setLayoutWidth(stats.downloaded)
            binding.pluginDisabled.setLayoutWidth(stats.disabled)
            binding.pluginNotDownloaded.setLayoutWidth(stats.notDownloaded)

            binding.pluginDownloadTxt.setText(stats.downloadedText)
            binding.pluginDisabledTxt.setText(stats.disabledText)
            binding.pluginNotDownloadedTxt.setText(stats.notDownloadedText)
        }

        // === Repo observer ===
        observe(viewModel.repositories) { repos ->
            (binding.repoRecyclerView.adapter as? RepoAdapter)
                ?.submitList(repos.toList())

            // ❌ AUTO REDIRECT DIHAPUS — repo aman, tidak kedip
        }

        reloadRepositories()
    }
}
