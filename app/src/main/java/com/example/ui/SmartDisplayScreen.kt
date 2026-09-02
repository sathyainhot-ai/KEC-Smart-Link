package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.GeoBlueContainer
import com.example.ui.theme.GeoBlueOnPrimary
import com.example.ui.theme.GeoBluePrimary
import com.example.ui.theme.GeoBorderLight
import com.example.ui.theme.GeoIconCircleBg
import com.example.ui.theme.GeoIconCircleText
import com.example.ui.theme.GeoLightCanvas
import com.example.ui.theme.GeoOnBlueContainer
import com.example.ui.theme.GeoSurfaceLight
import com.example.ui.theme.GeoSurfaceVariant
import com.example.ui.theme.GeoTextPrimary
import com.example.ui.theme.GeoTextSecondary
import com.example.ui.theme.GeoTextTertiary
import com.example.ui.theme.LedMatrixAmber
import com.example.ui.theme.LedMatrixCyan
import com.example.ui.theme.LedMatrixGreen

@Composable
fun SmartDisplayScreen(
  viewModel: SmartDisplayViewModel,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()
  val focusManager = LocalFocusManager.current
  val scrollState = rememberScrollState()

  Scaffold(
    containerColor = GeoLightCanvas,
    contentWindowInsets = WindowInsets(0.dp)
  ) { innerPadding ->
    Box(
      modifier = modifier
        .fillMaxSize()
        .background(GeoLightCanvas)
        .padding(innerPadding),
      contentAlignment = Alignment.TopCenter
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .widthIn(max = 600.dp)
          .verticalScroll(scrollState)
          .padding(horizontal = 20.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
      ) {

        // 1. Top College Header with Logo and "Kuppam Engineering College"
        KecCollegeTopHeader()

        // 2. App Title Card
        KecAppTitleCard()

        // 3. Crisp White Connection Card
        GeometricConnectionCard(
          ipAddress = uiState.ipAddress,
          onIpChanged = { viewModel.onIpAddressChanged(it) },
          onDone = { focusManager.clearFocus() }
        )

        // 4. Crisp White Content Control Card
        GeometricContentControlCard(
          message = uiState.messageText,
          onMessageChanged = { viewModel.onMessageTextChanged(it) },
          onSendMessage = {
            focusManager.clearFocus()
            viewModel.sendMessage()
          },
          onShowSensor = {
            focusManager.clearFocus()
            viewModel.showSensorData()
          },
          isExecuting = uiState.isExecuting,
          currentMode = uiState.currentMode
        )

        // 5. Network Status Card with crisp light blue background and clear alert pills
        GeometricStatusSection(
          statusMessage = uiState.statusMessage,
          isSuccess = uiState.isSuccessStatus,
          isExecuting = uiState.isExecuting,
          lastResponseBody = uiState.lastResponseBody,
          commandLogs = uiState.commandLogs,
          onClearLogs = { viewModel.clearLogs() }
        )

        // 6. Bottom Footer: Designed by "Dept. of ECE"
        KecDepartmentFooter()

        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }
}

/**
 * P10 2-Panel LED Matrix Simulation Card
 */
@Composable
fun P10MatrixPreviewCard(
  mode: DisplayMode,
  activeContent: String,
  isExecuting: Boolean,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .shadow(elevation = 2.dp, shape = RoundedCornerShape(24.dp)),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
  ) {
    Column(
      modifier = Modifier.padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Box(
            modifier = Modifier
              .size(9.dp)
              .clip(CircleShape)
              .background(if (isExecuting) LedMatrixAmber else LedMatrixGreen)
          )
          Text(
            text = "P10 DUAL MATRIX PANEL",
            style = MaterialTheme.typography.labelSmall.copy(
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.5.sp
            ),
            color = Color(0xFFCBD5E1)
          )
        }

        Surface(
          shape = CircleShape,
          color = when (mode) {
            DisplayMode.SCROLLING_TEXT -> GeoBluePrimary
            DisplayMode.SENSOR_TELEMETRY -> LedMatrixAmber
            DisplayMode.UNKNOWN -> Color(0xFF334155)
          }
        ) {
          Text(
            text = when (mode) {
              DisplayMode.SCROLLING_TEXT -> "TEXT MODE"
              DisplayMode.SENSOR_TELEMETRY -> "SENSOR MODE"
              DisplayMode.UNKNOWN -> "STANDBY"
            },
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall.copy(
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp
            ),
            color = if (mode == DisplayMode.SENSOR_TELEMETRY) Color(0xFF0F172A) else Color.White
          )
        }
      }

      // Simulated LED Matrix Screen
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(72.dp)
          .clip(RoundedCornerShape(14.dp))
          .background(Color(0xFF020617))
          .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp))
          .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          if (mode == DisplayMode.SENSOR_TELEMETRY) {
            Icon(
              imageVector = Icons.Default.Thermostat,
              contentDescription = "Temperature icon",
              tint = LedMatrixAmber,
              modifier = Modifier
                .size(24.dp)
                .padding(end = 6.dp)
            )
            Text(
              text = "24.8°C  •  58% RH",
              style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
              ),
              color = LedMatrixAmber
            )
          } else {
            Text(
              text = activeContent.ifEmpty { "ESP8266 SMART DISPLAY" },
              style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
              ),
              color = if (isExecuting) LedMatrixAmber else LedMatrixCyan,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }
    }
  }
}

