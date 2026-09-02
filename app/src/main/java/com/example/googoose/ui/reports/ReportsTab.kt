package com.example.googoose.ui.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.googoose.data.Strings
import com.example.googoose.data.model.TxnType
import com.example.googoose.ui.components.CardKicker
import com.example.googoose.ui.components.GooGooseCard
import com.example.googoose.ui.components.SegmentedControl
import com.example.googoose.ui.theme.GooGooseColors
import com.example.googoose.ui.theme.GooGooseType
import com.example.googoose.util.currencySymbol
import com.example.googoose.viewmodel.GooGooseLogic
import com.example.googoose.viewmodel.GooGooseUiState
import com.example.googoose.viewmodel.GooGooseViewModel
import com.example.googoose.viewmodel.ReportRange

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReportsTab(state: GooGooseUiState, strings: Strings, viewModel: GooGooseViewModel, modifier: Modifier = Modifier) {
    val currency = currencySymbol(state.settingsCurrency)
    val summary = GooGooseLogic.rangeSummary(state.transactions, state.reportRange, currencySymbol = currency)
    val breakdown = GooGooseLogic.categoryBreakdown(
        transactions = state.transactions,
        type = state.reportCategoryType,
        range = state.reportRange,
        excludedCategories = state.excludedReportCategories,
        currencySymbol = currency,
        otherLabel = strings.otherCategory,
    )
    val weekBars = GooGooseLogic.weekBars(state.transactions, currencySymbol = currency, language = state.language)
    val warningDays = weekBars.filter { it.isWarning }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        if (state.transactions.isEmpty()) {
            item {
                Text(strings.emptyReports, style = GooGooseType.bodySmall, color = GooGooseColors.textMuted)
            }
        }
        item {
            SegmentedControl(
                options = listOf(
                    ReportRange.ALL to strings.all,
                    ReportRange.MONTH_30D to strings.reportRangeMonth,
                    ReportRange.WEEK_7D to strings.reportRangeWeek,
                    ReportRange.DAY_TODAY to strings.reportRangeDay,
                ),
                selected = state.reportRange,
                onSelect = viewModel::setReportRange,
            )
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard(strings.income, summary.incomeLabel, GooGooseColors.text, Modifier.weight(1f))
                StatCard(strings.spend, summary.spendLabel, GooGooseColors.text, Modifier.weight(1f))
                StatCard(strings.net, summary.netLabel, GooGooseColors.accent, Modifier.weight(1f))
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    if (state.reportCategoryType == TxnType.SPEND) strings.spendByCategory else strings.incomeByCategory,
                    style = GooGooseType.h5,
                    color = GooGooseColors.text,
                )
                SegmentedControl(
                    options = listOf(TxnType.SPEND to strings.spend, TxnType.INCOME to strings.income),
                    selected = state.reportCategoryType,
                    onSelect = viewModel::setReportCategoryType,
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    state.categories.forEach { cat ->
                        CategoryToggleChip(
                            name = cat,
                            selected = cat !in state.excludedReportCategories,
                            onClick = { viewModel.toggleReportCategory(cat) },
                        )
                    }
                }
                if (breakdown.isEmpty()) {
                    Text(strings.noCategoryData, style = GooGooseType.caption, color = GooGooseColors.textMuted)
                } else {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CategoryPieChart(rows = breakdown)
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        breakdown.forEach { row ->
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Box(modifier = Modifier.width(8.dp).height(8.dp).clip(RoundedCornerShape(2.dp)).background(row.color))
                                        Text(row.category, style = GooGooseType.caption.copy(fontSize = 12.sp), color = GooGooseColors.text)
                                    }
                                    Text(row.amountLabel, style = GooGooseType.caption.copy(fontSize = 12.sp), color = GooGooseColors.textMuted)
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(7.dp)
                                        .clip(RoundedCornerShape(100.dp))
                                        .background(GooGooseColors.progressTrack),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(row.barPct / 100f)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(100.dp))
                                            .background(row.color),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(strings.last7, style = GooGooseType.h5, color = GooGooseColors.text)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    weekBars.forEach { bar ->
                        Column(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                        ) {
                            Text(
                                bar.netLabel,
                                style = GooGooseType.caption.copy(fontSize = 9.sp),
                                color = if (bar.isWarning) GooGooseColors.error else GooGooseColors.textMuted,
                            )
                            Spacer(Modifier.height(3.dp))
                            Box(
                                modifier = Modifier
                                    .width(22.dp)
                                    .height(bar.heightDp.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (bar.isNegative) GooGooseColors.negativeBar else GooGooseColors.positiveBar),
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(bar.label, style = GooGooseType.caption.copy(fontSize = 10.sp), color = GooGooseColors.textMuted)
                        }
                    }
                }
                if (warningDays.isNotEmpty()) {
                    Text(
                        strings.lowCashFlowWarning(warningDays.joinToString(", ") { it.shortDate }),
                        style = GooGooseType.caption,
                        color = GooGooseColors.error,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    GooGooseCard(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        CardKicker(label)
        Text(value, style = GooGooseType.bodySmall.copy(fontSize = 15.sp, fontWeight = FontWeight.Medium), color = valueColor)
    }
}

@Composable
private fun CategoryToggleChip(name: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) GooGooseColors.tagAccentBg else Color.Transparent)
            .border(1.dp, if (selected) GooGooseColors.accent else GooGooseColors.divider, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(name, style = GooGooseType.tag, color = if (selected) GooGooseColors.tagAccentFg else GooGooseColors.textMuted)
    }
}
