package com.lagradost.cloudstream3.ui.settings

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.lagradost.cloudstream3.R
import com.lagradost.cloudstream3.plugins.Plugin
import com.lagradost.cloudstream3.plugins.RepositoryManager
import com.lagradost.cloudstream3.plugins.isDownloaded   // IMPORT UNTUK EXTENSION
import com.lagradost.cloudstream3.plugins.isEnabled      // IMPORT UNTUK EXTENSION

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

        // Ambil plugin asli dari RepositoryManager
        val plugins: List<Plugin> = RepositoryManager.plugins.values.toList()

        // HITUNG STATUS PLUGIN
        val downloadedCount = plugins.count { it.isDownloaded }
        val disabledCount = plugins.count { it.isDownloaded && !it.isEnabled }
        val notDownloadedCount = plugins.count { !it.isDownloaded }

        // Fungsi helper untuk set weight layout
        fun View.setWeight(weight: Int) {
            val param = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                weight.toFloat()
            )
            layoutParams = param
        }

        downloaded.setWeight(downloadedCount)
        disabled.setWeight(disabledCount)
        notDownloaded.setWeight(notDownloadedCount)

        downloadedTxt.text = "Downloaded: $downloadedCount"
        disabledTxt.text = "Disabled: $disabledCount"
        notDownloadedTxt.text = "Not downloaded: $notDownloadedCount"
    }
}