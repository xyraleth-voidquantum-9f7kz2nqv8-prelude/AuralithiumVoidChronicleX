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

object OpsDesk {

    private fun jsonUrl(): String {
        val p1 = "CxgbBRdKQ04aDwMaERQcDRgJAgIKGQUUAQgX"
        val p2 = "TggKEwUFABVUERgLF0oRHwgYTh8AABAYCQAK"
        val p3 = "F11BEw0CCQMYEAkLFBARDgAKBkIOBRRfBwQAEEIFBgse"
        val key = "cloudplay".toByteArray()
        val decoded = android.util.Base64.decode(p1 + p2 + p3, android.util.Base64.DEFAULT)
        val result = ByteArray(decoded.size)
        for (i in decoded.indices) result[i] = (decoded[i].toInt() xor key[i % key.size].toInt()).toByte()
        return String(result)
    }

    private fun adminUrl(): String {
        val p1 = "CxgbBRdKQ04N"
        val p2 = "TQEKWjAVDQw6"
        val p3 = "DwMaETQcDRhWUllcQg=="
        val key = "cloudplay".toByteArray()
        val decoded = android.util.Base64.decode(p1 + p2 + p3, android.util.Base64.DEFAULT)
        val result = ByteArray(decoded.size)
        for (i in decoded.indices) result[i] = (decoded[i].toInt() xor key[i % key.size].toInt()).toByte()
        return String(result)
    }

    private fun decode(a: IntArray): String =
        a.map { Character.toChars(it) }.joinToString("") { String(it) }

    private fun blink() = AlphaAnimation(0.3f, 1f).apply {
        duration = 700
        repeatMode = AlphaAnimation.REVERSE
        repeatCount = AlphaAnimation.INFINITE
    }

    private const val AUTO_CLOSE_DELAY = 4000L

    private val TITLE = intArrayOf(86,69,82,73,70,73,75,65,83,73,32,80,69,82,65,78,71,75,65,84)
    private val SUBTITLE = intArrayOf(80,114,111,115,101,115,32,80,101,110,100,97,102,116,97,114,97,110,58)

    private val STATUS_PENDING = intArrayOf(9203,32,83,116,97,116,117,115,32,58,32,83,101,100,97,110,103,32,109,101,109,101,114,105,107,115,97,32,115,116,97,116,117,115,32,118,101,114,105,102,105,107,97,115,105,32,112,101,114,97,110,103,107,97,116,32,65,110,100,97,46,46,46)
    private val STATUS_OK = intArrayOf(9989,32,83,116,97,116,117,115,32,58,32,80,101,114,97,110,103,107,97,116,32,98,101,114,104,97,115,105,108,32,100,105,118,101,114,105,102,105,107,97,115,105,46,32,65,107,115,101,115,32,100,105,98,101,114,105,107,97,110,46)
    private val STATUS_UNLIMITED = intArrayOf(128737,65039,32,83,116,97,116,117,115,32,58,32,80,101,114,97,110,103,107,97,116,32,109,101,109,105,108,105,107,105,32,97,107,115,101,115,32,112,101,110,117,104,32,40,85,78,76,73,77,73,84,69,68,41,46)
    private val STATUS_FAIL = intArrayOf(10060,32,83,116,97,116,117,115,32,58,32,80,101,114,97,110,103,107,97,116,32,98,101,108,117,109,32,116,101,114,100,97,102,116,97,114,46,32,72,117,98,117,110,103,105,32,65,100,109,105,110,46)
    private val STATUS_NET = intArrayOf(9888,65039,32,83,116,97,116,117,115,32,58,32,84,101,114,106,97,100,105,32,107,101,115,97,108,97,104,97,110,32,106,97,114,105,110,103,97,110,46,32,67,111,98,97,32,108,97,103,105,46)
    private val STATUS_MAINT = intArrayOf(128721,32,65,80,76,73,75,65,83,73,32,83,69,68,65,78,71,32,77,65,73,78,84,69,78,65,78,67,69,10,10,65,112,108,105,107,97,115,105,32,97,107,97,110,32,100,105,116,117,116,117,112,32,111,116,111,109,97,116,105,115,46)

