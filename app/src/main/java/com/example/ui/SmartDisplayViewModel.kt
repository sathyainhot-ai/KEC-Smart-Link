package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.NetworkResult
import com.example.data.NetworkScanner
import com.example.data.PreferencesManager
import com.example.data.ScanProgress
import com.example.data.SmartDisplayClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class DisplayMode {
  SCROLLING_TEXT,
  SENSOR_TELEMETRY,
  UNKNOWN
}

data class CommandLog(
  val id: Long = System.currentTimeMillis(),
  val timestamp: String,
  val command: String,
  val endpoint: String,
  val isSuccess: Boolean,
  val statusMessage: String,
  val latencyMs: Long? = null,
  val responseBody: String? = null
)

data class SmartDisplayUiState(
  val ipAddress: String = "",
  val messageText: String = "",
  val currentMode: DisplayMode = DisplayMode.UNKNOWN,
  val activeDisplayContent: String = "ESP8266 SMART DISPLAY",
  val isExecuting: Boolean = false,
  val currentAction: String? = null,
  val statusMessage: String = "Ready to connect",
  val isSuccessStatus: Boolean? = null,
  val lastResponseBody: String? = null,
  val commandLogs: List<CommandLog> = emptyList(),
  val scanProgress: ScanProgress = ScanProgress(),
  val detectedSubnet: String = "192.168.1."
)

class SmartDisplayViewModel(application: Application) : AndroidViewModel(application) {

  private val prefs = PreferencesManager(application)
  private val client = SmartDisplayClient()
  private val scanner = NetworkScanner(application)
  private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
  private var scanJob: Job? = null

  private val _uiState = MutableStateFlow(
    SmartDisplayUiState(
      ipAddress = prefs.getIpAddress(),
      messageText = prefs.getLastMessage(),
      detectedSubnet = scanner.detectLocalSubnet()
    )
  )
  val uiState: StateFlow<SmartDisplayUiState> = _uiState.asStateFlow()

  fun onIpAddressChanged(newIp: String) {
    _uiState.update { it.copy(ipAddress = newIp) }
    prefs.saveIpAddress(newIp)
  }

  fun applyQuickIp(ip: String) {
    onIpAddressChanged(ip)
    testConnection()
  }

  fun startNetworkScan(customSubnet: String? = null) {
    if (_uiState.value.scanProgress.isScanning) return

    val targetSubnet = customSubnet ?: _uiState.value.detectedSubnet
    _uiState.update {
      it.copy(
        scanProgress = ScanProgress(
          isScanning = true,
          message = "Scanning for NodeMCU display on $targetSubnet*..."
        )
      )
    }

    scanJob?.cancel()
    scanJob = viewModelScope.launch {
      val found = scanner.scanForDisplay(targetSubnet) { progress ->
        _uiState.update { it.copy(scanProgress = progress) }
      }

      if (found != null) {
        onIpAddressChanged(found)
        val timestamp = timeFormat.format(Date())
        val scanLog = CommandLog(
          timestamp = timestamp,
          command = "Auto-Discovery",
          endpoint = found,
          isSuccess = true,
          statusMessage = "Found display at $found"
        )
        _uiState.update {
          it.copy(
            statusMessage = "Connected to NodeMCU at $found",
            isSuccessStatus = true,
            commandLogs = listOf(scanLog) + it.commandLogs.take(19)
          )
        }
      } else {
        _uiState.update {
          it.copy(
            scanProgress = ScanProgress(
              isScanning = false,
              message = "Scan finished: Display not detected on $targetSubnet*"
            )
          )
        }
      }
    }
  }

  fun cancelNetworkScan() {
    scanJob?.cancel()
    scanJob = null
    _uiState.update {
      it.copy(
        scanProgress = ScanProgress(isScanning = false, message = "Scan cancelled")
      )
    }
  }

  fun testConnection() {
    val state = _uiState.value
    val ip = state.ipAddress.trim()

    if (ip.isEmpty()) {
      _uiState.update {
        it.copy(
          statusMessage = "Please enter an IP Address to test",
          isSuccessStatus = false
        )
      }
      return
    }

    _uiState.update {
      it.copy(
        isExecuting = true,
        currentAction = "Pinging display...",
        statusMessage = "Testing connection to $ip..."
      )
    }

    viewModelScope.launch {
      when (val result = client.testConnection(ip)) {
        is NetworkResult.Success -> {
          val timestamp = timeFormat.format(Date())
          val log = CommandLog(
            timestamp = timestamp,
            command = "Test Ping",
            endpoint = "/message",
            isSuccess = true,
            statusMessage = "Online (HTTP ${result.statusCode})",
            latencyMs = result.latencyMs,
            responseBody = result.responseBody
          )
          _uiState.update {
            it.copy(
              isExecuting = false,
              currentAction = null,
              statusMessage = "Online! Display is responding at $ip (${result.latencyMs}ms)",
              isSuccessStatus = true,
              commandLogs = listOf(log) + it.commandLogs.take(19)
            )
          }
        }
        is NetworkResult.Error -> {
          val timestamp = timeFormat.format(Date())
          val log = CommandLog(
            timestamp = timestamp,
            command = "Test Ping",
            endpoint = "/message",
            isSuccess = false,
            statusMessage = result.message
          )
          _uiState.update {
            it.copy(
              isExecuting = false,
              currentAction = null,
              statusMessage = result.message,
              isSuccessStatus = false,
              commandLogs = listOf(log) + it.commandLogs.take(19)
            )
          }
        }
      }
    }
  }

