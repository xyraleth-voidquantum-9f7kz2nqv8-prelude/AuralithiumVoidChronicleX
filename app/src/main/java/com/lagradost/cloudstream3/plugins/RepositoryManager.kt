package com.lagradost.cloudstream3.plugins

import android.content.Context
import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.CloudStreamApp.Companion.context
import com.lagradost.cloudstream3.CloudStreamApp.Companion.getKey
import com.lagradost.cloudstream3.CloudStreamApp.Companion.setKey
import com.lagradost.cloudstream3.R
import com.lagradost.cloudstream3.amap
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.mvvm.logError
import com.lagradost.cloudstream3.mvvm.safe
import com.lagradost.cloudstream3.mvvm.safeAsync
import com.lagradost.cloudstream3.plugins.PluginManager.getPluginSanitizedFileName
import com.lagradost.cloudstream3.plugins.PluginManager.unloadPlugin
import com.lagradost.cloudstream3.ui.settings.extensions.REPOSITORIES_KEY
import com.lagradost.cloudstream3.ui.settings.extensions.RepositoryData
import com.lagradost.cloudstream3.utils.AppUtils.tryParseJson
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.BufferedInputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream

data class Repository(
    @JsonProperty("iconUrl") val iconUrl: String?,
    @JsonProperty("name") val name: String,
    @JsonProperty("description") val description: String?,
    @JsonProperty("manifestVersion") val manifestVersion: Int,
    @JsonProperty("pluginLists") val pluginLists: List<String>
)

data class SitePlugin(
    @JsonProperty("url") val url: String,
    @JsonProperty("status") val status: Int,
    @JsonProperty("version") val version: Int,
    @JsonProperty("apiVersion") val apiVersion: Int,
    @JsonProperty("name") val name: String,
    @JsonProperty("internalName") val internalName: String,
    @JsonProperty("authors") val authors: List<String>,
    @JsonProperty("description") val description: String?,
    @JsonProperty("repositoryUrl") val repositoryUrl: String?,
    @JsonProperty("tvTypes") val tvTypes: List<String>?,
    @JsonProperty("language") val language: String?,
    @JsonProperty("iconUrl") val iconUrl: String?,
    @JsonProperty("fileSize") val fileSize: Long?
)

object RepositoryManager {

    const val ONLINE_PLUGINS_FOLDER = "Extensions"

    val PREBUILT_REPOSITORIES: Array<RepositoryData> by lazy {
        getKey("PREBUILT_REPOSITORIES") ?: emptyArray()
    }

    private val GH_REGEX =
        Regex("^https://raw.githubusercontent.com/([A-Za-z0-9-]+)/([A-Za-z0-9_.-]+)/(.*)$")

    fun convertRawGitUrl(url: String): String {
        if (getKey<Boolean>(context!!.getString(R.string.jsdelivr_proxy_key)) != true) return url
        val match = GH_REGEX.find(url) ?: return url
        val (user, repo, rest) = match.destructured
        return "https://cdn.jsdelivr.net/gh/$user/$repo@$rest"
    }

    suspend fun parseRepoUrl(url: String): String? {
        val fixedUrl = url.trim()
        return if (fixedUrl.contains("^https?://".toRegex())) {
            fixedUrl
        } else if (fixedUrl.contains("^(cloudstreamrepo://)|(https://cs\\.repo/\\??)".toRegex())) {
            fixedUrl.replace("^(cloudstreamrepo://)|(https://cs\\.repo/\\??)".toRegex(), "").let {
                if (!it.contains("^https?://".toRegex())) "https://${it}" else fixedUrl
            }
        } else if (fixedUrl.matches("^[a-zA-Z0-9!_-]+$".toRegex())) {
            safeAsync {
                app.get("https://cutt.ly/${fixedUrl}", allowRedirects = false)
                    .headers["Location"]
                    ?.takeIf {
                        !it.startsWith("https://cutt.ly/404") &&
                                it.removeSuffix("/") != "https://cutt.ly"
                    }
            }
        } else null
    }

    suspend fun parseRepository(url: String): Repository? {
        return safeAsync {
            app.get(
                convertRawGitUrl(url),
                headers = mapOf(
                    "Cache-Control" to "no-cache",
                    "Pragma" to "no-cache"
                )
            ).parsedSafe()
        }
    }

    private suspend fun parsePlugins(pluginUrls: String): List<SitePlugin> {
        return try {
            val response = app.get(
                convertRawGitUrl(pluginUrls),
                headers = mapOf(
                    "Cache-Control" to "no-cache",
                    "Pragma" to "no-cache"
                )
            )
            tryParseJson<Array<SitePlugin>>(response.text)?.toList() ?: emptyList()
        } catch (t: Throwable) {
            logError(t)
            emptyList()
        }
    }

    suspend fun getRepoPlugins(repositoryUrl: String): List<Pair<String, SitePlugin>>? {
        val repo = parseRepository(repositoryUrl) ?: return null
        return repo.pluginLists.amap { url ->
            parsePlugins(url).map { repositoryUrl to it }
        }.flatten()
    }

    suspend fun downloadPluginToFile(pluginUrl: String, file: File): File? {
        return safeAsync {
            file.parentFile?.mkdirs()
            if (file.exists()) file.delete()
            file.createNewFile()
            val body = app.get(convertRawGitUrl(pluginUrl)).okhttpResponse.body
            write(body.byteStream(), file.outputStream())
            file
        }
    }

    fun getRepositories(): Array<RepositoryData> {
        return getKey(REPOSITORIES_KEY) ?: emptyArray()
    }

    private val repoLock = Mutex()

    suspend fun addRepository(repository: RepositoryData) {
        repoLock.withLock {
            val currentRepos = getRepositories()
            setKey(REPOSITORIES_KEY, (currentRepos + repository).distinctBy { it.url })
        }
    }

    suspend fun removeRepository(context: Context, repository: RepositoryData) {
        val extensionsDir = File(context.filesDir, ONLINE_PLUGINS_FOLDER)

        repoLock.withLock {
            val currentRepos = getKey<Array<RepositoryData>>(REPOSITORIES_KEY) ?: emptyArray()
            setKey(REPOSITORIES_KEY, currentRepos.filter { it.url != repository.url })
        }

        val file = File(
            extensionsDir,
            getPluginSanitizedFileName(repository.url)
        )

        safe {
            file.listFiles { plugin ->
                unloadPlugin(plugin.absolutePath)
                false
            }
        }

        PluginManager.deleteRepositoryData(file.absolutePath)
    }

    private fun write(stream: InputStream, output: OutputStream) {
        val input = BufferedInputStream(stream)
        val buffer = ByteArray(512)
        var read: Int
        while (input.read(buffer).also { read = it } != -1) {
            output.write(buffer, 0, read)
        }
    }
}
