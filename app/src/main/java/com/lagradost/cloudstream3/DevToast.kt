package com.lagradost.cloudstream3

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView

object DevToast {

    fun show(activity: Activity, text: String = "☠️ Modded by ModSanz ☠️") {

        val dialog = Dialog(activity, android.R.style.Theme_Translucent_NoTitleBar)

        val badge = TextView(activity).apply {
            this.text = text
            textSize = 15f
            setTextColor(Color.WHITE)
            setTypeface(Typeface.DEFAULT_BOLD)
            setPadding(dp(activity, 18), dp(activity, 10), dp(activity, 18), dp(activity, 10))
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#1A1A1A"))
                cornerRadius = dp(activity, 18).toFloat()
            }
            elevation = dp(activity, 20).toFloat()
        }

        val container = FrameLayout(activity).apply {
            addView(
                badge,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                ).apply {
                    bottomMargin = dp(activity, 48)
                }
            )
        }

        dialog.setContentView(container)
        dialog.setCancelable(false)

        dialog.window?.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND) // ❌ NO BLUR
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
        }

        dialog.show()

        // auto close
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                dialog.dismiss()
            } catch (_: Exception) {}
        }, 2500)
    }

    private fun dp(activity: Activity, v: Int): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            v.toFloat(),
            activity.resources.displayMetrics
        ).toInt()
}
