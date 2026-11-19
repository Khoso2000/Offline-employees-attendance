package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.local.entity.ActivityLogEntity
import com.example.data.local.entity.EmployeeEntity
import com.example.data.local.entity.LeaveRequestEntity
import com.example.data.model.AttendanceStatus
import com.example.data.model.LeaveStatus
import com.example.data.model.PaymentStatus
import com.example.data.model.ThemeMode
import com.example.domain.DateUtils
import com.example.ui.state.DailyChartPoint
import com.example.ui.theme.AbsentRed
import com.example.ui.theme.AbsentRedContainerDark
import com.example.ui.theme.AbsentRedContainerLight
import com.example.ui.theme.HalfDayOrange
import com.example.ui.theme.HalfDayOrangeContainerDark
import com.example.ui.theme.HalfDayOrangeContainerLight
import com.example.ui.theme.InfoCyan
import com.example.ui.theme.InfoCyanContainerDark
import com.example.ui.theme.InfoCyanContainerLight
import com.example.ui.theme.LateAmber
import com.example.ui.theme.LateAmberContainerDark
import com.example.ui.theme.LateAmberContainerLight
import com.example.ui.theme.LeavePurple
import com.example.ui.theme.LeavePurpleContainerDark
import com.example.ui.theme.LeavePurpleContainerLight
import com.example.ui.theme.PresentGreen
import com.example.ui.theme.PresentGreenContainerDark
import com.example.ui.theme.PresentGreenContainerLight
import java.io.File

@Composable
fun attendanceStatusColors(status: AttendanceStatus, themeMode: ThemeMode = ThemeMode.SYSTEM): Pair<Color, Color> {
    val isDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    return when (status) {
        AttendanceStatus.PRESENT -> PresentGreen to if (isDark) PresentGreenContainerDark else PresentGreenContainerLight
        AttendanceStatus.ABSENT -> AbsentRed to if (isDark) AbsentRedContainerDark else AbsentRedContainerLight
        AttendanceStatus.HALF_DAY -> HalfDayOrange to if (isDark) HalfDayOrangeContainerDark else HalfDayOrangeContainerLight
        AttendanceStatus.ON_LEAVE -> LeavePurple to if (isDark) LeavePurpleContainerDark else LeavePurpleContainerLight
        AttendanceStatus.LATE -> LateAmber to if (isDark) LateAmberContainerDark else LateAmberContainerLight
        AttendanceStatus.HOLIDAY -> InfoCyan to if (isDark) InfoCyanContainerDark else InfoCyanContainerLight
    }
}

@Composable
fun SaasCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    val border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    val colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    val elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            border = border,
            colors = colors,
            elevation = elevation,
            content = content
        )
    } else {
        Card(
            modifier = modifier,
            shape = shape,
            border = border,
            colors = colors,
            elevation = elevation,
            content = content
        )
    }
}

