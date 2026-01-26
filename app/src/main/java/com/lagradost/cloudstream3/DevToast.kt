package com.lagradost.cloudstream3

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView

object DevToast {

    fun show(activity: Activity, text: String = "☠️ Modded by ModSanz ☠️") {
        val decor = activity.window.decorView as FrameLayout

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
            alpha = 1f
        }

        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        ).apply {
            bottomMargin = dp(activity, 24)
        }

        decor.addView(badge, params)

        // anim masuk
        badge.translationY = dp(activity, 40).toFloat()
        badge.animate().translationY(0f).setDuration(350).start()

        // auto hide
        Handler(Looper.getMainLooper()).postDelayed({
            badge.animate()
                .translationY(dp(activity, 40).toFloat())
                .setDuration(350)
                .withEndAction { decor.removeView(badge) }
                .start()
        }, 2500)
    }

    private fun dp(activity: Activity, v: Int): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            v.toFloat(),
            activity.resources.displayMetrics
        ).toInt()
}