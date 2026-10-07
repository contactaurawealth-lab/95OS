package com.os95.app.core.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.os95.app.core.ui.theme.OS95Theme

import androidx.compose.material.icons.outlined.Menu
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Composition local providing a lambda to open the global navigation drawer.
 */
val LocalDrawerOpener = staticCompositionLocalOf<(() -> Unit)?> { null }

data class OS95NavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

@Composable
fun OS95TopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    onOpenDrawer: (() -> Unit)? = null,
    overflowActions: List<OS95MenuAction> = emptyList(),
    actions: @Composable RowScope.() -> Unit = {}
) {
    val colors = OS95Theme.colors
    val typography = OS95Theme.typography
    val spacing = OS95Theme.spacing
    val drawerOpener = LocalDrawerOpener.current

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colors.background,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                OS95IconButton(
                    icon = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Navigate Back",
                    onClick = onBack
                )
                Spacer(modifier = Modifier.width(spacing.s))
            } else if (onOpenDrawer != null || drawerOpener != null) {
                OS95IconButton(
                    icon = Icons.Outlined.Menu,
                    contentDescription = "Open Navigation Drawer",
                    onClick = { (onOpenDrawer ?: drawerOpener)?.invoke() }
                )
                Spacer(modifier = Modifier.width(spacing.s))
            }

            Box(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = typography.sectionTitle,
                        color = colors.primaryText
                    )
                    if (subtitle != null) {
                        Spacer(modifier = Modifier.width(spacing.s))
                        Text(
                            text = "• $subtitle",
                            style = typography.caption,
                            color = colors.mutedText
                        )
                    }
                }
            }

            actions()

            if (overflowActions.isNotEmpty()) {
                OS95OverflowMenu(actions = overflowActions)
            }
        }
    }
}

@Composable
fun OS95BottomBar(
    items: List<OS95NavItem>,
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = OS95Theme.colors
    val typography = OS95Theme.typography

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.border)
    ) {
        NavigationBar(
            containerColor = colors.surface,
            contentColor = colors.primaryText,
            tonalElevation = 0.dp,
            modifier = Modifier.height(64.dp)
        ) {
            items.forEach { item ->
                val selected = currentRoute == item.route
                NavigationBarItem(
                    selected = selected,
                    onClick = { onNavigate(item.route) },
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = item.title,
                            style = typography.caption,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = colors.accent,
                        selectedTextColor = colors.accent,
                        indicatorColor = colors.cardBackground,
                        unselectedIconColor = colors.secondaryText,
                        unselectedTextColor = colors.secondaryText
                    )
                )
            }
        }
    }
}
