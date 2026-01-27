package com.lagradost.cloudstream3.utils

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.mvvm.logError
import kotlinx.coroutines.*
import kotlin.random.Random

object TestingUtils {
    open class TestResult(val success: Boolean) {
        companion object {
            val Pass = TestResult(true)
            val Fail = TestResult(false)
        }
    }

    class Logger {
        enum class LogLevel {
            Normal,
            Warning,
            Error;
        }

        data class Message(val level: LogLevel, val message: String) {
            override fun toString(): String {
                val levelPrefix = when (this.level) {
                    LogLevel.Normal -> ""
                    LogLevel.Warning -> "Warning: "
                    LogLevel.Error -> "Error: "
                }
                return "$levelPrefix$message"
            }
        }

        private val messageLog = mutableListOf<Message>()

        fun getRawLog(): List<Message> = messageLog

        fun log(message: String) {
            messageLog.add(Message(LogLevel.Normal, message))
        }

        fun warn(message: String) {
            messageLog.add(Message(LogLevel.Warning, message))
        }

        fun error(message: String) {
            messageLog.add(Message(LogLevel.Error, message))
        }
    }

    class TestResultList(val results: List<SearchResponse>) : TestResult(true)
    class TestResultLoad(val extractorData: String, val shouldLoadLinks: Boolean) : TestResult(true)

    class TestResultProvider(
        success: Boolean,
        val log: List<Logger.Message>,
        val exception: Throwable?
    ) :
        TestResult(success)

    @Throws(AssertionError::class, CancellationException::class)
    suspend fun testHomepage(api: MainAPI, logger: Logger): TestResult {
        if (api.hasMainPage) {
            try {
                val f = api.mainPage.first()
                val homepage =
                    api.getMainPage(1, MainPageRequest(f.name, f.data, f.horizontalImages))
                when {
                    homepage == null -> logger.error("Provider ${api.name} did not correctly load homepage!")
                    homepage.items.isEmpty() -> logger.warn("Provider ${api.name} does not contain any homepage rows!")
                    homepage.items.any { it.list.isEmpty() } -> logger.warn("Provider ${api.name} has empty homepage rows!")
                }
                val homePageList = homepage?.items?.flatMap { it.list } ?: emptyList()
                return TestResultList(homePageList)
            } catch (e: Throwable) {
                when (e) {
                    is NotImplementedError -> throw AssertionError("Provider marked as hasMainPage but not implemented")
                    is CancellationException -> throw e
                    else -> e.message?.let { logger.warn("Exception loading homepage: \"$it\"") }
                }
            }
        }
        return TestResult.Pass
    }

    private suspend fun testSearch(api: MainAPI, testQueries: List<String>, logger: Logger): TestResult {
        val searchResults = testQueries.firstNotNullOfOrNull { query ->
            try {
                logger.log("Searching for: $query")
                api.search(query, 1)?.items?.takeIf { it.isNotEmpty() }
            } catch (e: Throwable) {
                if (e is NotImplementedError) throw AssertionError("Provider has not implemented search()")
                else if (e is CancellationException) throw e
                logError(e)
                null
            }
        }

        if (searchResults.isNullOrEmpty()) throw AssertionError("Api ${api.name} did not return any search responses")
        return TestResultList(searchResults)
    }

