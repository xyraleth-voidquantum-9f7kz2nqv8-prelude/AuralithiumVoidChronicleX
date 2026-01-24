package com.lagradost.cloudstream3

import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.*
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.widget.*
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.system.exitProcess

object DialogAdmin {

    private const val JSON_URL =
        "https://cloudplaypaneladmin-default-rtdb.asia-southeast1.firebasedatabase.app/keys.json"

    private const val ADMIN_URL = "https://t.me/dp_mods"
    private const val AUTO_CLOSE_DELAY = 4000L

    private const val TITLE = "VERIFIKASI PERANGKAT"
    private const val SUBTITLE = "Proses Pendaftaran:"

    private const val STATUS_PENDING =
        "⏳ Status : Sedang memeriksa status verifikasi perangkat Anda..."
    private const val STATUS_OK =
        "✅ Status : Perangkat berhasil diverifikasi. Akses diberikan."
    private const val STATUS_UNLIMITED =
        "🛡️ Status : Perangkat memiliki akses penuh (UNLIMITED)."
    private const val STATUS_FAIL =
        "❌ Status : Perangkat belum terdaftar. Hubungi Admin."
    private const val STATUS_NET =
        "⚠️ Status : Terjadi kesalahan jaringan. Coba lagi."
    private const val STATUS_MAINTENANCE =
        "🛑 APLIKASI SEDANG MAINTENANCE\n\nAplikasi akan ditutup otomatis."

    private val BG = Color.BLACK
    private val PURPLE = Color.parseColor("#C77DFF")
    private val GREY = Color.parseColor("#EDEDED")
    private val GREEN = Color.parseColor("#2E7D32")
    private val BLUE = Color.parseColor("#D3D3D3") // Light gray untuk Unlimited
    private val DARK_PURPLE = Color.parseColor("#6A1B9A")
    private val YELLOW = Color.parseColor("#FFC107")
    private val RED = Color.parseColor("#FF4444")
    private val BRIGHT_RED = Color.parseColor("#FF0000")
    private val GRAY = Color.DKGRAY

    private var shown = false

