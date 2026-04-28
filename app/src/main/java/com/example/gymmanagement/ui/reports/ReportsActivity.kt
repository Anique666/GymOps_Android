package com.example.gymmanagement.ui.reports

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.gymmanagement.R
import com.example.gymmanagement.data.local.model.LabelCount
import com.example.gymmanagement.data.local.model.ReportRange
import com.example.gymmanagement.data.local.model.RevenuePoint
import com.example.gymmanagement.ui.common.AppBottomBar
import com.example.gymmanagement.ui.common.BottomNavHelper

@OptIn(ExperimentalMaterial3Api::class)
class ReportsActivity : AppCompatActivity() {

    private val viewModel: ReportsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            MaterialTheme(colorScheme = reportsColorScheme()) {
                val colors = MaterialTheme.colorScheme
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Column {
                                    Text("Performance Insights", fontWeight = FontWeight.Bold)
                                    Text("Analytics period", fontSize = 12.sp, color = colors.onSurfaceVariant)
                                }
                            },
                            navigationIcon = {
                                Box(
                                    modifier = Modifier
                                        .padding(start = 12.dp)
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(colors.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("M", color = colors.onPrimary, fontWeight = FontWeight.Bold)
                                }
                            },
                            actions = {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.padding(end = 16.dp)
                                )
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
                        )
                    },
                    bottomBar = {
                        AppBottomBar(selectedItemId = R.id.navReports) { itemId ->
                            BottomNavHelper.navigate(this@ReportsActivity, R.id.navReports, itemId)
                        }
                    },
                    containerColor = colors.background
                ) { padding ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            RangeSelector(
                                selected = uiState.selectedRange,
                                onSelected = viewModel::setRange
                            )
                        }

                        item {
                            RevenueHeaderCard(
                                totalRevenue = uiState.snapshot.totalRevenue,
                                trendPercent = uiState.snapshot.revenueTrendPercent,
                                revenueSeries = uiState.snapshot.revenueSeries
                            )
                        }

                        item {
                            SectionHeader("Revenue & Payments")
                            RevenueSection(
                                series = uiState.snapshot.revenueSeries,
                                paymentDistribution = uiState.snapshot.paymentDistribution
                            )
                        }

                        item {
                            SectionHeader("Membership Trends")
                            MembershipTrendsSection(
                                newMembers = uiState.snapshot.newMembers,
                                expiredMembers = uiState.snapshot.expiredMembers,
                                renewedMembers = uiState.snapshot.renewedMembers
                            )
                        }

                        item {
                            SectionHeader("Retention & Churn")
                            RetentionSection(
                                retentionRate = uiState.snapshot.retentionRate,
                                churnRate = uiState.snapshot.churnRate
                            )
                        }

                        item {
                            SectionHeader("Expiry & Risk")
                            ExpiryRiskSection(uiState.snapshot.expiringSoon)
                        }

                        item {
                            SectionHeader("Demographics")
                            DemographicsSection(
                                genderDistribution = uiState.snapshot.genderDistribution,
                                ageGroups = uiState.snapshot.ageGroups
                            )
                        }

                        item {
                            InsightCard(
                                title = "Revenue insight",
                                message = if (uiState.snapshot.revenueTrendPercent >= 0) {
                                    "Revenue is up ${formatPercent(uiState.snapshot.revenueTrendPercent)} versus the previous period."
                                } else {
                                    "Revenue is down ${formatPercent(kotlin.math.abs(uiState.snapshot.revenueTrendPercent))}; review stalled renewals."
                                },
                                background = if (uiState.snapshot.revenueTrendPercent >= 0) Color(0xFFDFF4EA) else Color(0xFFFDE0D2),
                                accent = if (uiState.snapshot.revenueTrendPercent >= 0) Color(0xFF0A7B58) else Color(0xFFA54B00)
                            )
                        }

                        item { Spacer(modifier = Modifier.height(12.dp)) }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RangeSelector(selected: ReportRange, onSelected: (ReportRange) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        listOf(ReportRange.TODAY, ReportRange.WEEK, ReportRange.MONTH).forEach { range ->
            FilterChip(
                selected = selected == range,
                onClick = { onSelected(range) },
                label = {
                    Text(
                        when (range) {
                            ReportRange.TODAY -> "Today"
                            ReportRange.WEEK -> "This Week"
                            ReportRange.MONTH -> "This Month"
                        }
                    )
                }
            )
        }
    }
}

