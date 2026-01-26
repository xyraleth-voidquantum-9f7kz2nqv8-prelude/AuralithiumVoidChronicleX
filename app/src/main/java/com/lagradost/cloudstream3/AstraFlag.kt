package com.lagradost.cloudstream3

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView

object AstraFlag {

    fun dispatch(activity: Activity, changelog: String, apkUrl: String) {
        val view = LayoutInflater.from(activity)
            .inflate(R.layout.dialog_update, null)

        val txtChangelog = view.findViewById<TextView>(R.id.txt_changelog)
        val btnUpdate = view.findViewById<Button>(R.id.btn_update)

        txtChangelog.text = changelog

        val dialog = AlertDialog.Builder(activity)
            .setView(view)
            .setCancelable(false)
            .create()

        btnUpdate.setOnClickListener {
            activity.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(apkUrl))
            )
            dialog.dismiss()
        }

        dialog.show()
    }
}