/**
 * Geometric Connection Card on Pure White Surface with Crisp Navy/Slate text
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GeometricConnectionCard(
  ipAddress: String,
  onIpChanged: (String) -> Unit,
  onDone: () -> Unit,
  modifier: Modifier = Modifier
) {
  var isDropdownExpanded by remember { mutableStateOf(false) }
  val subnetPresets = remember {
    listOf(
      "192.168.4.1" to "192.168.4.1 (ESP AP Mode)",
      "192.168.1." to "192.168.1.x (Class C Subnet)",
      "192.168.0." to "192.168.0.x (Home Subnet)",
      "10.0.0." to "10.0.0.x (Class A Subnet)"
    )
  }
  val matchingPreset = subnetPresets.find { (prefix, _) -> ipAddress.startsWith(prefix) }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .shadow(elevation = 2.dp, shape = RoundedCornerShape(24.dp)),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = GeoSurfaceLight),
    border = androidx.compose.foundation.BorderStroke(1.dp, GeoBorderLight)
  ) {
    Column(
      modifier = Modifier.padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      OutlinedTextField(
        value = ipAddress,
        onValueChange = onIpChanged,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("ip_address_input"),
        placeholder = {
          Text(
            "192.168.1.45 or 192.168.4.1",
            color = GeoTextTertiary
          )
        },
        label = {
          Text(
            "Host IP Address",
            fontWeight = FontWeight.SemiBold
          )
        },
        textStyle = TextStyle(
          color = GeoTextPrimary,
          fontSize = 16.sp,
          fontWeight = FontWeight.Medium,
          fontFamily = FontFamily.Monospace
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.Uri,
          imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        leadingIcon = {
          Icon(
            imageVector = Icons.Default.Wifi,
            contentDescription = "WiFi",
            tint = GeoBluePrimary
          )
        },
        trailingIcon = {
          if (ipAddress.isNotEmpty()) {
            IconButton(onClick = { onIpChanged("") }) {
              Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Clear IP",
                tint = GeoTextSecondary
              )
            }
          }
        },
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = GeoBluePrimary,
          unfocusedBorderColor = GeoBorderLight,
          focusedContainerColor = GeoSurfaceLight,
          unfocusedContainerColor = GeoSurfaceVariant,
          focusedLabelColor = GeoBluePrimary,
          unfocusedLabelColor = GeoTextSecondary,
          focusedTextColor = GeoTextPrimary,
          unfocusedTextColor = GeoTextPrimary
        )
      )

      // Quick subnet presets dropdown
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
          text = "Quick Subnet Presets:",
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
          color = GeoTextSecondary
        )

        ExposedDropdownMenuBox(
          expanded = isDropdownExpanded,
          onExpandedChange = { isDropdownExpanded = it },
          modifier = Modifier.fillMaxWidth()
        ) {
          OutlinedTextField(
            value = matchingPreset?.second ?: "Select a Subnet Preset...",
            onValueChange = {},
            readOnly = true,
            shape = RoundedCornerShape(12.dp),
            trailingIcon = {
              ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded)
            },
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = GeoSurfaceVariant,
              unfocusedContainerColor = GeoSurfaceVariant,
              focusedBorderColor = GeoBluePrimary,
              unfocusedBorderColor = GeoBorderLight,
              focusedTextColor = GeoTextPrimary,
              unfocusedTextColor = GeoTextPrimary
            ),
            textStyle = TextStyle(
              fontSize = 14.sp,
              fontWeight = FontWeight.Medium,
              color = GeoTextPrimary
            ),
            modifier = Modifier
              .fillMaxWidth()
              .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
              .testTag("subnet_preset_dropdown")
          )

          ExposedDropdownMenu(
            expanded = isDropdownExpanded,
            onDismissRequest = { isDropdownExpanded = false },
            containerColor = GeoSurfaceLight
          ) {
            subnetPresets.forEach { (prefix, label) ->
              val isSelected = ipAddress.startsWith(prefix)
              DropdownMenuItem(
                text = {
                  Column {
                    Text(
                      text = label,
                      style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) GeoBluePrimary else GeoTextPrimary
                      )
                    )
                    Text(
                      text = "Prefix: $prefix",
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = GeoTextSecondary
                      )
                    )
                  }
                },
                leadingIcon = {
                  Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = null,
                    tint = if (isSelected) GeoBluePrimary else GeoTextSecondary,
                    modifier = Modifier.size(18.dp)
                  )
                },
                onClick = {
                  onIpChanged(prefix)
                  isDropdownExpanded = false
                },
                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
              )
            }
          }
        }
      }
    }
  }
}

/**
 * Geometric Content Control Card on Pure White Surface with High Contrast Buttons
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GeometricContentControlCard(
  message: String,
  onMessageChanged: (String) -> Unit,
  onSendMessage: () -> Unit,
  onShowSensor: () -> Unit,
  isExecuting: Boolean,
  currentMode: DisplayMode,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .shadow(elevation = 2.dp, shape = RoundedCornerShape(24.dp)),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = GeoSurfaceLight),
    border = androidx.compose.foundation.BorderStroke(1.dp, GeoBorderLight)
  ) {
    Column(
      modifier = Modifier.padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Input field for scrolling text
      OutlinedTextField(
        value = message,
        onValueChange = { input ->
          if (input.length <= 500) {
            onMessageChanged(input)
          } else {
            onMessageChanged(input.take(500))
          }
        },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("message_input"),
        placeholder = {
          Text(
            "Enter scrolling text (max 500 chars)...",
            color = GeoTextTertiary
          )
        },
        label = {
          Text(
            "Display Message",
            fontWeight = FontWeight.SemiBold
          )
        },
        textStyle = TextStyle(
          color = GeoTextPrimary,
          fontSize = 16.sp,
          fontWeight = FontWeight.Medium
        ),
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.Text,
          imeAction = ImeAction.Send
        ),
        keyboardActions = KeyboardActions(onSend = { onSendMessage() }),
        trailingIcon = {
          if (message.isNotEmpty()) {
            IconButton(onClick = { onMessageChanged("") }) {
              Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Clear message",
                tint = GeoTextSecondary
              )
            }
          }
        },
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = GeoBluePrimary,
          unfocusedBorderColor = GeoBorderLight,
          focusedContainerColor = GeoSurfaceLight,
          unfocusedContainerColor = GeoSurfaceVariant,
          focusedLabelColor = GeoBluePrimary,
          unfocusedLabelColor = GeoTextSecondary,
          focusedTextColor = GeoTextPrimary,
          unfocusedTextColor = GeoTextPrimary
        ),
        supportingText = {
          Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Character Count: ${message.length} / 500",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace,
                  fontSize = 12.sp
                ),
                color = if (message.length >= 500) Color(0xFFEF4444) else GeoBluePrimary
              )
              Text(
                text = "${500 - message.length} remaining",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Medium,
                  fontSize = 11.sp
                ),
                color = GeoTextSecondary
              )
            }
            if (message.isNotEmpty()) {
              Text(
                text = "GET /message?text=${message.take(24)}${if (message.length > 24) "..." else ""}",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Normal,
                  fontSize = 10.sp,
                  fontFamily = FontFamily.Monospace
                ),
                color = GeoTextTertiary
              )
            }
          }
        }
      )

      // Primary Action: "Send Message" Button (High-Contrast Solid Blue Pill)
      Button(
        onClick = onSendMessage,
        enabled = !isExecuting && message.isNotBlank(),
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .shadow(elevation = 2.dp, shape = CircleShape)
          .testTag("send_message_button"),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
          containerColor = GeoBluePrimary,
          contentColor = GeoBlueOnPrimary,
          disabledContainerColor = GeoBluePrimary.copy(alpha = 0.4f),
          disabledContentColor = Color.White.copy(alpha = 0.8f)
        )
      ) {
        if (isExecuting) {
          CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            color = GeoBlueOnPrimary,
            strokeWidth = 2.5.dp
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text("Sending...", fontWeight = FontWeight.Bold, color = Color.White)
        } else {
          Icon(
            imageVector = Icons.Default.Send,
            contentDescription = "Send",
            tint = Color.White,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Send Message",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Color.White
          )
        }
      }
    }
  }
}

/**
 * Geometric Status Card with clear, high-contrast badges & collapsible transmission logs
 */
