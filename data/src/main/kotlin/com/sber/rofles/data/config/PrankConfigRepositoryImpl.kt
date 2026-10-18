package com.sber.rofles.data.config

import android.content.Context
import android.util.Log
import com.sber.rofles.domain.model.PrankConfig
import com.sber.rofles.domain.repository.PrankConfigRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrankConfigRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : PrankConfigRepository {

    private val cacheFile = File(context.filesDir, CACHE_NAME)

    private val state = MutableStateFlow(loadLocal())

    override val config: StateFlow<PrankConfig?> = state.asStateFlow()

    override suspend fun refresh() {
        val body = withContext(Dispatchers.IO) { runCatching { download() }.getOrNull() } ?: return
        val remote = ConfigParser.parse(body) ?: run {
            Log.w(TAG, "remote config is invalid")
            return
        }
        val current = state.value
        if (current != null && remote.version <= current.version) return
        withContext(Dispatchers.IO) { runCatching { cacheFile.writeText(body) } }
        state.value = remote
    }

    private fun loadLocal(): PrankConfig? {
        val cached = runCatching { cacheFile.takeIf { it.exists() }?.readText() }.getOrNull()
            ?.let(ConfigParser::parse)
        val bundled = runCatching { context.assets.open(ASSET_NAME).bufferedReader().use { it.readText() } }
            .getOrNull()
            ?.let(ConfigParser::parse)
        return listOfNotNull(cached, bundled).maxByOrNull { it.version }
    }

    private fun download(): String {
        val connection = URL(CONFIG_URL).openConnection() as HttpURLConnection
        return try {
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            connection.useCaches = false
            check(connection.responseCode == HttpURLConnection.HTTP_OK)
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val TAG = "PrankConfig"
        const val CMS_KEY = "kids_prank_high_five"
        const val CONFIG_URL = "https://raw.githubusercontent.com/vendetta1-1/kids-hackaton/main/config/$CMS_KEY.json"
        const val ASSET_NAME = "prank_config.json"
        const val CACHE_NAME = "$CMS_KEY.json"
        const val TIMEOUT_MS = 5_000
    }
}
