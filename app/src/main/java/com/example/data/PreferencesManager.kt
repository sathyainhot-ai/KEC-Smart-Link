package com.example.data

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
  private val sharedPreferences: SharedPreferences =
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

  fun getIpAddress(): String {
    return sharedPreferences.getString(KEY_IP_ADDRESS, DEFAULT_IP) ?: DEFAULT_IP
  }

  fun saveIpAddress(ip: String) {
    sharedPreferences.edit().putString(KEY_IP_ADDRESS, ip.trim()).apply()
  }

  fun getLastMessage(): String {
    return sharedPreferences.getString(KEY_LAST_MESSAGE, DEFAULT_MESSAGE) ?: DEFAULT_MESSAGE
  }

  fun saveLastMessage(message: String) {
    sharedPreferences.edit().putString(KEY_LAST_MESSAGE, message).apply()
  }

  companion object {
    private const val PREFS_NAME = "smart_display_prefs"
    private const val KEY_IP_ADDRESS = "key_ip_address"
    private const val KEY_LAST_MESSAGE = "key_last_message"
    private const val KEY_SECURITY_PIN = "key_security_pin"
    private const val DEFAULT_IP = "192.168.4.1"
    private const val DEFAULT_MESSAGE = "HELLO WORLD"
  }

  fun getSecurityPin(): String {
    return sharedPreferences.getString(KEY_SECURITY_PIN, "") ?: ""
  }

  fun saveSecurityPin(pin: String) {
    sharedPreferences.edit().putString(KEY_SECURITY_PIN, pin.trim()).apply()
  }
}