@Composable
fun GeometricStatusSection(
  statusMessage: String,
  isSuccess: Boolean?,
  isExecuting: Boolean,
  lastResponseBody: String?,
  commandLogs: List<CommandLog>,
  onClearLogs: () -> Unit,
  modifier: Modifier = Modifier
) {
  var showDetails by remember { mutableStateOf(false) }

  val badgeBg = when {
    isExecuting -> Color(0xFFEFF6FF)
    isSuccess == true -> Color(0xFFDCFCE7)
    isSuccess == false -> Color(0xFFFEE2E2)
    else -> GeoSurfaceVariant
  }

  val badgeBorder = when {
    isExecuting -> Color(0xFFBFDBFE)
    isSuccess == true -> Color(0xFF86EFAC)
    isSuccess == false -> Color(0xFFFCA5A5)
    else -> GeoBorderLight
  }

  val badgeTextColor = when {
    isExecuting -> Color(0xFF1E3A8A)
    isSuccess == true -> Color(0xFF14532D)
    isSuccess == false -> Color(0xFF991B1B)
    else -> GeoTextPrimary
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .shadow(elevation = 2.dp, shape = RoundedCornerShape(24.dp)),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = GeoSurfaceLight),
    border = androidx.compose.foundation.BorderStroke(1.dp, GeoBorderLight)
  ) {
    Column(
      modifier = Modifier.padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Status",
            tint = GeoBluePrimary,
            modifier = Modifier.size(20.dp)
          )
          Text(
            text = "NETWORK STATUS",
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.2.sp,
              color = GeoTextPrimary
            )
          )
        }

        if (commandLogs.isNotEmpty()) {
          TextButton(onClick = { showDetails = !showDetails }) {
            Text(
              if (showDetails) "Hide Logs" else "Logs (${commandLogs.size})",
              color = GeoBluePrimary,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      // High-contrast Status Banner
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = badgeBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, badgeBorder)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          if (isExecuting) {
            CircularProgressIndicator(
              modifier = Modifier.size(18.dp),
              color = badgeTextColor,
              strokeWidth = 2.5.dp
            )
          } else {
            Icon(
              imageVector = when (isSuccess) {
                true -> Icons.Default.CheckCircle
                false -> Icons.Default.Error
                null -> Icons.Default.Info
              },
              contentDescription = "Status icon",
              tint = badgeTextColor,
              modifier = Modifier.size(20.dp)
            )
          }

          Text(
            text = statusMessage,
            modifier = Modifier.testTag("status_text"),
            style = MaterialTheme.typography.bodyMedium.copy(
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            ),
            color = badgeTextColor
          )
        }
      }

      // Server Response Body Banner
      if (!lastResponseBody.isNullOrEmpty() && isSuccess == true) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = GeoSurfaceVariant,
          border = androidx.compose.foundation.BorderStroke(1.dp, GeoBorderLight),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text(
              text = "NodeMCU HTTP Response:",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = GeoTextSecondary
            )
            Text(
              text = lastResponseBody,
              style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium
              ),
              color = GeoTextPrimary,
              modifier = Modifier.padding(top = 2.dp)
            )
          }
        }
      }

      // Expandable Transmission Logs
      AnimatedVisibility(visible = showDetails && commandLogs.isNotEmpty()) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Recent Transmissions:",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = GeoTextPrimary
              )
            )
            TextButton(onClick = onClearLogs) {
              Text("Clear Logs", color = GeoBluePrimary, style = MaterialTheme.typography.labelSmall)
            }
          }

          commandLogs.take(5).forEach { log ->
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = if (log.isSuccess) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (log.isSuccess) Color(0xFF86EFAC) else Color(0xFFFCA5A5)
              ),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "${log.timestamp} • ${log.command}",
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = FontWeight.Bold,
                      color = if (log.isSuccess) Color(0xFF14532D) else Color(0xFF991B1B)
                    )
                  )
                  Text(
                    text = log.statusMessage,
                    style = MaterialTheme.typography.bodySmall.copy(
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Medium
                    ),
                    color = GeoTextPrimary
                  )
                }

                if (log.latencyMs != null) {
                  Text(
                    text = "${log.latencyMs}ms",
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontFamily = FontFamily.Monospace,
                      fontWeight = FontWeight.Bold,
                      color = GeoTextSecondary
                    )
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

/**
 * Top Header with Kuppam Engineering College Emblem and Title
 */
@Composable
fun KecCollegeTopHeader(
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .shadow(elevation = 2.dp, shape = RoundedCornerShape(24.dp)),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = GeoSurfaceLight),
    border = androidx.compose.foundation.BorderStroke(1.dp, GeoBorderLight)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // College Emblem Logo
      Box(
        modifier = Modifier
          .size(64.dp)
          .clip(CircleShape)
          .background(GeoSurfaceVariant)
          .border(1.5.dp, GeoBlueContainer, CircleShape)
          .padding(3.dp),
        contentAlignment = Alignment.Center
      ) {
        Image(
          painter = painterResource(id = R.drawable.ic_kec_logo),
          contentDescription = "Kuppam Engineering College Logo",
          modifier = Modifier.fillMaxSize()
        )
      }

      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Text(
          text = "Kuppam Engineering College",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            letterSpacing = (-0.2).sp
          ),
          color = GeoBluePrimary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        Text(
          text = "KES Nagar, Kuppam-517425, AP.",
          style = MaterialTheme.typography.bodySmall.copy(
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
          ),
          color = GeoTextSecondary
        )
      }
    }
  }
}

