package com.example.gymmanagement.data.local.model

enum class ReportRange {
    TODAY,
    WEEK,
    MONTH
}

enum class PaymentMethod {
    CASH,
    UPI,
    CARD
}

enum class PaymentStatus {
    PAID,
    PENDING,
    FAILED
}

data class LabelCount(
    val label: String,
    val value: Int
)

data class RevenuePoint(
    val label: String,
    val value: Double
)

data class ReportsSnapshot(
    val totalRevenue: Double = 0.0,
    val revenueTrendPercent: Double = 0.0,
    val revenueSeries: List<RevenuePoint> = emptyList(),
    val paymentDistribution: List<LabelCount> = emptyList(),
    val newMembers: Int = 0,
    val expiredMembers: Int = 0,
    val renewedMembers: Int = 0,
    val retentionRate: Double = 0.0,
    val churnRate: Double = 0.0,
    val expiringSoon: Int = 0,
    val genderDistribution: List<LabelCount> = emptyList(),
    val ageGroups: List<LabelCount> = emptyList()
)

data class ReportsUiState(
    val selectedRange: ReportRange = ReportRange.TODAY,
    val snapshot: ReportsSnapshot = ReportsSnapshot(),
    val loading: Boolean = true
)