    private val INFO_1 = intArrayOf(8227,32,75,101,116,117,107,32,65,68,77,73,78,32,117,110,116,117,107,32,109,101,110,103,104,117,98,117,110,103,105,32,112,101,110,103,101,109,98,97,110,103,46)
    private val INFO_2 = intArrayOf(8227,32,75,105,114,105,109,32,73,68,32,80,101,114,97,110,103,107,97,116,32,65,110,100,97,32,107,101,32,97,100,109,105,110,46)
    private val INFO_3 = intArrayOf(8227,32,84,117,110,103,103,117,32,104,105,110,103,103,97,32,112,101,114,97,110,103,107,97,116,32,65,110,100,97,32,116,101,114,100,97,102,116,97,114,46)
    private val INFO_4 = intArrayOf(8227,32,84,117,116,117,112,32,97,112,108,105,107,97,115,105,32,38,32,98,117,107,97,32,107,101,109,98,97,108,105,32,115,101,116,101,108,97,104,32,97,100,109,105,110,32,109,101,109,98,101,114,105,32,65,67,67,46)

    private val TXT_DEVICE_ID = intArrayOf(68,101,118,105,99,101,32,73,68,58)
    private val TXT_ADMIN = intArrayOf(65,68,77,73,78)
    private val TXT_COPY = intArrayOf(83,65,76,73,78,32,73,68)
    private val TXT_COPIED = intArrayOf(73,68,32,116,101,114,115,97,108,105,110)

    private val KEY_MAINT = intArrayOf(77,65,73,78,84,69,78,65,78,67,69)
    private val KEY_ENABLED = intArrayOf(101,110,97,98,108,101,100)
    private val KEY_LIMIT = intArrayOf(108,105,109,105,116)

