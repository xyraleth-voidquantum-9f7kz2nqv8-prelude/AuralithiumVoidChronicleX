package com.lagradost.cloudstream3.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.google.android.material.snackbar.Snackbar

object ModSnackbar {

    private const val PREF_NAME = "mod_snackbar"
    private const val KEY_SHOWN = "shown"

    fun show(anchorView: View) {
        val context = anchorView.context
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        // ❌ Kalau sudah pernah muncul, stop
        if (prefs.getBoolean(KEY_SHOWN, false)) return

        val snackbar = Snackbar.make(anchorView, "", Snackbar.LENGTH_LONG)

        val layout = snackbar.view as Snackbar.SnackbarLayout
        layout.setPadding(0, 0, 0, 24)

        val custom = LayoutInflater.from(context)
            .inflate(R.layout.snackbar_mod, layout, false)

        layout.addView(custom, 0)
        snackbar.show()

        // ✅ Tandai sudah pernah tampil
        prefs.edit().putBoolean(KEY_SHOWN, true).apply()
    }
}