/**
 * Bottom Footer: "Designed by Dept. of ECE"
 */
@Composable
fun KecDepartmentFooter(
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .shadow(elevation = 1.dp, shape = RoundedCornerShape(20.dp)),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = GeoSurfaceLight),
    border = androidx.compose.foundation.BorderStroke(1.dp, GeoBorderLight)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 18.dp, vertical = 14.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Box(
          modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(GeoBlueContainer),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Memory,
            contentDescription = "ECE Hardware Icon",
            tint = GeoBluePrimary,
            modifier = Modifier.size(16.dp)
          )
        }

        Text(
          text = "Designed by R&D Team",
          style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            letterSpacing = 0.2.sp
          ),
          color = GeoTextPrimary
        )
      }
    }
  }
}

/**
 * Division card explicitly displaying the APP title "KEC Smart Display Link APP"
 */
@Composable
fun KecAppTitleCard(
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .shadow(elevation = 1.dp, shape = RoundedCornerShape(20.dp))
      .testTag("kec_app_title_card"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = GeoSurfaceLight),
    border = androidx.compose.foundation.BorderStroke(1.dp, GeoBorderLight)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 18.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .background(GeoBlueContainer),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Wifi,
          contentDescription = "App Icon",
          tint = GeoBluePrimary,
          modifier = Modifier.size(22.dp)
        )
      }

      Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
          text = "KEC Smart Display Link APP",
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            letterSpacing = (-0.2).sp
          ),
          color = GeoBluePrimary
        )
      }
    }
  }
}

