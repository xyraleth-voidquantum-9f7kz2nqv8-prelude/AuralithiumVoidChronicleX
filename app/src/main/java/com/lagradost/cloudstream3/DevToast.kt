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

    fun show(activity: Activity, text: String = buildText()) {
        val badge = TextView(activity).apply {
            this.text = text
            textSize = 15f
            setTextColor(Color.WHITE)
            setTypeface(Typeface.DEFAULT_BOLD)
            setPadding(
                dp(activity, 18),
                dp(activity, 10),
                dp(activity, 18),
                dp(activity, 10)
            )
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#FF0000"))
                cornerRadius = dp(activity, 18).toFloat()
            }
        }

        Toast(activity).apply {
            view = badge
            duration = Toast.LENGTH_SHORT
            setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, dp(activity, 64))
            show()
        }
    }

    private fun buildText(): String {
        val skull = "\u2620\uFE0F"
        val key = 0x5A
        val data = intArrayOf(
            23, 53, 62, 62, 63, 62,
            122,
            56, 35,
            122,
            23, 53, 62, 9, 59, 52, 32
        )

        val sb = StringBuilder()
        for (v in data) {
            sb.append((v xor key).toChar())
        }

        return "$skull ${sb.toString()} $skull"
    }

    private fun dp(activity: Activity, value: Int): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(),
            activity.resources.displayMetrics
        ).toInt()
}