@Composable
private fun RevenueHeaderCard(totalRevenue: Double, trendPercent: Double, revenueSeries: List<RevenuePoint>) {
    val colors = MaterialTheme.colorScheme
    Card(colors = CardDefaults.cardColors(containerColor = colors.surface), shape = RoundedCornerShape(28.dp)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("Total Revenue", color = colors.onSurfaceVariant, fontSize = 14.sp)
            Row(verticalAlignment = Alignment.Bottom) {
                Text("₹${totalRevenue.toInt()}", fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, color = colors.primary)
                Spacer(modifier = Modifier.width(10.dp))
                Text("${formatPercent(trendPercent)}", color = if (trendPercent >= 0) Color(0xFF47C696) else Color(0xFFF28B82))
            }
            Spacer(modifier = Modifier.height(12.dp))
            LineChart(series = if (revenueSeries.isEmpty()) demoLine(totalRevenue) else revenueSeries)
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    val colors = MaterialTheme.colorScheme
    Text(title, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = colors.primary, modifier = Modifier.padding(top = 4.dp, bottom = 4.dp))
}

@Composable
private fun RevenueSection(series: List<RevenuePoint>, paymentDistribution: List<LabelCount>) {
    val colors = MaterialTheme.colorScheme
    Card(colors = CardDefaults.cardColors(containerColor = colors.surface), shape = RoundedCornerShape(24.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            BarChart(series = if (series.isEmpty()) demoBars() else series)
            DistributionBar(items = paymentDistribution)
        }
    }
}

@Composable
private fun MembershipTrendsSection(newMembers: Int, expiredMembers: Int, renewedMembers: Int) {
    val colors = MaterialTheme.colorScheme
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        TrendTile("New", newMembers.toString(), colors.secondaryContainer, colors.onSecondaryContainer, Modifier.weight(1f))
        TrendTile("Expired", expiredMembers.toString(), colors.errorContainer, colors.onErrorContainer, Modifier.weight(1f))
        TrendTile("Renewed", renewedMembers.toString(), colors.tertiaryContainer, colors.onTertiaryContainer, Modifier.weight(1f))
    }
}

@Composable
private fun RetentionSection(retentionRate: Double, churnRate: Double) {
    val colors = MaterialTheme.colorScheme
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        MetricCard("Retention Rate", "${retentionRate.toInt()}%", colors.onSecondaryContainer, colors.secondaryContainer, Modifier.weight(1f))
        MetricCard("Churn Rate", "${churnRate.toInt()}%", colors.onErrorContainer, colors.errorContainer, Modifier.weight(1f))
    }
}

@Composable
private fun ExpiryRiskSection(expiringSoon: Int) {
    val colors = MaterialTheme.colorScheme
    Card(colors = CardDefaults.cardColors(containerColor = colors.surface), shape = RoundedCornerShape(24.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Expiring soon", color = colors.onSurfaceVariant, fontSize = 12.sp)
                    Text(expiringSoon.toString(), color = colors.primary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                }
                Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(colors.tertiaryContainer), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = colors.tertiary)
                }
            }
            RiskBars()
            InsightCard(
                title = "Churn warning",
                message = "Review memberships nearing expiry and trigger renewal outreach for the most at-risk cohort.",
                background = colors.tertiaryContainer,
                accent = colors.tertiary
            )
        }
    }
}

@Composable
private fun DemographicsSection(genderDistribution: List<LabelCount>, ageGroups: List<LabelCount>) {
    val colors = MaterialTheme.colorScheme
    Card(colors = CardDefaults.cardColors(containerColor = colors.surface), shape = RoundedCornerShape(24.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                DonutChart(
                    slices = genderDistribution.mapIndexed { index, item ->
                        DonutSlice(item.label, item.value.toFloat(), listOf(Color(0xFF0D5A55), Color(0xFFF28C28), Color(0xFFBDBDBD))[index % 3])
                    },
                    modifier = Modifier.weight(1f)
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    genderDistribution.take(3).forEach { item ->
                        DistributionRow(item.label, item.value.toString())
                    }
                }
            }

            val maxAgeCount = ageGroups.take(3).maxOfOrNull { it.value }?.coerceAtLeast(1) ?: 1
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ageGroups.take(3).forEach { item ->
                    AgeRow(item, maxAgeCount)
                }
            }
        }
    }
}

