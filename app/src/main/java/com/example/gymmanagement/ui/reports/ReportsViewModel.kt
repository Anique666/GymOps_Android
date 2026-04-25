package com.example.gymmanagement.ui.reports

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymmanagement.data.local.model.ReportRange
import com.example.gymmanagement.data.local.model.ReportsUiState
import com.example.gymmanagement.data.repository.ReportsRepository
import com.example.gymmanagement.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ReportsViewModel(application: Application) : AndroidViewModel(application) {

    private val reportsRepository: ReportsRepository = AppContainer(application).reportsRepository

    private val selectedRange = MutableStateFlow(ReportRange.TODAY)
    private val refreshSignal = MutableStateFlow(System.currentTimeMillis())

    val uiState: StateFlow<ReportsUiState> = combine(selectedRange, refreshSignal) { range, now ->
        range to now
    }.flatMapLatest { (range, now) ->
        val windowStart = when (range) {
            ReportRange.TODAY -> now - DAY_MILLIS
            ReportRange.WEEK -> now - WEEK_MILLIS
            ReportRange.MONTH -> now - MONTH_MILLIS
        }
        reportsRepository.observeReports(range, windowStart, now)
            .map { snapshot -> ReportsUiState(selectedRange = range, snapshot = snapshot, loading = false) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReportsUiState())

    fun setRange(range: ReportRange) {
        selectedRange.value = range
        refresh()
    }

    fun refresh() {
        refreshSignal.value = System.currentTimeMillis()
    }

    companion object {
        private const val DAY_MILLIS = 24L * 60L * 60L * 1000L
        private const val WEEK_MILLIS = 7L * DAY_MILLIS
        private const val MONTH_MILLIS = 30L * DAY_MILLIS
    }
}