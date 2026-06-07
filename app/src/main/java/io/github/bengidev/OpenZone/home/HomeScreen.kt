package io.github.bengidev.openzone.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import io.github.bengidev.openzone.chat.presenter.ChatThreadView
import io.github.bengidev.openzone.chat.theme.OpenZoneChatTheme
import io.github.bengidev.openzone.home.application.HomeComponent
import io.github.bengidev.openzone.home.presenter.HomeComposerView
import io.github.bengidev.openzone.home.presenter.HomeTopBar
import io.github.bengidev.openzone.home.presenter.HomeWelcomeView
import io.github.bengidev.openzone.home.presenter.clearFocusOnTapOutside
import io.github.bengidev.openzone.home.theme.HomeTheme
import io.github.bengidev.openzone.home.theme.OpenZoneHomeTheme
import io.github.bengidev.openzone.settings.SettingsScreen

/**
 * Home shell — iOS `MainChat` welcome layout. Swaps between welcome
 * hero and chat-thread view based on whether the chat feature has
 * any messages (mirrors iOS `HomeView.showsWelcome`).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    component: HomeComponent,
    darkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val state by component.state.subscribeAsState()
    val chatState by component.chatComponent.state.collectAsState()

    OpenZoneHomeTheme(darkTheme = darkTheme) {
        OpenZoneChatTheme(darkTheme = darkTheme) {
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
                        HomeTopBar(
                            onSidebarToggle = component::onSidebarToggleTapped,
                            onSettingsTapped = component::onSettingsTapped
                        )
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
                                onModelPopupOpen = component::onModelPopupOpen,
                                onModelPopupDismiss = component::onModelPopupDismiss,
                                onModelSearchQueryChanged = component::onModelSearchQueryChanged,
                                onModelFilterFreeOnlyToggled = component::onModelFilterFreeOnlyToggled,
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
                        contentAlignment = Alignment.TopCenter
                    ) {
                        if (chatState.hasMessages) {
                            ChatThreadView(
                                state = chatState,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .widthIn(max = 680.dp)
                            )
                        } else {
                            HomeWelcomeView(
                                isChatConfigured = state.isChatConfigured,
                                onConfigureTapped = component::onSettingsTapped,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .widthIn(max = 680.dp)
                                    .padding(horizontal = 8.dp)
                            )
                        }
                    }
                }

                val settingsComponent = component.settingsComponent
                if (state.isSettingsPresented && settingsComponent != null) {
                    SettingsScreen(
                        component = settingsComponent,
                        darkTheme = darkTheme,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}


