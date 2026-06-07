package io.github.bengidev.openzone.settings

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
import io.github.bengidev.openzone.settings.application.SettingsComponent
import io.github.bengidev.openzone.settings.presenter.SettingsView
import io.github.bengidev.openzone.settings.theme.OpenZoneSettingsTheme
import io.github.bengidev.openzone.settings.theme.SettingsTheme

/**
 * Settings presented surface. Opens from the Home top bar as its own screen
 * (mirrors the iOS presented Settings sheet). Hosts [SettingsView] under a
 * back/close top bar. All colors flow from the authoritative palette via
 * [OpenZoneSettingsTheme].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    component: SettingsComponent,
    darkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val state by component.state.subscribeAsState()

    OpenZoneSettingsTheme(darkTheme = darkTheme) {
        val palette = SettingsTheme.palette
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = palette.background,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Settings",
                            color = palette.textPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = component::onCloseTapped) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Close settings",
                                tint = palette.textPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = palette.background,
                        navigationIconContentColor = palette.textPrimary
                    ),
                    expandedHeight = 52.dp
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                SettingsView(
                    state = state,
                    onApiKeyDraftChanged = component::onApiKeyDraftChanged,
                    onSaveApiKey = component::onSaveApiKey,
                    onClearApiKey = component::onClearApiKey,
                    onProviderSelected = component::onProviderSelected,
                    onModelSelected = component::onModelSelected
                )
            }
        }
    }
}
