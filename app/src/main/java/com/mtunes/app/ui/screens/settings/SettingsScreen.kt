package com.mtunes.app.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.mtunes.app.ui.theme.CyberGreen
import com.mtunes.app.ui.theme.CyberGreenDim
import com.mtunes.app.ui.theme.DarkCard
import com.mtunes.app.ui.theme.DarkSurfaceVariant
import com.mtunes.app.ui.theme.PureBlack
import com.mtunes.app.ui.theme.TextGray

@Composable
fun SettingsScreen() {
    var cacheLimit by remember { mutableFloatStateOf(2f) }
    var uploadLimit by remember { mutableFloatStateOf(5f) }
    var securityEnabled by remember { mutableStateOf(true) }
    var autoSeedEnabled by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        contentPadding = PaddingValues(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineLarge,
                color = CyberGreen
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Storage
        item {
            SettingsSection(title = "Storage", icon = Icons.Filled.Storage) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Cache Limit", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "${cacheLimit.toInt()} GB",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CyberGreen
                        )
                    }
                    Slider(
                        value = cacheLimit,
                        onValueChange = { cacheLimit = it },
                        valueRange = 1f..10f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = CyberGreen,
                            activeTrackColor = CyberGreen,
                            inactiveTrackColor = DarkSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Used: 0.3 GB / ${cacheLimit.toInt()} GB", style = MaterialTheme.typography.bodySmall, color = TextGray)
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = 0.03f / cacheLimit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = CyberGreen,
                        trackColor = DarkSurfaceVariant
                    )
                }
            }
        }

        // Upload
        item {
            SettingsSection(title = "Upload", icon = Icons.Filled.CloudUpload) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Upload Limit", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "${uploadLimit.toInt()} MB/s",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CyberGreen
                        )
                    }
                    Slider(
                        value = uploadLimit,
                        onValueChange = { uploadLimit = it },
                        valueRange = 1f..20f,
                        colors = SliderDefaults.colors(
                            thumbColor = CyberGreen,
                            activeTrackColor = CyberGreen,
                            inactiveTrackColor = DarkSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SettingsToggle(
                        title = "Auto-Seed",
                        subtitle = "Share downloaded music with peers",
                        checked = autoSeedEnabled,
                        onCheckedChange = { autoSeedEnabled = it }
                    )
                }
            }
        }

        // Security
        item {
            SettingsSection(title = "Security", icon = Icons.Filled.Security) {
                Column {
                    SettingsToggle(
                        title = "File Validation",
                        subtitle = "Block non-audio and suspicious files",
                        checked = securityEnabled,
                        onCheckedChange = { securityEnabled = it }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Allowed: MP3, FLAC, AAC, M4A, OGG, WAV",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGray
                    )
                }
            }
        }

        // Cache Management
        item {
            SettingsSection(title = "Cache", icon = Icons.Filled.DeleteSweep) {
                Column {
                    Text(
                        "Cached songs: 0",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextGray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Total size: 0 MB",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGray
                    )
                }
            }
        }

        // About
        item {
            SettingsSection(title = "About", icon = Icons.Filled.Info) {
                Column {
                    Text("MTunes v1.0.0", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Decentralized P2P Music Streaming",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGray
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = CyberGreen, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.size(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, color = CyberGreen)
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SettingsToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextGray)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PureBlack,
                checkedTrackColor = CyberGreen,
                uncheckedThumbColor = TextGray,
                uncheckedTrackColor = DarkSurfaceVariant
            )
        )
    }
}
