package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.SocketTimeoutException
import java.net.URLEncoder
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

sealed interface NetworkResult {
  data class Success(
    val statusCode: Int,
    val responseBody: String,
    val latencyMs: Long,
    val url: String
  ) : NetworkResult

  data class Error(
    val message: String,
    val url: String,
    val isTimeout: Boolean = false
  ) : NetworkResult
}

class SmartDisplayClient {

  private val client = OkHttpClient.Builder()
    .connectTimeout(5, TimeUnit.SECONDS)
    .readTimeout(5, TimeUnit.SECONDS)
    .writeTimeout(5, TimeUnit.SECONDS)
    .build()

  /**
   * Sanitizes the IP input to ensure a valid HTTP base URL.
   */
  fun formatBaseUrl(rawIp: String): String {
    var trimmed = rawIp.trim()
    if (trimmed.isEmpty()) return ""
    if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
      trimmed = "http://$trimmed"
    }
    return trimmed.removeSuffix("/")
  }

  /**
   * Sends custom text message to NodeMCU:
   * GET http://<IP_ADDRESS>/message?text=<USER_TEXT>
   */
  suspend fun sendMessage(ipAddress: String, text: String): NetworkResult = withContext(Dispatchers.IO) {
    val baseUrl = formatBaseUrl(ipAddress)
    if (baseUrl.isEmpty()) {
      return@withContext NetworkResult.Error("Please enter a valid IP address", "")
    }

    val encodedText = try {
      URLEncoder.encode(text, "UTF-8")
    } catch (e: Exception) {
      text.replace(" ", "%20")
    }

    val fullUrl = "$baseUrl/message?text=$encodedText"
    executeGetRequest(fullUrl)
  }

  /**
   * Switches display to live DHT11 sensor mode:
   * GET http://<IP_ADDRESS>/sensor
   */
  suspend fun showSensorData(ipAddress: String): NetworkResult = withContext(Dispatchers.IO) {
    val baseUrl = formatBaseUrl(ipAddress)
    if (baseUrl.isEmpty()) {
      return@withContext NetworkResult.Error("Please enter a valid IP address", "")
    }

    val fullUrl = "$baseUrl/sensor"
    executeGetRequest(fullUrl)
  }

  /**
   * Tests reachability of the NodeMCU Web Server.
   */
  suspend fun testConnection(ipAddress: String): NetworkResult = withContext(Dispatchers.IO) {
    val baseUrl = formatBaseUrl(ipAddress)
    if (baseUrl.isEmpty()) {
      return@withContext NetworkResult.Error("Please enter a valid IP address", "")
    }

    // Ping the /message endpoint to see if ESP server responds
    val fullUrl = "$baseUrl/message"
    executeGetRequest(fullUrl)
  }

  private fun executeGetRequest(url: String): NetworkResult {
    val startTime = System.currentTimeMillis()
    val request = try {
      Request.Builder()
        .url(url)
        .get()
        .build()
    } catch (e: Exception) {
      return NetworkResult.Error("Invalid URL: ${e.localizedMessage ?: "Check IP address"}", url)
    }

    return try {
      client.newCall(request).execute().use { response ->
        val latency = System.currentTimeMillis() - startTime
        val bodyString = response.body?.string()?.trim().orEmpty()

        if (response.isSuccessful) {
          NetworkResult.Success(
            statusCode = response.code,
            responseBody = if (bodyString.isEmpty()) "OK (HTTP ${response.code})" else bodyString,
            latencyMs = latency,
            url = url
          )
        } else {
          NetworkResult.Error(
            message = "HTTP ${response.code}: ${response.message.ifEmpty { "Server error" }}",
            url = url
          )
        }
      }
    } catch (e: SocketTimeoutException) {
      NetworkResult.Error(
        message = "Error: Timeout (ESP8266 not responding at $url)",
        url = url,
        isTimeout = true
      )
    } catch (e: UnknownHostException) {
      NetworkResult.Error(
        message = "Error: Host unreachable. Verify WiFi connection and IP address.",
        url = url
      )
    } catch (e: java.net.ConnectException) {
      NetworkResult.Error(
        message = "Error: Connection refused. Is NodeMCU powered on and on the same WiFi network?",
        url = url
      )
    } catch (e: Exception) {
      NetworkResult.Error(
        message = "Network error: ${e.localizedMessage ?: e.javaClass.simpleName}",
        url = url
      )
    }
  }
}
