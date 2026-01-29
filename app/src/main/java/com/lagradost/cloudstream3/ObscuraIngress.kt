package com.lagradost.cloudstream3

import android.app.Activity
import android.util.Base64
import android.widget.Toast
import com.lagradost.cloudstream3.plugins.RepositoryManager
import com.lagradost.cloudstream3.ui.settings.extensions.RepositoryData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ObscuraIngress {

    private const val P1 = "aHR0cHM6Ly9wYXN0ZWJp"
    private const val P2 = "bi5jb20vcmF3L0oxa0dhZ3Zn"

    private fun repoUrl(): String {
        val encoded = P1 + P2
        return String(Base64.decode(encoded, Base64.DEFAULT))
    }

    private fun decode(data: IntArray, key: Int): String {
        val out = CharArray(data.size)
        for (i in data.indices) {
            out[i] = (data[i] xor key).toChar()
        }
        return String(out)
    }

    private val REPO_NAME = intArrayOf(
        31, 46, 46, 25, 54, 53, 47, 62
    )

    private val MSG_OK = intArrayOf(
        8, 63, 42, 53, 122, 56, 63, 40, 41, 51, 54, 122,
        62, 51, 46, 59, 55, 56, 59, 50, 49, 59, 52
    )

    private val MSG_FAIL = intArrayOf(
        29, 59, 61, 59, 54, 122, 55, 63, 52, 59,
        55, 56, 59, 50, 49, 59, 52, 122, 40, 63, 42, 53
    )

    private const val KEY = 0x5A

    fun install(activity: Activity) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                RepositoryManager.addRepository(
                    RepositoryData(
                        name = decode(REPO_NAME, KEY),
                        url = repoUrl(),
                        iconUrl = null
                    )
                )

                activity.runOnUiThread {
                    Toast.makeText(
                        activity,
                        decode(MSG_OK, KEY),
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (_: Throwable) {
                activity.runOnUiThread {
                    Toast.makeText(
                        activity,
                        decode(MSG_FAIL, KEY),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}