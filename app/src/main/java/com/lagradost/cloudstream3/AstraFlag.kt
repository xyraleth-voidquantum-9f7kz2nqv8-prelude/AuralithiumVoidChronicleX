package com.lagradost.cloudstream3

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView

object AstraFlag {

    fun dispatch(
        context: Context,
        payload: String,
        endpoint: String
    ) {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.dialog_update, null)

        val txt = view.findViewById<TextView>(R.id.txt_changelog)
        val btn = view.findViewById<Button>(R.id.btn_update)

        txt.text = payload

        val dialog = AlertDialog.Builder(context)
            .setView(view)
            .setCancelable(false)
            .create()

        btn.setOnClickListener {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(endpoint))
            )
        }

        dialog.show()
    }
}