@Composable
private fun TrendTile(title: String, value: String, background: Color, foreground: Color, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = background), shape = RoundedCornerShape(18.dp)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, color = colors.onSurfaceVariant, fontSize = 12.sp)
            Text(value, color = foreground, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, foreground: Color, background: Color, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = background), shape = RoundedCornerShape(18.dp)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, color = colors.onSurfaceVariant, fontSize = 12.sp)
            Text(value, color = foreground, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun InsightCard(title: String, message: String, background: Color, accent: Color) {
    val colors = MaterialTheme.colorScheme
    Card(colors = CardDefaults.cardColors(containerColor = background), shape = RoundedCornerShape(18.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(42.dp).clip(CircleShape).background(accent.copy(alpha = 0.18f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.BarChart, contentDescription = null, tint = accent)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, color = accent)
                Text(message, color = colors.onSurface, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun DistributionBar(items: List<LabelCount>) {
    val colors = MaterialTheme.colorScheme
    val barColors = listOf(Color(0xFF0D5A55), Color(0xFFA54B00), Color(0xFFA8DADC))
    val weights = items.map { it.value.toFloat().coerceAtLeast(0.5f) }
    Row(modifier = Modifier.fillMaxWidth().height(14.dp).clip(RoundedCornerShape(999.dp)).background(colors.surfaceVariant)) {
        if (items.isNotEmpty()) {
            items.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .weight(weights[index])
                        .fillMaxWidth()
                        .background(barColors[index % barColors.size])
                )
            }
        }
    }
    if (items.isNotEmpty()) {
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            items.forEachIndexed { index, item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(barColors[index % barColors.size]))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("${item.label} ${item.value}", fontSize = 11.sp, color = colors.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun DistributionRow(label: String, value: String) {
    val colors = MaterialTheme.colorScheme
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = colors.onSurfaceVariant)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.primary)
    }
}

@Composable
private fun AgeRow(item: LabelCount, maxCount: Int) {
    val colors = MaterialTheme.colorScheme
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(item.label, modifier = Modifier.width(54.dp), fontSize = 12.sp, color = colors.onSurfaceVariant)
        Box(modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(999.dp)).background(colors.surfaceVariant)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(item.value.toFloat() / maxCount.toFloat())
                    .height(6.dp)
                    .background(colors.tertiary)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(item.value.toString(), fontSize = 12.sp, color = colors.primary)
    }
}

@Composable
private fun LineChart(series: List<RevenuePoint>) {
    val colors = MaterialTheme.colorScheme
    val values = if (series.isEmpty()) listOf(12f, 18f, 14f, 26f, 18f, 34f) else series.map { it.value.toFloat() }
    Canvas(modifier = Modifier.fillMaxWidth().height(150.dp)) {
        val max = values.maxOrNull()?.takeIf { it > 0 } ?: 1f
        val stepX = if (values.size <= 1) size.width else size.width / (values.size - 1)
        val points = values.mapIndexed { index, value ->
            Offset(x = index * stepX, y = size.height - ((value / max) * (size.height - 20.dp.toPx())) - 10.dp.toPx())
        }
        for (index in 0 until points.lastIndex) {
            drawLine(colors.primary.copy(alpha = 0.28f), points[index], points[index + 1], strokeWidth = 4f, cap = StrokeCap.Round)
        }
        points.forEach { point -> drawCircle(colors.primary, radius = 6f, center = point) }
    }
}

@Composable
private fun BarChart(series: List<RevenuePoint>) {
    val colors = MaterialTheme.colorScheme
    val values = if (series.isEmpty()) listOf(20f, 34f, 28f, 45f, 60f, 30f) else series.map { it.value.toFloat() }
    Canvas(modifier = Modifier.fillMaxWidth().height(180.dp)) {
        val max = values.maxOrNull()?.takeIf { it > 0 } ?: 1f
        val barWidth = size.width / (values.size * 1.6f)
        val gap = barWidth * 0.6f
        values.forEachIndexed { index, value ->
            val left = index * (barWidth + gap) + gap / 2
            val barHeight = (value / max) * (size.height - 16.dp.toPx())
            drawRoundRect(
                color = if (index == values.lastIndex) colors.tertiary else colors.primary.copy(alpha = 0.78f),
                topLeft = Offset(left, size.height - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(10.dp.toPx())
            )
        }
    }
}

@Composable
private fun DonutChart(slices: List<DonutSlice>, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val total = slices.sumOf { it.value.toDouble() }.toFloat().coerceAtLeast(1f)
    Canvas(modifier = modifier.size(128.dp)) {
        var startAngle = -90f
        val stroke = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Butt)
        slices.forEach { slice ->
            val sweep = (slice.value / total) * 360f
            drawArc(color = slice.color, startAngle = startAngle, sweepAngle = sweep, useCenter = false, style = stroke)
            startAngle += sweep
        }
        drawCircle(color = colors.background, radius = size.minDimension / 2.8f)
    }
}

@Composable
private fun RiskBars() {
    val colors = MaterialTheme.colorScheme
    Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
        listOf(0.4f, 0.62f, 0.88f, 0.5f).forEach { heightFactor ->
            Box(modifier = Modifier.width(14.dp).height((56 * heightFactor).dp).clip(RoundedCornerShape(999.dp)).background(colors.primary.copy(alpha = 0.65f)))
        }
    }
}

private data class DonutSlice(val label: String, val value: Float, val color: Color)

private fun demoLine(totalRevenue: Double): List<RevenuePoint> = listOf(
    RevenuePoint("Mon", totalRevenue * 0.32),
    RevenuePoint("Tue", totalRevenue * 0.48),
    RevenuePoint("Wed", totalRevenue * 0.41),
    RevenuePoint("Thu", totalRevenue * 0.67),
    RevenuePoint("Fri", totalRevenue * 0.54),
    RevenuePoint("Sat", totalRevenue * 0.76)
)

private fun demoBars(): List<RevenuePoint> = listOf(
    RevenuePoint("Jan", 18.0),
    RevenuePoint("Feb", 24.0),
    RevenuePoint("Mar", 22.0),
    RevenuePoint("Apr", 31.0),
    RevenuePoint("May", 42.0),
    RevenuePoint("Jun", 28.0)
)

private fun formatPercent(value: Double): String = "${kotlin.math.abs(value).toInt()}%"

@Composable
private fun reportsColorScheme() = if (isSystemInDarkTheme()) {
    darkColorScheme(
        primary = Color(0xFF7ED2C8),
        onPrimary = Color(0xFF062B2A),
        primaryContainer = Color(0xFF1C3B38),
        onPrimaryContainer = Color(0xFFBCEBE5),
        secondary = Color(0xFFFFB66C),
        onSecondary = Color(0xFF111316),
        secondaryContainer = Color(0xFF46301A),
        onSecondaryContainer = Color(0xFFFFDCB8),
        tertiary = Color(0xFFA54B00),
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFF3D2A14),
        onTertiaryContainer = Color(0xFFFFD9B9),
        background = Color(0xFF111316),
        onBackground = Color(0xFFECEFF2),
        surface = Color(0xFF1A1D20),
        onSurface = Color(0xFFECEFF2),
        surfaceVariant = Color(0xFF24282D),
        onSurfaceVariant = Color(0xFFB6BEC7),
        error = Color(0xFFF28B82),
        errorContainer = Color(0xFF4A2321),
        onErrorContainer = Color(0xFFFFDAD6)
    )
} else {
    lightColorScheme(
        primary = Color(0xFF0A4642),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFD9E2E2),
        onPrimaryContainer = Color(0xFF062B2A),
        secondary = Color(0xFFF28C28),
        onSecondary = Color.Black,
        secondaryContainer = Color(0xFFEFF5F4),
        onSecondaryContainer = Color(0xFF0A4642),
        tertiary = Color(0xFFA54B00),
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFFDEAD9),
        onTertiaryContainer = Color(0xFF6E3600),
        background = Color(0xFFECEDEE),
        onBackground = Color(0xFF111316),
        surface = Color.White,
        onSurface = Color(0xFF111316),
        surfaceVariant = Color(0xFFE8EDEE),
        onSurfaceVariant = Color(0xFF5F6368),
        error = Color(0xFFB3261E),
        errorContainer = Color(0xFFFFEBEE),
        onErrorContainer = Color(0xFFB3261E)
    )
}