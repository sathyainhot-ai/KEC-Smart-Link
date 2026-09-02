package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.PreferencesManager
import com.example.data.SmartDisplayClient
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("KEC Smart Display", appName)
  }

  @Test
  fun `test preferences manager ip persistence`() {
    val context = ApplicationProvider.getApplicationContext<Application>()
    val prefs = PreferencesManager(context)
    prefs.saveIpAddress("192.168.1.150")
    assertEquals("192.168.1.150", prefs.getIpAddress())
  }

  @Test
  fun `test client format base url`() {
    val client = SmartDisplayClient()
    assertEquals("http://192.168.1.50", client.formatBaseUrl("192.168.1.50"))
    assertEquals("http://192.168.1.50", client.formatBaseUrl("http://192.168.1.50/"))
  }
}

