package io.github.bengidev.openzone.home.presenter

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.bengidev.openzone.home.theme.HomeTheme

/** Top bar with sidebar toggle and new-conversation action — mirrors iOS `HomeView.topBar`. */
@Composable
fun HomeTopBar(
        onSidebarToggle: () -> Unit,
        onNewConversationTapped: () -> Unit,
        modifier: Modifier = Modifier
) {
    val palette = HomeTheme.palette

    Row(
            modifier =
                    modifier.fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onSidebarToggle, modifier = Modifier.size(44.dp)) {
            Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Show sidebar",
                    tint = palette.textPrimary,
                    modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        IconButton(onClick = onNewConversationTapped, modifier = Modifier.size(44.dp)) {
            Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New conversation",
                    tint = palette.textPrimary,
                    modifier = Modifier.size(22.dp)
            )
        }
    }
}
