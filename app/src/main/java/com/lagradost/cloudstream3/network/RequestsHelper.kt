package com.lagradost.cloudstream3.network

import android.content.Context
import androidx.preference.PreferenceManager
import com.lagradost.cloudstream3.R
import com.lagradost.cloudstream3.USER_AGENT
import com.lagradost.cloudstream3.mvvm.safe
import com.lagradost.nicehttp.Requests
import com.lagradost.nicehttp.ignoreAllSSLErrors
import okhttp3.Cache
import okhttp3.Headers
import okhttp3.Headers.Companion.toHeaders
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import org.conscrypt.Conscrypt
import java.io.File
import java.security.Security

/**
 * Extension function untuk init client Requests
 */
fun Requests.initClient(context: Context) {
    this.baseClient = buildDefaultClient(context)
}

/**
 * Build OkHttpClient standar untuk CloudPlay
 * Termasuk: cache, SSL bypass, custom DNS, logging
 */
fun buildDefaultClient(context: Context): OkHttpClient {
    safe { Security.insertProviderAt(Conscrypt.newProvider(), 1) }

    val settingsManager = PreferenceManager.getDefaultSharedPreferences(context)
    val dns = settingsManager.getInt(context.getString(R.string.dns_pref), 0)

    val builder = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .ignoreAllSSLErrors()
        .cache(
            Cache(
                directory = File(context.cacheDir, "http_cache"),
                maxSize = 50L * 1024L * 1024L // 50 MB
            )
        )
        // Logging interceptor untuk debug HTTP
        .addInterceptor(DebugLoggingInterceptor())

    // Setup DNS custom sesuai setting user
    when (dns) {
        1 -> builder.addGoogleDns()
        2 -> builder.addCloudFlareDns()
        4 -> builder.addAdGuardDns()
        5 -> builder.addDNSWatchDns()
        6 -> builder.addQuad9Dns()
        7 -> builder.addDnsSbDns()
        8 -> builder.addCanadianShieldDns()
    }

    return builder.build()
}

/**
 * Interceptor untuk log request + response
 */
class DebugLoggingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        println("✅ DEBUG REQUEST: ${request.method} ${request.url}")
        val response = chain.proceed(request)
        println("📌 DEBUG RESPONSE: ${response.code} ${response.message} -> ${request.url}")
        return response
    }
}

/**
 * Headers helper
 * Gabungin default headers + custom headers + cookies
 */
private val DEFAULT_HEADERS = mapOf("user-agent" to USER_AGENT)

fun getHeaders(
    headers: Map<String, String>,
    cookie: Map<String, String>
): Headers {
    val cookieMap =
        if (cookie.isNotEmpty()) mapOf(
            "Cookie" to cookie.entries.joinToString(" ") { "${it.key}=${it.value};" }
        ) else emptyMap()

    val tempHeaders = DEFAULT_HEADERS + headers + cookieMap
    return tempHeaders.toHeaders()
}
