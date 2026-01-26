package com.lagradost.cloudstream3

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView

object DevToast {

    fun show(activity: Activity, text: String = "☠️ Modded by ModSanz ☠️") {
        val wm = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager

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

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_PANEL
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            y = dp(activity, 48)
        }

        wm.addView(badge, params)

        // auto remove
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                wm.removeView(badge)
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
