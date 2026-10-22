package com.apnasandesh.app.feature.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apnasandesh.app.core.ui.theme.*

/**
 * High-performance, production-grade stat card for dashboards and overview screens.
 */
@Composable
fun MetricStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    subtext: String? = null,
    subtextColor: Color? = null,
    accentColor: Color? = null
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (icon != null) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                color = (accentColor ?: MaterialTheme.colorScheme.primary).copy(alpha = 0.12f),
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor ?: MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = accentColor ?: MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtext != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtext,
                    style = MaterialTheme.typography.labelSmall,
                    color = subtextColor ?: MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Human-designed Status Badge with crisp semantic color mapping and accessible contrast.
 */
@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background == BackgroundDark

    val (bgColor, textColor, label) = when (status) {
        "DELIVERED" -> Triple(
            if (isDark) StatusSuccessContainerDark else StatusSuccessContainer,
            if (isDark) StatusSuccessDark else StatusSuccess,
            "Delivered"
        )
        "SENT" -> Triple(
            if (isDark) StatusSuccessContainerDark else StatusSuccessContainer,
            if (isDark) StatusSuccessDark else StatusSuccess,
            "Sent"
        )
        "SUBMITTED", "IN_PROGRESS" -> Triple(
            if (isDark) StatusInfoContainerDark else StatusInfoContainer,
            if (isDark) StatusInfoDark else StatusInfo,
            "In Progress"
        )
        "HANDED_OFF_TO_MESSAGING_APP" -> Triple(
            if (isDark) StatusInfoContainerDark else StatusInfoContainer,
            if (isDark) StatusInfoDark else StatusInfo,
            "Handed Off"
        )
        "AWAITING_CONFIRMATION", "QUEUED", "DRAFT", "VALIDATED" -> Triple(
            if (isDark) BrandBluePrimaryContainerDark.copy(alpha = 0.4f) else BrandBluePrimaryContainerLight.copy(alpha = 0.6f),
            if (isDark) BrandBluePrimaryDark else BrandBluePrimaryLight,
            status.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
        )
        "COMPLETED" -> Triple(
            if (isDark) StatusSuccessContainerDark else StatusSuccessContainer,
            if (isDark) StatusSuccessDark else StatusSuccess,
            "Completed"
        )
        "GRANTED", "OPTED_IN" -> Triple(
            if (isDark) StatusSuccessContainerDark else StatusSuccessContainer,
            if (isDark) StatusSuccessDark else StatusSuccess,
            "Opted-In"
        )
        "UNKNOWN" -> Triple(
            if (isDark) StatusWarningContainerDark else StatusWarningContainer,
            if (isDark) StatusWarningDark else StatusWarning,
            "Unknown"
        )
        "OPTED_OUT", "SUPPRESSED" -> Triple(
            if (isDark) StatusErrorContainerDark else StatusErrorContainer,
            if (isDark) StatusErrorDark else StatusError,
            "Suppressed"
        )
        "FAILED", "INVALID" -> Triple(
            if (isDark) StatusErrorContainerDark else StatusErrorContainer,
            if (isDark) StatusErrorDark else StatusError,
            "Failed"
        )
        "CANCELLED" -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            "Cancelled"
        )
        else -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            status.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
        )
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
        )
    }
}

/**
 * Reusable, human-crafted Empty State with helpful title, descriptive context, and CTA button.
 * Supports custom vector illustrations as well as icon fallbacks.
 */
