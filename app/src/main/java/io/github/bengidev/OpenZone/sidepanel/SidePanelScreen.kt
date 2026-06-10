package io.github.bengidev.openzone.sidepanel

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import io.github.bengidev.openzone.home.theme.HomeTheme
import io.github.bengidev.openzone.settings.SettingsScreen
import io.github.bengidev.openzone.settings.application.SettingsComponent
import io.github.bengidev.openzone.sidepanel.application.SidePanelSessionComponent

/**
 * Side panel surface — mirrors iOS `SidePanelView` in that it presents either
 * the settings sheet or the session sidebar. In the Android layout, the session
 * sidebar stays overlaid inside [HomeScreen]; settings is presented as a full
 * screen overlay.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SidePanelSettingsOverlay(
    settingsComponent: SettingsComponent?,
    darkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    if (settingsComponent == null) return
    SettingsScreen(
        component = settingsComponent,
        darkTheme = darkTheme,
        modifier = modifier.fillMaxSize()
    )
}
