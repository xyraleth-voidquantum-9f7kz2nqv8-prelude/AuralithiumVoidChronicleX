package com.lagradost.cloudstream3

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView

object DevToast {

    fun show(activity: Activity, text: String = "☠️ Modded by ModSanz ☠️") {

        Handler(Looper.getMainLooper()).postDelayed({

            val root = activity.window.decorView as ViewGroup

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

            val params = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                bottomMargin = dp(activity, 48)
            }

            root.addView(badge, params)

            // anim masuk
            badge.translationY = dp(activity, 40).toFloat()
            badge.animate().translationY(0f).setDuration(350).start()

            // auto remove
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    root.removeView(badge)
                } catch (_: Exception) {}
            }, 2500)

        }, 300) // delay kecil biar window siap
    }

    private fun dp(activity: Activity, v: Int): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            v.toFloat(),
            activity.resources.displayMetrics
        ).toInt()
}