@Composable
fun EmptyStateView(
    icon: ImageVector = Icons.Default.Info,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    illustrationRes: Int? = null,
    actionButtonLabel: String? = null,
    onActionButtonClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            if (illustrationRes != null) {
                Image(
                    painter = painterResource(id = illustrationRes),
                    contentDescription = null,
                    modifier = Modifier.size(110.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (actionButtonLabel != null && onActionButtonClick != null) {
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onActionButtonClick,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(actionButtonLabel, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

/**
 * Avatar with initial badge and consistent, pleasant pseudo-random colors.
 */
@Composable
fun CustomerAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Int = 40
) {
    val initials = remember(name) {
        val parts = name.trim().split("\\s+".toRegex())
        when {
            parts.isEmpty() || parts[0].isBlank() -> "C"
            parts.size == 1 -> parts[0].take(2).uppercase()
            else -> "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
        }
    }

    val avatarPalette = listOf(
        Color(0xFF2563EB), // Blue
        Color(0xFF0D9488), // Teal
        Color(0xFF7C3AED), // Purple
        Color(0xFFD97706), // Amber
        Color(0xFF059669), // Emerald
        Color(0xFFE11D48)  // Rose
    )
    val color = remember(name) {
        val hash = kotlin.math.abs(name.hashCode())
        avatarPalette[hash % avatarPalette.size]
    }

    Box(
        modifier = modifier
            .size(size.dp)
            .background(color.copy(alpha = 0.15f), shape = CircleShape)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = color,
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = (size * 0.38f).sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

/**
 * Step Wizard Bar for multi-stage workflows (e.g. Campaign Editor -> Recipients -> Review).
 */
@Composable
fun StepWizardBar(
    steps: List<String>,
    currentStepIndex: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, stepName ->
            val isCompleted = index < currentStepIndex
            val isActive = index == currentStepIndex

            val circleColor = when {
                isCompleted -> MaterialTheme.colorScheme.primary
                isActive -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
            val textColor = when {
                isActive -> MaterialTheme.colorScheme.primary
                isCompleted -> MaterialTheme.colorScheme.onSurface
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            color = circleColor,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    } else {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stepName,
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (index < steps.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 6.dp),
                    color = if (index < currentStepIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                    thickness = 2.dp
                )
            }
        }
    }
}

/**
 * Rich, grouped chip tray for dynamic placeholder tokens.
 */
@Composable
fun TokenChipTray(
    onTokenSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedGroup by remember { mutableStateOf("All") }
    val groups = remember { listOf("All", "Customer", "Business", "Sales", "Orders", "Appointments", "General") }

    val allVariables = remember { com.apnasandesh.app.domain.validation.TemplateRenderer.ALL_VARIABLES }
    val displayedVariables = remember(selectedGroup) {
        if (selectedGroup == "All") allVariables
        else allVariables.filter { it.group.equals(selectedGroup, ignoreCase = true) }
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Insert Personalized Variable:",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${displayedVariables.size} available",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Group selector row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            groups.forEach { group ->
                val isSelected = selectedGroup == group
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedGroup = group },
                    label = { Text(group, style = MaterialTheme.typography.labelSmall) },
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        // Variables chips horizontal scroll
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            displayedVariables.forEach { variable ->
                TokenChip(
                    label = variable.displayName,
                    token = variable.token,
                    onClick = onTokenSelected
                )
            }
        }
    }
}

@Composable
private fun TokenChip(
    label: String,
    token: String,
    onClick: (String) -> Unit
) {
    SuggestionChip(
        onClick = { onClick(token) },
        label = {
            Text(
                text = "+ $label",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium
            )
        },
        shape = RoundedCornerShape(8.dp),
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            labelColor = MaterialTheme.colorScheme.primary
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    )
}


/**
 * Realistic mobile SMS Bubble Preview with metadata pill.
 */
@Composable
fun SmsBubblePreview(
    messageText: String,
    encoding: String,
    charCount: Int,
    segmentCount: Int,
    modifier: Modifier = Modifier,
    recipientName: String? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (recipientName != null) "Message Preview for $recipientName" else "SMS Preview",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Text(
                        text = if (encoding.contains("UCS", ignoreCase = true)) "UCS-2 (Unicode)" else "GSM-7",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (encoding.contains("UCS", ignoreCase = true)) BrandAmberTertiaryLight else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Realistic Chat Bubble
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                shape = RoundedCornerShape(
                    topStart = 14.dp,
                    topEnd = 14.dp,
                    bottomEnd = 4.dp,
                    bottomStart = 14.dp
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = messageText.ifBlank { "Type your message to see a live preview..." },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Character and segment metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$charCount characters",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$segmentCount SMS segment${if (segmentCount > 1) "s" else ""}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (segmentCount > 1) BrandAmberTertiaryLight else MaterialTheme.colorScheme.primary,
                    maxLines = 1
                )
            }
        }
    }
}