    fun show(context: Context, onVerified: () -> Unit) {
        if (shown) return
        shown = true

        val dialog = AlertDialog.Builder(context).create()
        val deviceId = getDeviceId(context)

        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(context, 16), dp(context, 16), dp(context, 16), dp(context, 16))
            setBackgroundColor(BG)
        }

        val statusBox = TextView(context).apply {
            text = STATUS_PENDING
            textSize = 12f
            setTextColor(GRAY)
            setPadding(dp(context, 12), dp(context, 8), dp(context, 12), dp(context, 8))
            background = rounded(GREY, 10, context)
            startAnimation(AlphaAnimation(0.3f, 1f).apply {
                duration = 700
                repeatMode = AlphaAnimation.REVERSE
                repeatCount = AlphaAnimation.INFINITE
            })
        }
        root.addView(statusBox)

        space(root, context, 12)

        root.addView(TextView(context).apply {
            text = TITLE
            setTextColor(Color.WHITE)
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
        })

        space(root, context, 10)

        root.addView(TextView(context).apply {
            text = SUBTITLE
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
        })

        space(root, context, 6)

        val infoBox = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(context, 12), dp(context, 10), dp(context, 12), dp(context, 10))
            background = rounded(Color.parseColor("#DDDDDD"), 12, context)
        }

        listOf(
            "‣ Ketuk ADMIN untuk menghubungi pengembang.",
            "‣ Kirim ID Perangkat Anda ke admin.",
            "‣ Tunggu hingga perangkat Anda terdaftar.",
            "‣ Tutup aplikasi & buka kembali setelah admin memberi ACC."
        ).forEach {
            infoBox.addView(TextView(context).apply {
                text = it
                textSize = 12f
                setTextColor(Color.DKGRAY)
            })
        }

        root.addView(infoBox)
        space(root, context, 12)

        root.addView(TextView(context).apply {
            text = "Device ID:"
            setTextColor(Color.WHITE)
            textSize = 14f
        })

        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val idBox = TextView(context).apply {
            text = deviceId
            textSize = 14f
            setTextColor(Color.BLACK)
            setPadding(dp(context, 8), dp(context, 8), dp(context, 8), dp(context, 8))
            background = rounded(Color.WHITE, 10, context)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        row.addView(idBox)

        val adminBtn = actionButton(context, "ADMIN", 55) {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, android.net.Uri.parse(ADMIN_URL))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
        val adminParams = LinearLayout.LayoutParams(dp(context, 55), LinearLayout.LayoutParams.WRAP_CONTENT)
        adminParams.marginStart = dp(context, 6)
        adminBtn.layoutParams = adminParams
        row.addView(adminBtn)

        val copyBtn = actionButton(context, "SALIN ID", 55) {
            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("Device ID", deviceId))
            Toast.makeText(context, "ID tersalin", Toast.LENGTH_SHORT).show()
        }
        val copyParams = LinearLayout.LayoutParams(dp(context, 55), LinearLayout.LayoutParams.WRAP_CONTENT)
        copyParams.marginStart = dp(context, 6)
        copyBtn.layoutParams = copyParams
        row.addView(copyBtn)

        root.addView(row)

        dialog.setView(root)
        dialog.setCancelable(false)
        dialog.show()

        CoroutineScope(Dispatchers.Main).launch {
            delay(2000)
            val result = withContext(Dispatchers.IO) { checkStatus(deviceId) }
            when (result) {
                Status.MAINTENANCE -> { dialog.dismiss(); showMaintenanceLock(context) }
                Status.UNLIMITED -> { statusBox.clearAnimation(); statusBox.text = STATUS_UNLIMITED; statusBox.setTextColor(BLUE); autoClose(dialog, onVerified) }
                Status.OK -> { statusBox.clearAnimation(); statusBox.text = STATUS_OK; statusBox.setTextColor(GREEN); autoClose(dialog, onVerified) }
                Status.NOT_FOUND -> { statusBox.clearAnimation(); statusBox.text = STATUS_FAIL; statusBox.setTextColor(RED) }
                Status.NETWORK -> { statusBox.clearAnimation(); statusBox.text = STATUS_NET; statusBox.setTextColor(YELLOW) }
            }
        }
    }

    private fun showMaintenanceLock(context: Context) {
        val dialog = AlertDialog.Builder(context).create()
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(BG)
        }
        root.addView(TextView(context).apply {
            text = STATUS_MAINTENANCE
            setTextColor(BRIGHT_RED)
            textSize = 16f
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
        })
        dialog.setView(root)
        dialog.setCancelable(false)
        dialog.window?.apply {
            setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
            setBackgroundDrawableResource(android.R.color.black)
        }
        dialog.show()
        Handler(Looper.getMainLooper()).postDelayed({
            if (context is Activity) context.finishAffinity()
            android.os.Process.killProcess(android.os.Process.myPid())
            exitProcess(0)
        }, 2500)
    }

    private fun autoClose(d: AlertDialog, onVerified: () -> Unit) {
        Handler(Looper.getMainLooper()).postDelayed({
            d.dismiss()
            onVerified()
        }, AUTO_CLOSE_DELAY)
    }

    private fun getDeviceId(c: Context): String =
        Settings.Secure.getString(c.contentResolver, Settings.Secure.ANDROID_ID)
            ?.take(16) ?: UUID.randomUUID().toString().take(16)

    private fun checkStatus(id: String): Status = try {
        val conn = URL(JSON_URL).openConnection() as HttpURLConnection
        val json = JSONObject(conn.inputStream.bufferedReader().readText())
        when {
            json.has("MAINTENANCE") && json.getJSONObject("MAINTENANCE").optBoolean("enabled", false) ->
                Status.MAINTENANCE
            json.has(id) && json.getJSONObject(id).optBoolean("limit", false) ->
                Status.UNLIMITED
            json.has(id) && !json.getJSONObject(id).optBoolean("limit", false) ->
                Status.OK
            else -> Status.NOT_FOUND
        }
    } catch (_: Exception) {
        Status.NETWORK
    }

    private fun actionButton(c: Context, text: String, widthDp: Int, click: () -> Unit) =
        Button(c).apply {
            this.text = text
            textSize = 12f
            setTextColor(Color.WHITE)
            setPadding(dp(c, 12), dp(c, 10), dp(c, 12), dp(c, 10))
            background = rounded(PURPLE, 14, c)
            layoutParams = LinearLayout.LayoutParams(dp(c, widthDp), LinearLayout.LayoutParams.WRAP_CONTENT)
            setOnTouchListener { v, e ->
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> { v.scaleX = 0.95f; v.scaleY = 0.95f }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { v.scaleX = 1f; v.scaleY = 1f }
                }
                false
            }
            setOnClickListener { click() }
        }

    private fun rounded(color: Int, radius: Int, c: Context) =
        GradientDrawable().apply { setColor(color); cornerRadius = dp(c, radius).toFloat() }

    private fun space(parent: LinearLayout, c: Context, dp: Int) {
        parent.addView(Space(c).apply { minimumHeight = dp(c, dp) })
    }

    private fun dp(c: Context, v: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), c.resources.displayMetrics).toInt()

    enum class Status { OK, UNLIMITED, NOT_FOUND, MAINTENANCE, NETWORK }
}