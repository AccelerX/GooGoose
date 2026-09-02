package com.example.googoose.ui.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.googoose.data.Language
import com.example.googoose.data.Strings
import com.example.googoose.data.model.Transaction
import com.example.googoose.data.model.TxnType
import com.example.googoose.ui.components.CardKicker
import com.example.googoose.ui.components.GooGooseCard
import com.example.googoose.ui.components.SegmentedControl
import com.example.googoose.ui.components.TagAccent
import com.example.googoose.ui.theme.GooGooseColors
import com.example.googoose.ui.theme.GooGooseType
import com.example.googoose.util.DateFormat
import com.example.googoose.util.currencySymbol
import com.example.googoose.util.formatMoney
import com.example.googoose.viewmodel.GooGooseLogic
import com.example.googoose.viewmodel.GooGooseUiState
import com.example.googoose.viewmodel.GooGooseViewModel
import com.example.googoose.viewmodel.TxnFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsTab(
    state: GooGooseUiState,
    strings: Strings,
    viewModel: GooGooseViewModel,
    modifier: Modifier = Modifier,
) {
    val groups = GooGooseLogic.filteredGroups(
        state.filter,
        state.transactions,
        beforeDate = state.dateBeforeFilter,
        language = state.language,
    )
    val currency = currencySymbol(state.settingsCurrency)
    var showDatePicker by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            GooGooseCard {
                CardKicker(strings.accountBalance)
                Text(
                    GooGooseLogic.accountBalanceLabel(state.transactions, currency),
                    style = GooGooseType.balance,
                    color = GooGooseColors.text,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TagAccent(GooGooseLogic.monthNetLabel(state.transactions, currencySymbol = currency))
                    Text(
                        strings.thisMonth,
                        style = GooGooseType.caption,
                        color = GooGooseColors.textMuted,
                        modifier = Modifier.align(Alignment.CenterVertically),
                    )
                }
            }
        }
        item {
            SegmentedControl(
                options = listOf(
                    TxnFilter.ALL to strings.all,
                    TxnFilter.INCOME to strings.income,
                    TxnFilter.SPEND to strings.spend,
                    TxnFilter.UNSOLVED to strings.unsolvedFilter,
                ),
                selected = state.filter,
                onSelect = viewModel::setFilter,
            )
        }
        item {
            DateBeforeChip(
                dateBeforeFilter = state.dateBeforeFilter,
                strings = strings,
                onOpenPicker = { showDatePicker = true },
                onClear = { viewModel.setDateBeforeFilter(null) },
            )
        }
        if (state.transactions.isEmpty()) {
            item {
                Text(strings.emptyTransactions, style = GooGooseType.bodySmall, color = GooGooseColors.textMuted)
            }
        }
        groups.forEach { group ->
            item(key = "header-${group.date}") {
                Text(
                    group.date.uppercase(),
                    style = GooGooseType.caption.copy(letterSpacing = 0.06.em),
                    color = GooGooseColors.textMuted,
                )
            }
            items(group.items, key = { it.id }) { txn ->
                TransactionRow(txn, currency, state.language, onClick = { viewModel.openDetail(txn) })
            }
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = state.dateBeforeFilter)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { viewModel.setDateBeforeFilter(DateFormat.localDayStart(it)) }
                    showDatePicker = false
                }) { Text(strings.confirm) }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text(strings.cancel) } },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
private fun DateBeforeChip(
    dateBeforeFilter: Long?,
    strings: Strings,
    onOpenPicker: () -> Unit,
    onClear: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (dateBeforeFilter != null) GooGooseColors.accent.copy(alpha = 0.14f) else GooGooseColors.surface)
            .clickable(onClick = onOpenPicker)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            Icons.Outlined.CalendarMonth,
            contentDescription = null,
            tint = if (dateBeforeFilter != null) GooGooseColors.accent else GooGooseColors.textMuted,
            modifier = Modifier.size(14.dp),
        )
        val label = if (dateBeforeFilter != null) strings.beforeDateLabel(DateFormat.isoDate(dateBeforeFilter)) else strings.beforeDatePrompt
        Text(
            label,
            style = GooGooseType.bodySmall.copy(fontSize = 13.sp),
            color = if (dateBeforeFilter != null) GooGooseColors.accent else GooGooseColors.textMuted,
        )
        if (dateBeforeFilter != null) {
            Icon(
                Icons.Outlined.Close,
                contentDescription = strings.clearDateFilter,
                tint = GooGooseColors.accent,
                modifier = Modifier.size(14.dp).clickable(onClick = onClear),
            )
        }
    }
}

@Composable
private fun TransactionRow(txn: Transaction, currencySymbol: String, language: Language, onClick: () -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier.size(34.dp).clip(CircleShape).background(GooGooseColors.avatarBg),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    txn.category.take(1),
                    color = GooGooseColors.avatarFg,
                    style = GooGooseType.bodySmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(txn.name, style = GooGooseType.bodySmall.copy(fontSize = 14.sp), color = GooGooseColors.text)
                Row {
                    val paidColor = if (txn.paid) GooGooseColors.incomeAmount else GooGooseColors.textMuted
                    Text(
                        "${txn.category} · ${DateFormat.timeOfDay(txn.occurredAt, language)} · ${txn.method} ",
                        style = GooGooseType.caption,
                        color = GooGooseColors.textMuted,
                    )
                    Text(if (txn.paid) "✔" else "✖", style = GooGooseType.caption, color = paidColor)
                }
            }
            val amountColor = if (txn.type == TxnType.INCOME) GooGooseColors.incomeAmount else GooGooseColors.text
            val sign = if (txn.type == TxnType.INCOME) "+" else "-"
            Text(
                "$sign$currencySymbol${formatMoney(txn.amount)}",
                style = GooGooseType.bodySmall.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium),
                color = amountColor,
            )
        }
        HorizontalDivider(color = GooGooseColors.divider, thickness = 1.dp)
    }
}
