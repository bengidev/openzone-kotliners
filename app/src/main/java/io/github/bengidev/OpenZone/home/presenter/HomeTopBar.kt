package io.github.bengidev.openzone.home.presenter

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.bengidev.openzone.home.theme.HomeTheme

/** Top bar with sidebar toggle and new-conversation action — mirrors iOS `HomeView.topBar`. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
        onSidebarToggle: () -> Unit,
        onNewConversationTapped: () -> Unit,
        modifier: Modifier = Modifier
) {
    val palette = HomeTheme.palette

    TopAppBar(
            modifier = modifier,
            title = {},
            navigationIcon = {
                IconButton(onClick = onSidebarToggle) {
                    Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Show sidebar",
                            tint = palette.textPrimary
                    )
                }
            },
            actions = {
                IconButton(onClick = onNewConversationTapped) {
                    Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New conversation",
                            tint = palette.textPrimary
                    )
                }
            },
            colors =
                    TopAppBarDefaults.topAppBarColors(
                            containerColor = palette.background,
                            navigationIconContentColor = palette.textPrimary,
                            actionIconContentColor = palette.textPrimary
                    ),
            expandedHeight = 52.dp
    )
}
