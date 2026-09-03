package com.example.data

import android.content.Context
import android.net.wifi.WifiManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket
import java.util.concurrent.TimeUnit

data class ScanProgress(
  val isScanning: Boolean = false,
  val scannedCount: Int = 0,
  val totalCount: Int = 254,
  val currentIp: String = "",
  val foundIp: String? = null,
  val message: String = ""
)

class NetworkScanner(private val context: Context) {

  private val probeClient = OkHttpClient.Builder()
    .connectTimeout(700, TimeUnit.MILLISECONDS)
    .readTimeout(700, TimeUnit.MILLISECONDS)
    .build()

  /**
   * Resolves the current local IPv4 subnet prefix (e.g., "192.168.1.")
   * from active network interfaces or WiFi info.
   */
  fun detectLocalSubnet(): String {
    try {
      val interfaces = NetworkInterface.getNetworkInterfaces()
      if (interfaces != null) {
        for (intf in interfaces) {
          if (intf.isLoopback || !intf.isUp) continue
          val addrs = intf.inetAddresses
          for (addr in addrs) {
            if (!addr.isLoopbackAddress && addr is java.net.Inet4Address) {
              val host = addr.hostAddress ?: continue
              if (host.startsWith("192.168.") || host.startsWith("10.") || host.startsWith("172.")) {
                val parts = host.split(".")
                if (parts.size == 4) {
                  return "${parts[0]}.${parts[1]}.${parts[2]}."
                }
              }
            }
          }
        }
      }
    } catch (e: Exception) {
      // Fallback
    }

    try {
      @Suppress("DEPRECATION")
      val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
      val ipInt = wifiManager?.connectionInfo?.ipAddress ?: 0
      if (ipInt != 0) {
        val b1 = ipInt and 0xff
        val b2 = (ipInt shr 8) and 0xff
        val b3 = (ipInt shr 16) and 0xff
        return "$b1.$b2.$b3."
      }
    } catch (e: Exception) {
      // Fallback
    }

    // Default to common NodeMCU subnet if undetected
    return "192.168.1."
  }

  /**
   * Probes whether port 80 is open and optionally if /message endpoint exists.
   */
  suspend fun isDisplayServer(ip: String): Boolean = withContext(Dispatchers.IO) {
    // 1. Fast socket check (300ms)
    val portOpen = try {
      Socket().use { socket ->
        socket.connect(InetSocketAddress(ip, 80), 300)
        true
      }
    } catch (e: Exception) {
      false
    }

    if (!portOpen) return@withContext false

    // 2. HTTP probe to /message endpoint
    return@withContext try {
      val request = Request.Builder()
        .url("http://$ip/message")
        .get()
        .build()

      probeClient.newCall(request).execute().use { response ->
        // ESP8266WebServer will return 400 (if no text arg) or 200, which confirms it's the display server!
        response.code == 200 || response.code == 400 || response.code == 404
      }
    } catch (e: Exception) {
      // If socket connected on port 80, it is very likely the ESP device
      true
    }
  }

  /**
   * Scans priority candidate IPs and then the subnet in parallel batches.
   */
  suspend fun scanForDisplay(
    customSubnet: String? = null,
    onProgress: (ScanProgress) -> Unit
  ): String? = coroutineScope {
    val subnet = customSubnet?.trim()?.let {
      if (it.endsWith(".")) it else "$it."
    } ?: detectLocalSubnet()

    onProgress(
      ScanProgress(
        isScanning = true,
        scannedCount = 0,
        totalCount = 254,
        message = "Checking static IP & subnet $subnet*"
      )
    )

    // Priority targets: static IP in sketch (150), AP default (192.168.4.1), and common router assignments
    val priorityIps = mutableListOf("${subnet}150", "192.168.1.150", "192.168.4.1", "${subnet}100", "${subnet}1", "${subnet}2", "${subnet}200")
      .distinct()

    for (ip in priorityIps) {
      onProgress(
        ScanProgress(
          isScanning = true,
          scannedCount = 1,
          totalCount = 254,
          currentIp = ip,
          message = "Testing priority candidate: $ip"
        )
      )

      if (isDisplayServer(ip)) {
        onProgress(
          ScanProgress(
            isScanning = false,
            scannedCount = 254,
            foundIp = ip,
            message = "Found KEC Smart Display at $ip!"
          )
        )
        return@coroutineScope ip
      }
    }

    // Parallel scan of the remainder of the 1..254 subnet in chunks of 25
    val remainingIps = (1..254)
      .map { "$subnet$it" }
      .filterNot { priorityIps.contains(it) }

    var testedCount = priorityIps.size
    val chunks = remainingIps.chunked(25)

    for (chunk in chunks) {
      val deferreds = chunk.map { ip ->
        async(Dispatchers.IO) {
          if (isDisplayServer(ip)) ip else null
        }
      }

      val results = deferreds.awaitAll()
      val found = results.firstOrNull { it != null }
      if (found != null) {
        onProgress(
          ScanProgress(
            isScanning = false,
            scannedCount = testedCount + chunk.size,
            foundIp = found,
            message = "Found KEC Smart Display at $found!"
          )
        )
        return@coroutineScope found
      }

      testedCount += chunk.size
      onProgress(
        ScanProgress(
          isScanning = true,
          scannedCount = testedCount,
          totalCount = 254,
          currentIp = chunk.last(),
          message = "Scanning $subnet* ($testedCount/254)..."
        )
      )
    }

    onProgress(
      ScanProgress(
        isScanning = false,
        scannedCount = 254,
        foundIp = null,
        message = "Scan complete. No display found on $subnet*."
      )
    )
    null
  }
}
