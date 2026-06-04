package io.github.bengidev.openzone.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import io.github.bengidev.openzone.home.application.HomeComponent
import io.github.bengidev.openzone.home.presenter.HomeComposerView
import io.github.bengidev.openzone.home.presenter.HomeTopBar
import io.github.bengidev.openzone.home.presenter.HomeWelcomeView
import io.github.bengidev.openzone.home.presenter.clearFocusOnTapOutside
import io.github.bengidev.openzone.home.theme.HomeTheme
import io.github.bengidev.openzone.home.theme.OpenZoneHomeTheme

/** Home shell — iOS HomeRootView / MainChat welcome layout. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    component: HomeComponent,
    modifier: Modifier = Modifier
) {
    val state by component.state.subscribeAsState()

    OpenZoneHomeTheme {
        Box(
            modifier = modifier
                .fillMaxSize()
                .clearFocusOnTapOutside()
        ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            containerColor = HomeTheme.palette.background,
            topBar = {
                HomeTopBar(onSidebarToggle = component::onSidebarToggleTapped)
            },
            bottomBar = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    HomeComposerView(
                        state = state,
                        onDraftMessageChanged = component::onDraftMessageChanged,
                        onSendTapped = component::onSendTapped,
                        onAttachmentTapped = component::onAttachmentTapped,
                        onMicrophoneTapped = component::onMicrophoneTapped,
                        onModelSelected = component::onModelSelected,
                        onReasoningLevelSelected = component::onReasoningLevelSelected,
                        onSpeedModeSelected = component::onSpeedModeSelected,
                        onContextUsageTapped = component::onContextUsageTapped,
                        onContextUsageDismissed = component::onContextUsageDismissed,
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 620.dp)
                            .padding(bottom = 10.dp)
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                HomeWelcomeView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 680.dp)
                        .padding(horizontal = 8.dp)
                )
            }
        }
        }
    }
}