@Composable
fun ModuleShortcutCard(
    title: String,
    subtitle: String,
    countText: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.22f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            accentColor.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Surface(
                    color = accentColor.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = countText,
                        color = accentColor,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

@Composable
fun StatusBadge(
    text: String,
    fgColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = bgColor,
        shape = RoundedCornerShape(9.dp)
    ) {
        Text(
            text = text,
            color = fgColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun AttendanceBadge(status: AttendanceStatus, themeMode: ThemeMode = ThemeMode.SYSTEM) {
    val (fg, bg) = attendanceStatusColors(status, themeMode)
    StatusBadge(text = status.label, fgColor = fg, bgColor = bg)
}

@Composable
fun LeaveStatusBadge(status: LeaveStatus) {
    val (fg, bg) = when (status) {
        LeaveStatus.APPROVED -> PresentGreen to PresentGreen.copy(alpha = 0.14f)
        LeaveStatus.PENDING -> HalfDayOrange to HalfDayOrange.copy(alpha = 0.14f)
        LeaveStatus.REJECTED -> AbsentRed to AbsentRed.copy(alpha = 0.14f)
    }
    StatusBadge(text = status.label, fgColor = fg, bgColor = bg)
}

@Composable
fun PaymentStatusBadge(status: PaymentStatus) {
    val (fg, bg) = when (status) {
        PaymentStatus.PAID -> PresentGreen to PresentGreen.copy(alpha = 0.14f)
        PaymentStatus.PARTIAL -> HalfDayOrange to HalfDayOrange.copy(alpha = 0.14f)
        PaymentStatus.UNPAID -> AbsentRed to AbsentRed.copy(alpha = 0.14f)
    }
    StatusBadge(text = status.label, fgColor = fg, bgColor = bg)
}

@Composable
fun EmployeeAvatar(
    fullName: String,
    photoUri: String?,
    size: Dp = 44.dp,
    modifier: Modifier = Modifier
) {
    val initials = fullName
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "ST" }

    val model: Any? = photoUri?.takeIf { it.isNotBlank() }?.let { path ->
        if (path.startsWith("/")) File(path) else path
    }

    if (model != null) {
        AsyncImage(
            model = model,
            contentDescription = "$fullName photo",
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .border(1.5.dp, MaterialTheme.colorScheme.outline, CircleShape)
        )
    } else {
        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun OnlineSavedPills(
    isOnline: Boolean,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val netBg by animateColorAsState(
        targetValue = if (isOnline) PresentGreen.copy(alpha = 0.14f) else HalfDayOrange.copy(alpha = 0.16f),
        label = "net_bg"
    )
    val netFg = if (isOnline) PresentGreen else HalfDayOrange

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(color = netBg, shape = RoundedCornerShape(20.dp)) {
            Row(
                modifier = Modifier.padding(
                    horizontal = if (compact) 7.dp else 8.dp,
                    vertical = 4.dp
                ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = if (isOnline) Icons.Rounded.CloudDone else Icons.Rounded.CloudOff,
                    contentDescription = if (isOnline) "Online and saved locally" else "Offline mode, saved locally",
                    tint = netFg,
                    modifier = Modifier.size(14.dp)
                )
                if (!compact) {
                    Text(
                        text = if (isOnline) "Online" else "Offline",
                        color = netFg,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        if (!compact) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Save,
                        contentDescription = "Saved locally",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Saved Locally",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun KpiCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    SaasCard(modifier = modifier, onClick = onClick) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun MonthlyAttendanceBarChart(
    points: List<DailyChartPoint>,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(140.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No attendance data recorded this month yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val maxTotal = points.maxOf { it.present + it.late + it.halfDay + it.leave + it.absent }.coerceAtLeast(1)
    val gridColor = MaterialTheme.colorScheme.outlineVariant

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(148.dp)
        ) {
            val w = size.width
            val h = size.height
            for (i in 0..3) {
                val y = h * (i / 3f)
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1f
                )
            }

            val slotWidth = w / points.size
            val barWidth = (slotWidth * 0.54f).coerceIn(8f, 26f)

            points.forEachIndexed { idx, pt ->
                val x = idx * slotWidth + (slotWidth - barWidth) / 2f
                val segments = listOf(
                    pt.present to PresentGreen,
                    pt.late to HalfDayOrange,
                    pt.halfDay to InfoCyan,
                    pt.leave to LeavePurple,
                    pt.absent to AbsentRed
                )
                var currentBottom = h
                segments.forEach { (count, color) ->
                    if (count > 0) {
                        val segHeight = (count.toFloat() / maxTotal) * (h - 6f)
                        drawRoundRect(
                            color = color,
                            topLeft = Offset(x, currentBottom - segHeight),
                            size = Size(barWidth, segHeight),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                        currentBottom -= segHeight
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            points.forEach { pt ->
                Text(
                    text = pt.dayLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ChartLegendItem("Present", PresentGreen)
            ChartLegendItem("Late", HalfDayOrange)
            ChartLegendItem("Half", InfoCyan)
            ChartLegendItem("Leave", LeavePurple)
            ChartLegendItem("Absent", AbsentRed)
        }
    }
}

@Composable
private fun ChartLegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun TodayDonutChart(
    present: Int,
    late: Int,
    halfDay: Int,
    leave: Int,
    absent: Int,
    totalActive: Int,
    modifier: Modifier = Modifier
) {
    val markedTotal = (present + late + halfDay + leave + absent).coerceAtLeast(1)
    val trackColor = Color.White.copy(alpha = 0.22f)
    val segments = listOf(
        present to PresentGreen,
        late to HalfDayOrange,
        halfDay to InfoCyan,
        leave to LeavePurple,
        absent to AbsentRed
    )

    Box(modifier = modifier.size(106.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(98.dp)) {
            val stroke = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Butt)
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = stroke
            )
            var startAngle = -90f
            val totalForArc = if (present + late + halfDay + leave + absent == 0) 1 else markedTotal
            segments.forEach { (count, color) ->
                if (count > 0) {
                    val sweep = (count.toFloat() / totalForArc) * 360f
                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = stroke
                    )
                    startAngle += sweep
                }
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val pct = if (totalActive > 0) ((present + late + halfDay) * 100) / totalActive else 0
            Text(
                text = "$pct%",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Active",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
fun ConfirmActionDialog(
    title: String,
    message: String,
    confirmLabel: String,
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = if (isDestructive) AbsentRed else MaterialTheme.colorScheme.primary
            )
        },
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(message, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = if (isDestructive) {
                    ButtonDefaults.buttonColors(containerColor = AbsentRed, contentColor = Color.White)
                } else {
                    ButtonDefaults.buttonColors()
                }
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun NotificationsDialog(
    pendingLeaves: List<LeaveRequestEntity>,
    employees: List<EmployeeEntity>,
    recentLogs: List<ActivityLogEntity>,
    onReviewLeaves: () -> Unit,
    onDismiss: () -> Unit
) {
    val empMap = employees.associateBy { it.id }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Rounded.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text("Notifications & Alerts", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (pendingLeaves.isNotEmpty()) {
                    Text("Pending Leave Requests (${pendingLeaves.size})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = HalfDayOrange)
                    pendingLeaves.forEach { lv ->
                        val empName = empMap[lv.employeeId]?.fullName ?: "Employee #${lv.employeeId}"
                        Text("• $empName requested ${lv.category.label} (${lv.totalDays}d)", style = MaterialTheme.typography.bodySmall)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                }
                Text("Recent Activity", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                recentLogs.take(5).forEach { log ->
                    Column {
                        Text(log.title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        Text(
                            "${log.description} • ${DateUtils.formatTimestamp(log.timestamp)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (pendingLeaves.isNotEmpty()) {
                Button(onClick = onReviewLeaves) { Text("Review Leaves") }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (trailing != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailing()
        }
    }
}