  fun onMessageTextChanged(newMessage: String) {
    val limitedMessage = if (newMessage.length > 500) newMessage.take(500) else newMessage
    _uiState.update { it.copy(messageText = limitedMessage) }
    prefs.saveLastMessage(limitedMessage)
  }

  fun applyPresetMessage(preset: String) {
    onMessageTextChanged(preset)
  }

  fun sendMessage() {
    val state = _uiState.value
    val ip = state.ipAddress.trim()
    val text = state.messageText

    if (ip.isEmpty()) {
      _uiState.update {
        it.copy(
          statusMessage = "Error: Please enter NodeMCU IP Address",
          isSuccessStatus = false
        )
      }
      return
    }

    if (text.isEmpty()) {
      _uiState.update {
        it.copy(
          statusMessage = "Error: Message cannot be empty",
          isSuccessStatus = false
        )
      }
      return
    }

    _uiState.update {
      it.copy(
        isExecuting = true,
        currentAction = "Sending message...",
        statusMessage = "Sending message to $ip..."
      )
    }

    viewModelScope.launch {
      when (val result = client.sendMessage(ip, text)) {
        is NetworkResult.Success -> {
          val timestamp = timeFormat.format(Date())
          val newLog = CommandLog(
            timestamp = timestamp,
            command = "Send Message",
            endpoint = "/message?text=${text.take(20)}",
            isSuccess = true,
            statusMessage = "Command Sent (HTTP ${result.statusCode})",
            latencyMs = result.latencyMs,
            responseBody = result.responseBody
          )

          _uiState.update {
            it.copy(
              isExecuting = false,
              currentAction = null,
              statusMessage = "Command Sent (HTTP ${result.statusCode} in ${result.latencyMs}ms)",
              isSuccessStatus = true,
              currentMode = DisplayMode.SCROLLING_TEXT,
              activeDisplayContent = text,
              lastResponseBody = result.responseBody,
              commandLogs = listOf(newLog) + it.commandLogs.take(19)
            )
          }
        }

        is NetworkResult.Error -> {
          val timestamp = timeFormat.format(Date())
          val newLog = CommandLog(
            timestamp = timestamp,
            command = "Send Message",
            endpoint = "/message",
            isSuccess = false,
            statusMessage = result.message
          )

          _uiState.update {
            it.copy(
              isExecuting = false,
              currentAction = null,
              statusMessage = result.message,
              isSuccessStatus = false,
              lastResponseBody = null,
              commandLogs = listOf(newLog) + it.commandLogs.take(19)
            )
          }
        }
      }
    }
  }

  fun showSensorData() {
    val state = _uiState.value
    val ip = state.ipAddress.trim()

    if (ip.isEmpty()) {
      _uiState.update {
        it.copy(
          statusMessage = "Error: Please enter NodeMCU IP Address",
          isSuccessStatus = false
        )
      }
      return
    }

    _uiState.update {
      it.copy(
        isExecuting = true,
        currentAction = "Requesting sensor mode...",
        statusMessage = "Switching display to sensor telemetry..."
      )
    }

    viewModelScope.launch {
      when (val result = client.showSensorData(ip)) {
        is NetworkResult.Success -> {
          val timestamp = timeFormat.format(Date())
          val newLog = CommandLog(
            timestamp = timestamp,
            command = "Show Sensor Data",
            endpoint = "/sensor",
            isSuccess = true,
            statusMessage = "Command Sent (HTTP ${result.statusCode})",
            latencyMs = result.latencyMs,
            responseBody = result.responseBody
          )

          _uiState.update {
            it.copy(
              isExecuting = false,
              currentAction = null,
              statusMessage = "Command Sent (Switched to Sensor Mode - HTTP ${result.statusCode})",
              isSuccessStatus = true,
              currentMode = DisplayMode.SENSOR_TELEMETRY,
              activeDisplayContent = "DHT11: TEMP & HUMIDITY MODE",
              lastResponseBody = result.responseBody,
              commandLogs = listOf(newLog) + it.commandLogs.take(19)
            )
          }
        }

        is NetworkResult.Error -> {
          val timestamp = timeFormat.format(Date())
          val newLog = CommandLog(
            timestamp = timestamp,
            command = "Show Sensor Data",
            endpoint = "/sensor",
            isSuccess = false,
            statusMessage = result.message
          )

          _uiState.update {
            it.copy(
              isExecuting = false,
              currentAction = null,
              statusMessage = result.message,
              isSuccessStatus = false,
              lastResponseBody = null,
              commandLogs = listOf(newLog) + it.commandLogs.take(19)
            )
          }
        }
      }
    }
  }

  fun clearLogs() {
    _uiState.update { it.copy(commandLogs = emptyList()) }
  }
}
