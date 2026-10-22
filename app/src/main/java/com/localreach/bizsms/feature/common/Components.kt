package com.localreach.bizsms.feature.common

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun MetricStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    subtext: String? = null
) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            if (subtext != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtext,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (color, text) = when (status) {
        "DELIVERED" -> MaterialTheme.colorScheme.primary to "DELIVERED"
        "SENT" -> MaterialTheme.colorScheme.tertiary to "SENT"
        "SUBMITTED" -> MaterialTheme.colorScheme.secondary to "SUBMITTED"
        "HANDED_OFF_TO_MESSAGING_APP" -> MaterialTheme.colorScheme.secondary to "HANDED OFF"
        "FAILED" -> MaterialTheme.colorScheme.error to "FAILED"
        "UNKNOWN" -> MaterialTheme.colorScheme.errorContainer to "UNKNOWN"
        "CANCELLED" -> MaterialTheme.colorScheme.outline to "CANCELLED"
        "GRANTED", "OPTED_IN" -> MaterialTheme.colorScheme.primary to "OPTED IN"
        "OPTED_OUT", "SUPPRESSED" -> MaterialTheme.colorScheme.error to "SUPPRESSED"
        else -> MaterialTheme.colorScheme.surfaceVariant to status
    }

    Surface(
        color = color.copy(alpha = 0.15f),
        shape = MaterialTheme.shapes.small,
        modifier = modifier
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
