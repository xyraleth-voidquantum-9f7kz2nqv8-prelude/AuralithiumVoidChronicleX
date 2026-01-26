package com.lagradost.cloudstream3

import android.app.Activity
import android.os.Handler
import android.os.Looper
import org.json.JSONObject
import java.net.URL
import kotlin.concurrent.thread

object NebulaGate {

    private const val UPDATE_URL =
        "https://raw.githubusercontent.com/xyraleth-voidquantum-9f7kz2nqv8-prelude/AuralithiumVoidChronicleX-Update/main/update.json"

    fun probe(activity: Activity) {
        thread {
            try {
                val json = URL(UPDATE_URL).readText()
                val data = JSONObject(json)

                val serverCode = data.getInt("versionCode")
                val changelog = data.getString("changelog")
                val apkUrl = data.getString("apk")

                if (serverCode > BuildConfig.VERSION_CODE) {
                    Handler(Looper.getMainLooper()).post {
                        AstraFlag.dispatch(activity, changelog, apkUrl)
                    }
                }

            } catch (_: Exception) {
            }
        }
    }
}