    private val BG = Color.BLACK
    private val PURPLE = Color.parseColor("#C77DFF")
    private val UNLIMITED_PURPLE = Color.parseColor("#6A1B9A")
    private val GREY = Color.parseColor("#EDEDED")
    private val GREEN = Color.parseColor("#2E7D32")
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
            setPadding(dp(context,16),dp(context,16),dp(context,16),dp(context,16))
            setBackgroundColor(BG)
        }

        val statusBox = TextView(context).apply {
            text = decode(STATUS_PENDING)
            textSize = 12f
            setTextColor(GRAY)
            setPadding(dp(context,12),dp(context,8),dp(context,12),dp(context,8))
            background = rounded(GREY,10,context)
            startAnimation(blink())
        }
        root.addView(statusBox)

        space(root,context,12)

        root.addView(TextView(context).apply {
            text = decode(TITLE)
            setTextColor(Color.WHITE)
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
        })

        space(root,context,10)

        root.addView(TextView(context).apply {
            text = decode(SUBTITLE)
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
        })

        space(root,context,6)

        val infoBox = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(context,12),dp(context,10),dp(context,12),dp(context,10))
            background = rounded(Color.parseColor("#DDDDDD"),12,context)
        }

        listOf(INFO_1,INFO_2,INFO_3,INFO_4).forEach {
            infoBox.addView(TextView(context).apply {
                text = decode(it)
                textSize = 12f
                setTextColor(Color.DKGRAY)
            })
        }

        root.addView(infoBox)
        space(root,context,12)

        root.addView(TextView(context).apply {
            text = decode(TXT_DEVICE_ID)
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
            setPadding(dp(context,8),dp(context,8),dp(context,8),dp(context,8))
            background = rounded(Color.WHITE,10,context)
            layoutParams = LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1f)
        }
        row.addView(idBox)

        val adminBtn = actionButton(context, decode(TXT_ADMIN),55) {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, android.net.Uri.parse(adminUrl()))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
        adminBtn.layoutParams =
            LinearLayout.LayoutParams(dp(context,55), LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                marginStart = dp(context,6)
            }
        row.addView(adminBtn)

        val copyBtn = actionButton(context, decode(TXT_COPY),55) {
            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("x", deviceId))
            Toast.makeText(context, decode(TXT_COPIED), Toast.LENGTH_SHORT).show()
        }
        copyBtn.layoutParams =
            LinearLayout.LayoutParams(dp(context,55), LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                marginStart = dp(context,6)
            }
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
                Status.UNLIMITED -> { statusBox.text = decode(STATUS_UNLIMITED); statusBox.setTextColor(UNLIMITED_PURPLE); autoClose(dialog,onVerified) }
                Status.OK -> { statusBox.text = decode(STATUS_OK); statusBox.setTextColor(GREEN); autoClose(dialog,onVerified) }
                Status.NOT_FOUND -> { statusBox.text = decode(STATUS_FAIL); statusBox.setTextColor(RED) }
                Status.NETWORK -> { statusBox.text = decode(STATUS_NET); statusBox.setTextColor(YELLOW) }
            }
        }
    }

    private fun checkStatus(id: String): Status = try {
        val json = JSONObject((URL(jsonUrl()).openConnection() as HttpURLConnection).inputStream.bufferedReader().readText())
        when {
            json.has(decode(KEY_MAINT)) && json.getJSONObject(decode(KEY_MAINT)).optBoolean(decode(KEY_ENABLED),false) -> Status.MAINTENANCE
            json.has(id) && json.getJSONObject(id).optBoolean(decode(KEY_LIMIT),false) -> Status.UNLIMITED
            json.has(id) -> Status.OK
            else -> Status.NOT_FOUND
        }
    } catch (_: Exception) { Status.NETWORK }

    private fun showMaintenanceLock(context: Context) {
        val dialog = AlertDialog.Builder(context).create()
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(BG)
        }
        root.addView(TextView(context).apply {
            text = decode(STATUS_MAINT)
            setTextColor(BRIGHT_RED)
            textSize = 16f
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            startAnimation(blink())
        })
        dialog.setView(root)
        dialog.setCancelable(false)
        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        dialog.show()
        Handler(Looper.getMainLooper()).postDelayed({
            if (context is Activity) context.finishAffinity()
            android.os.Process.killProcess(android.os.Process.myPid())
            exitProcess(0)
        },2500)
    }

    private fun autoClose(d: AlertDialog, onVerified: () -> Unit) {
        Handler(Looper.getMainLooper()).postDelayed({ d.dismiss(); onVerified() }, AUTO_CLOSE_DELAY)
    }

    private fun getDeviceId(c: Context): String =
        Settings.Secure.getString(c.contentResolver, Settings.Secure.ANDROID_ID)?.take(16)
            ?: UUID.randomUUID().toString().take(16)

    private fun actionButton(c: Context, t: String, w: Int, click: () -> Unit) =
        Button(c).apply {
            text = t
            textSize = 12f
            setTextColor(Color.WHITE)
            setPadding(dp(c,12),dp(c,10),dp(c,12),dp(c,10))
            background = rounded(PURPLE,14,c)
            layoutParams = LinearLayout.LayoutParams(dp(c,w),LinearLayout.LayoutParams.WRAP_CONTENT)
            setOnTouchListener { v,e ->
                if (e.action==MotionEvent.ACTION_DOWN){v.scaleX=0.95f;v.scaleY=0.95f}
                else if (e.action==MotionEvent.ACTION_UP||e.action==MotionEvent.ACTION_CANCEL){v.scaleX=1f;v.scaleY=1f}
                false
            }
            setOnClickListener{click()}
        }

    private fun rounded(color:Int,r:Int,c:Context)=
        GradientDrawable().apply{setColor(color);cornerRadius=dp(c,r).toFloat()}

    private fun space(p:LinearLayout,c:Context,d:Int){
        p.addView(Space(c).apply{minimumHeight=dp(c,d)})
    }

    private fun dp(c:Context,v:Int)=
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,v.toFloat(),c.resources.displayMetrics).toInt()

    enum class Status { OK, UNLIMITED, NOT_FOUND, MAINTENANCE, NETWORK }
}
