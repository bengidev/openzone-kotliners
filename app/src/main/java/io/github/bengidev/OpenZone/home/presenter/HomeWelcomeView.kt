package io.github.bengidev.openzone.home.presenter

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.bengidev.openzone.home.presenter.visuals.HomeParticleOrbView
import io.github.bengidev.openzone.home.theme.HomeTheme
import io.github.bengidev.openzone.onboarding.presenter.components.ScaleToFitText

/** Welcome hero — iOS MainChatWelcomeView. */
@Composable
fun HomeWelcomeView(
    modifier: Modifier = Modifier
) {
    val palette = HomeTheme.palette

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(72.dp))

        HomeParticleOrbView(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .padding(bottom = 28.dp)
        )

        ScaleToFitText(
            text = "Hi! How can I help you?",
            style = HomeTheme.typography.welcomeTitle,
            color = palette.textPrimary,
            maxLines = 1,
            minFontSize = 19.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Text(
            text = "Chats are end-to-end encrypted.",
            style = HomeTheme.typography.welcomeCaption,
            color = palette.textSecondary,
            modifier = Modifier.padding(top = 12.dp),
            textAlign = TextAlign.Center
        )

        Text(
            text = "Your data is safe.",
            style = HomeTheme.typography.welcomeCaption,
            color = palette.textSecondary,
            modifier = Modifier.padding(top = 4.dp),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}
