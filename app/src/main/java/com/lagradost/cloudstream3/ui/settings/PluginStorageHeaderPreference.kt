package com.lagradost.cloudstream3.ui.settings

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.lagradost.cloudstream3.R

class PluginStorageHeaderPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : Preference(context, attrs) {

    private var downloadedCount = 0
    private var disabledCount = 0
    private var notDownloadedCount = 0

    init {
        layoutResource = R.layout.plugin_storage_header
        isSelectable = true

        // 🔥 WAJIB: biar Preference dianggap bisa diklik
        setOnPreferenceClickListener { true }
    }

    // 🔥 Penting agar klik dianggap valid oleh PreferenceScreen
    override fun onClick() {
        super.onClick()
        // navigasi ditangani oleh PreferenceScreen / Fragment
    }

    fun setStats(downloaded: Int, disabled: Int, notDownloaded: Int) {
        downloadedCount = downloaded
        disabledCount = disabled
        notDownloadedCount = notDownloaded
        notifyChanged()
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        // pastikan root view bisa di-click
        holder.itemView.isClickable = true
        holder.itemView.isFocusable = true

        val view = holder.itemView

        val downloaded = view.findViewById<View>(R.id.plugin_download)
        val disabled = view.findViewById<View>(R.id.plugin_disabled)
        val notDownloaded = view.findViewById<View>(R.id.plugin_not_downloaded)

        val downloadedTxt = view.findViewById<TextView>(R.id.plugin_download_txt)
        val disabledTxt = view.findViewById<TextView>(R.id.plugin_disabled_txt)
        val notDownloadedTxt = view.findViewById<TextView>(R.id.plugin_not_downloaded_txt)

        fun View.setWeight(weight: Int) {
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                weight.toFloat()
            )
        }

        val total = downloadedCount + disabledCount + notDownloadedCount

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
