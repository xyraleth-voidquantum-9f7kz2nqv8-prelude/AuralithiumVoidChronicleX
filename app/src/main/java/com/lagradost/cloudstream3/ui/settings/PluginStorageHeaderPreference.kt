package com.lagradost.cloudstream3.ui.settings

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.lagradost.cloudstream3.R
import com.lagradost.cloudstream3.plugins.RepositoryManager

class PluginStorageHeaderPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : Preference(context, attrs) {

    init {
        layoutResource = R.layout.plugin_storage_header
        isSelectable = false
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        val view = holder.itemView

        val downloaded = view.findViewById<View>(R.id.plugin_download)
        val disabled = view.findViewById<View>(R.id.plugin_disabled)
        val notDownloaded = view.findViewById<View>(R.id.plugin_not_downloaded)

        val downloadedTxt = view.findViewById<TextView>(R.id.plugin_download_txt)
        val disabledTxt = view.findViewById<TextView>(R.id.plugin_disabled_txt)
        val notDownloadedTxt = view.findViewById<TextView>(R.id.plugin_not_downloaded_txt)

        // ✅ API STABIL (AMAN SEMUA BRANCH)
        val plugins = RepositoryManager.plugins.values

        val downloadedCount = plugins.count { it.isInstalled && it.isEnabled }
        val disabledCount = plugins.count { it.isInstalled && !it.isEnabled }
        val notDownloadedCount = plugins.count { !it.isInstalled }

        val total = plugins.size

        fun View.setWeight(weight: Int) {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                weight.toFloat()
            )
        }

        // 🔥 FIX: jangan sampai semua bar 0
        if (total == 0) {
            downloaded.setWeight(1)
            disabled.setWeight(0)
            notDownloaded.setWeight(0)
        } else {
            downloaded.setWeight(downloadedCount)
            disabled.setWeight(disabledCount)
            notDownloaded.setWeight(notDownloadedCount)
        }

        downloadedTxt.text =
            context.getString(R.string.plugin_downloaded_format, downloadedCount)
        disabledTxt.text =
            context.getString(R.string.plugin_disabled_format, disabledCount)
        notDownloadedTxt.text =
            context.getString(R.string.plugin_not_downloaded_format, notDownloadedCount)
    }
}