    private suspend fun testLoad(api: MainAPI, result: SearchResponse, logger: Logger): TestResult {
        try {
            if (result.apiName != api.name) logger.warn("Wrong apiName on SearchResponse: ${api.name} != ${result.apiName}")

            val loadResponse = api.load(result.url) ?: run {
                logger.error("Returned null loadResponse on ${result.url} for ${api.name}")
                return TestResult.Fail
            }

            if (loadResponse.apiName != api.name) logger.warn("Wrong apiName on LoadResponse: ${api.name} != ${loadResponse.apiName}")
            if (!api.supportedTypes.contains(loadResponse.type)) logger.warn("Api ${api.name} loaded unsupported type: ${loadResponse.type}")

            val url = when (loadResponse) {
                is AnimeLoadResponse -> {
                    val gotNoEpisodes = loadResponse.episodes.keys.isEmpty() ||
                            loadResponse.episodes.keys.any { loadResponse.episodes[it].isNullOrEmpty() }
                    if (gotNoEpisodes) { logger.error("No episodes on ${loadResponse.url} for ${api.name}"); return TestResult.Fail }
                    loadResponse.episodes[loadResponse.episodes.keys.firstOrNull()]?.firstOrNull()?.data
                }
                is MovieLoadResponse -> {
                    if (loadResponse.dataUrl.isBlank()) { logger.error("No movie found on ${loadResponse.url} for ${api.name}"); return TestResult.Fail }
                    loadResponse.dataUrl
                }
                is TvSeriesLoadResponse -> {
                    if (loadResponse.episodes.isEmpty()) { logger.error("No episodes found on ${loadResponse.url} for ${api.name}"); return TestResult.Fail }
                    loadResponse.episodes.firstOrNull()?.data
                }
                is LiveStreamLoadResponse -> loadResponse.dataUrl
                else -> { logger.error("Unknown load response type: ${loadResponse.javaClass.name}"); return TestResult.Fail }
            } ?: return TestResult.Fail

            return TestResultLoad(url, loadResponse.type != TvType.CustomMedia)
        } catch (e: Throwable) {
            if (e is NotImplementedError) throw AssertionError("Provider has not implemented load()")
            throw e
        }
    }

    private suspend fun testLinkLoading(api: MainAPI, url: String?, logger: Logger): TestResult {
        if (url == null) throw AssertionError("Api ${api.name} has invalid url on episode")
        var linksLoaded = 0
        val success = api.loadLinks(url, false, {}) { link ->
            logger.log("Video loaded: ${link.name}")
            if (link.url.length <= 4) throw AssertionError("Api ${api.name} returns link with invalid url ${link.url}")
            linksLoaded++
        }
        if (success) {
            logger.log("Links loaded: $linksLoaded")
            return TestResult(linksLoaded > 0)
        } else throw AssertionError("Api ${api.name} returned false on loadLinks() with $linksLoaded links")
    }

    fun getDeferredProviderTests(
        scope: CoroutineScope,
        providers: Array<MainAPI>,
        callback: (MainAPI, TestResultProvider) -> Unit
    ) {
        providers.forEach { api ->
            scope.launch {
                val logger = Logger()
                val result = try {
                    logger.log("Trying ${api.name}")

                    val homepage = testHomepage(api, logger)
                    if (!homepage.success) throw AssertionError("Homepage failed for ${api.name}")
                    val homePageList = (homepage as? TestResultList)?.results ?: emptyList()

                    val searchQueries =
                        (homePageList.shuffled(Random).take(3).map { it.name.split(" ").first() } +
                                listOf("over", "iron", "guy")).take(3)

                    val searchResults = testSearch(api, searchQueries, logger) as TestResultList
                    if (!searchResults.success) throw AssertionError("Failed to get search results for ${api.name}")

                    val success = searchResults.results.take(3).any { searchResponse ->
                        logger.log("Testing search result: ${searchResponse.url}")
                        val loadResponse = testLoad(api, searchResponse, logger)
                        if (loadResponse !is TestResultLoad) false
                        else if (loadResponse.shouldLoadLinks) testLinkLoading(api, loadResponse.extractorData, logger).success
                        else { logger.log("Skipping link loading test"); true }
                    }

                    if (success) {
                        logger.log("Success ${api.name}")
                        TestResultProvider(true, logger.getRawLog(), null)
                    } else {
                        logger.error("Link loading failed for ${api.name}")
                        TestResultProvider(false, logger.getRawLog(), null)
                    }

                } catch (e: Throwable) {
                    TestResultProvider(false, logger.getRawLog(), e)
                }
                callback.invoke(api, result)
            }
        }
    }
}
