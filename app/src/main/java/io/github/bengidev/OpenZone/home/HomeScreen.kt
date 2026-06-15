package io.github.bengidev.openzone.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import io.github.bengidev.openzone.chat.presenter.ChatThreadView
import io.github.bengidev.openzone.chat.theme.OpenZoneChatTheme
import io.github.bengidev.openzone.home.application.HomeComponent
import io.github.bengidev.openzone.home.presenter.HomeComposerView
import io.github.bengidev.openzone.home.presenter.HomeTopBar
import io.github.bengidev.openzone.home.presenter.HomeWelcomeView
import io.github.bengidev.openzone.home.presenter.clearFocusOnTapOutside
import io.github.bengidev.openzone.home.theme.OpenZoneHomeTheme
import io.github.bengidev.openzone.sidepanel.presenter.SidePanelSessionSidebarView
import io.github.bengidev.openzone.sidepanel.presenter.SidePanelSettingScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(component: HomeComponent, darkTheme: Boolean, modifier: Modifier = Modifier) {
    LaunchedEffect(component) { component.onAppear() }

    val state by component.state.subscribeAsState()
    val chatState by component.chatComponent.state.collectAsState()
    val sidePanel = component.sidePanelComponent
    val isSidebarVisible =
            if (sidePanel != null) {
                val sessionState by sidePanel.sessionComponent.state.subscribeAsState()
                sessionState.isSidebarVisible
            } else {
                false
            }

    OpenZoneHomeTheme(darkTheme = darkTheme) {
        OpenZoneChatTheme(darkTheme = darkTheme) {
            Box(modifier = modifier.fillMaxSize()) {
                Scaffold(
                        modifier =
                                Modifier.fillMaxSize()
                                        .navigationBarsPadding()
                                        .then(
                                                if (isSidebarVisible) {
                                                    Modifier.semantics { hideFromAccessibility() }
                                                } else {
                                                    Modifier
                                                }
                                        ),
                        containerColor =
                                io.github.bengidev.openzone.home.theme.HomeTheme.palette.background,
                        topBar = {
                            HomeTopBar(
                                    onSidebarToggle = component::onSidebarToggleTapped,
                                    onNewConversationTapped = component::onNewConversationTapped
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
                                        onConfigureApiKeyTapped = component::onSettingsTapped,
                                        onModelPopupOpen = component::onModelPopupOpen,
                                        onModelPopupDismiss = component::onModelPopupDismiss,
                                        onModelSearchQueryChanged =
                                                component::onModelSearchQueryChanged,
                                        onModelFilterFreeOnlyChanged =
                                                component::onModelFilterFreeOnlyChanged,
                                        onModelSelected = component::onModelSelected,
                                        onReasoningLevelSelected =
                                                component::onReasoningLevelSelected,
                                        onSpeedModeSelected = component::onSpeedModeSelected,
                                        onContextUsageTapped = component::onContextUsageTapped,
                                        onContextUsageDismissed =
                                                component::onContextUsageDismissed,
                                        modifier =
                                                Modifier.fillMaxWidth()
                                                        .widthIn(max = 620.dp)
                                                        .padding(bottom = 10.dp)
                                )
                            }
                        }
                ) { innerPadding ->
                    Box(
                            modifier =
                                    Modifier.fillMaxSize()
                                            .padding(innerPadding)
                                            .clearFocusOnTapOutside(),
                            contentAlignment = Alignment.TopCenter
                    ) {
                        if (chatState.hasMessages) {
                            ChatThreadView(
                                    state = chatState,
                                    modifier =
                                            Modifier.fillMaxWidth()
                                                    .fillMaxHeight()
                                                    .widthIn(max = 680.dp)
                            )
                        } else {
                            HomeWelcomeView(
                                    modifier =
                                            Modifier.fillMaxWidth()
                                                    .fillMaxHeight()
                                                    .widthIn(max = 680.dp)
                                                    .padding(horizontal = 8.dp)
                            )
                        }
                    }
                }

                sidePanel?.let { panel ->
                    val panelState by panel.state.subscribeAsState()
                    if (!panelState.isSettingPresented) {
                        SidePanelSessionSidebarView(
                                component = panel.sessionComponent,
                                modifier = Modifier.fillMaxSize().zIndex(1f)
                        )
                    }
                    panel.setting?.let { settingComponent ->
                        if (panelState.isSettingPresented) {
                            SidePanelSettingScreen(
                                    component = settingComponent,
                                    darkTheme = darkTheme,
                                    onDismiss = panel::dismissSettings,
                                    modifier = Modifier.fillMaxSize().zIndex(2f)
                            )
                        }
                    }
                }
            }
        }
    }
}
