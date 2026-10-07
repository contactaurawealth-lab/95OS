package com.os95.app.core.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.os95.app.core.ui.theme.OS95Theme

/**
 * Data model for actions rendered inside dropdown or overflow menus.
 */
data class OS95MenuAction(
    val label: String,
    val icon: ImageVector? = null,
    val isSelected: Boolean = false,
    val isDestructive: Boolean = false,
    val enabled: Boolean = true,
    val onClick: () -> Unit
)

/**
 * Minimalist, dark-obsidian styled dropdown menu container adhering to 95OS design system.
 */
@Composable
fun OS95DropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset(0.dp, 4.dp),
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    val colors = OS95Theme.colors
    val shapes = OS95Theme.shapes

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        offset = offset,
        modifier = modifier
            .background(colors.surface, shape = shapes.medium)
            .border(1.dp, colors.border, shape = shapes.medium),
        containerColor = colors.surface,
        content = content
    )
}

/**
 * Individual menu item with 44dp minimum touch target, optional icon, and active selection state.
 */
@Composable
fun OS95DropdownMenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingText: String? = null,
    isSelected: Boolean = false,
    enabled: Boolean = true,
    isDestructive: Boolean = false
) {
    val colors = OS95Theme.colors
    val typography = OS95Theme.typography
    val spacing = OS95Theme.spacing

    DropdownMenuItem(
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = text,
                    style = typography.caption.copy(
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    ),
                    color = when {
                        !enabled -> colors.mutedText
                        isDestructive -> colors.error
                        isSelected -> colors.accent
                        else -> colors.primaryText
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (trailingText != null) {
                    Spacer(modifier = Modifier.width(spacing.s))
                    Text(
                        text = trailingText,
                        style = typography.caption.copy(fontSize = 11.sp),
                        color = colors.mutedText
                    )
                }
            }
        },
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = spacing.minTouchTarget),
        leadingIcon = leadingIcon?.let { icon ->
            {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = when {
                        !enabled -> colors.mutedText
                        isDestructive -> colors.error
                        isSelected -> colors.accent
                        else -> colors.secondaryText
                    }
                )
            }
        },
        trailingIcon = if (isSelected) {
            {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    modifier = Modifier.size(16.dp),
                    tint = colors.accent
                )
            }
        } else null,
        enabled = enabled,
        colors = MenuDefaults.itemColors(
            textColor = colors.primaryText,
            leadingIconColor = colors.secondaryText,
            trailingIconColor = colors.accent,
            disabledTextColor = colors.mutedText,
            disabledLeadingIconColor = colors.mutedText
        ),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
    )
}

/**
 * Reusable generic dropdown selector showing an outlined field with label, selected item,
 * animated toggle arrow, and an expandable item list.
 */
@Composable
fun <T> OS95DropdownSelector(
    selectedValue: T?,
    items: List<T>,
    itemLabel: (T) -> String,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "Select...",
    enabled: Boolean = true,
    itemIcon: ((T) -> ImageVector?)? = null,
    itemTrailingText: ((T) -> String?)? = null
) {
    var expanded by remember { mutableStateOf(false) }
    val colors = OS95Theme.colors
    val typography = OS95Theme.typography
    val shapes = OS95Theme.shapes
    val spacing = OS95Theme.spacing
    val rotationAngle by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "arrowRotation"
    )

    Column(modifier = modifier) {
        if (label != null) {
            Text(
                text = label,
                style = typography.caption,
                color = colors.mutedText,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = spacing.minTouchTarget)
                    .clip(shapes.medium)
                    .clickable(enabled = enabled) { expanded = true }
                    .semantics { role = Role.DropdownList },
                shape = shapes.medium,
                color = colors.surface,
                border = BorderStroke(1.dp, if (expanded) colors.accent else colors.border)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val icon = if (selectedValue != null) itemIcon?.invoke(selectedValue) else null
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = colors.accentCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(spacing.s))
                    }

                    Text(
                        text = if (selectedValue != null) itemLabel(selectedValue) else placeholder,
                        style = typography.caption.copy(fontWeight = FontWeight.Medium),
                        color = if (selectedValue != null) colors.primaryText else colors.mutedText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(spacing.s))

                    Icon(
                        imageVector = Icons.Outlined.ArrowDropDown,
                        contentDescription = "Toggle dropdown",
                        tint = if (enabled) colors.secondaryText else colors.mutedText,
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(rotationAngle)
                    )
                }
            }

            OS95DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                items.forEach { item ->
                    val isSelected = item == selectedValue
                    OS95DropdownMenuItem(
                        text = itemLabel(item),
                        leadingIcon = itemIcon?.invoke(item),
                        trailingText = itemTrailingText?.invoke(item),
                        isSelected = isSelected,
                        onClick = {
                            expanded = false
                            onItemSelected(item)
                        }
                    )
                }
            }
        }
    }
}

/**
 * Standard 3-dot overflow menu button rendering a dropdown list of contextual actions.
 */
@Composable
fun OS95OverflowMenu(
    actions: List<OS95MenuAction>,
    modifier: Modifier = Modifier,
    contentDescription: String = "More options"
) {
    if (actions.isEmpty()) return
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.wrapContentSize(Alignment.TopEnd)) {
        OS95IconButton(
            icon = Icons.Outlined.MoreVert,
            contentDescription = contentDescription,
            onClick = { expanded = true }
        )

        OS95DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            actions.forEach { action ->
                OS95DropdownMenuItem(
                    text = action.label,
                    leadingIcon = action.icon,
                    isSelected = action.isSelected,
                    isDestructive = action.isDestructive,
                    enabled = action.enabled,
                    onClick = {
                        expanded = false
                        action.onClick()
                    }
                )
            }
        }
    }
}
