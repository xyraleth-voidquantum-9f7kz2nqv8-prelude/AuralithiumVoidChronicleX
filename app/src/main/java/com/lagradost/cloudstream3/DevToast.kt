package com.lagradost.cloudstream3

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast

object DevToast {

    fun show(activity: Activity, text: String = "☠️ Modded by ModSanz ☠️") {

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
        }

        val toast = Toast(activity)
        toast.view = badge
        toast.duration = Toast.LENGTH_SHORT
        toast.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, dp(activity, 64))
        toast.show()
    }

    private fun dp(activity: Activity, v: Int): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            v.toFloat(),
            activity.resources.displayMetrics
        ).toInt()
}
