package io.github.bengidev.openzone.sidepanel.presenter

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import io.github.bengidev.openzone.home.theme.OpenZoneHomeTheme
import io.github.bengidev.openzone.sidepanel.application.SidePanelSettingComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SidePanelSettingScreen(
    component: SidePanelSettingComponent,
    darkTheme: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by component.state.subscribeAsState()

    OpenZoneHomeTheme(darkTheme = darkTheme) {
        val palette = HomeTheme.palette
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = palette.background,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Settings",
                            color = palette.textPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    actions = {
                        IconButton(onClick = {
                            component.onCloseTapped()
                            onDismiss()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Done", tint = palette.textPrimary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = palette.background)
                )
            }
        ) { innerPadding ->
            Box(Modifier.fillMaxSize().padding(innerPadding)) {
                SidePanelSettingView(
                    state = state,
                    onApiKeyDraftChanged = component::onApiKeyDraftChanged,
                    onSaveApiKey = component::onSaveApiKey,
                    onClearApiKey = component::onClearApiKey,
                    onProviderSelected = component::onProviderSelected,
                    onReasoningModelSelected = component::onReasoningModelSelected
                )
            }
        }
    }
}
