package com.example.googoose.ui.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.googoose.data.Strings
import com.example.googoose.data.model.TxnType
import com.example.googoose.ui.components.FieldLabel
import com.example.googoose.ui.components.GooGooseTextField
import com.example.googoose.ui.components.PrimaryTextButton
import com.example.googoose.ui.components.SecondaryButton
import com.example.googoose.ui.components.SegmentedControl
import com.example.googoose.ui.theme.GooGooseColors
import com.example.googoose.ui.theme.GooGooseType
import com.example.googoose.util.DateFormat
import com.example.googoose.util.currencySymbol
import com.example.googoose.viewmodel.GooGooseUiState
import com.example.googoose.viewmodel.GooGooseViewModel

/**
 * Every field is real now — Save actually inserts a new transaction. The
 * category list comes from the live [GooGooseUiState.categories] rather than
 * a fixed sample list. Payment method is optional, defaults to whatever the
 * most-recently-created transaction used, and offers an autocomplete history
 * (tap into the field) backed by [GooGooseUiState.paymentMethods] — each
 * history entry can be removed via its own ✕ without touching past
 * transactions that used it. Payment status defaults to Paid. Cancel closes
 * without saving.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionSheet(state: GooGooseUiState, strings: Strings, viewModel: GooGooseViewModel) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember(state.categories) { mutableStateOf(state.categories.firstOrNull().orEmpty()) }
    var categoryMenuOpen by remember { mutableStateOf(false) }
    var method by remember { mutableStateOf(state.transactions.maxByOrNull { it.createdAt }?.method.orEmpty()) }
    var showMethodHistory by remember { mutableStateOf(false) }
    var paid by remember { mutableStateOf(true) }
    var occurredAt by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showValidation by remember { mutableStateOf(false) }

    val amountValue = amount.toDoubleOrNull()
    val amountValid = amountValue != null && amountValue > 0.0
    val descriptionValid = description.isNotBlank()

    ModalBottomSheet(
        onDismissRequest = viewModel::closeSheet,
        sheetState = rememberModalBottomSheetState(),
        containerColor = GooGooseColors.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, end = 18.dp, bottom = 22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(strings.addTransaction, style = GooGooseType.dialogTitle, color = GooGooseColors.text)

            SegmentedControl(
                options = listOf(TxnType.SPEND to strings.spend, TxnType.INCOME to strings.income),
                selected = state.sheetType,
                onSelect = viewModel::setSheetType,
            )

            Column {
                FieldLabel(strings.amountLabel)
                GooGooseTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    placeholder = "${currencySymbol(state.settingsCurrency)}0.00",
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (showValidation && !amountValid) {
                    Text(strings.amountRequired, style = GooGooseType.caption, color = GooGooseColors.error)
                }
            }
            Column {
                FieldLabel(strings.descriptionLabel)
                GooGooseTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = strings.posPlaceholder,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (showValidation && !descriptionValid) {
                    Text(strings.descriptionRequired, style = GooGooseType.caption, color = GooGooseColors.error)
                }
            }
            Column {
                FieldLabel(strings.categoryLabel)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(GooGooseColors.surface)
                        .border(1.dp, GooGooseColors.divider, RoundedCornerShape(8.dp))
                        .clickable { categoryMenuOpen = true }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            category.ifBlank { strings.categoryLabel },
                            style = GooGooseType.bodySmall,
                            color = GooGooseColors.text,
                        )
                        Icon(Icons.Outlined.ArrowDropDown, contentDescription = null, tint = GooGooseColors.textMuted)
                    }
                    DropdownMenu(expanded = categoryMenuOpen, onDismissRequest = { categoryMenuOpen = false }) {
                        state.categories.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = { category = option; categoryMenuOpen = false },
                            )
                        }
                    }
                }
            }
            Column {
                FieldLabel(strings.paymentMethod)
                Box(modifier = Modifier.fillMaxWidth()) {
                    GooGooseTextField(
                        value = method,
                        onValueChange = { method = it },
                        placeholder = strings.paymentMethodPlaceholder,
                        modifier = Modifier.fillMaxWidth(),
                        onFocusChanged = { focused -> showMethodHistory = focused },
                    )
                    DropdownMenu(
                        expanded = showMethodHistory && state.paymentMethods.isNotEmpty(),
                        onDismissRequest = { showMethodHistory = false },
                    ) {
                        state.paymentMethods.forEach { historyValue ->
                            DropdownMenuItem(
                                text = { Text(historyValue) },
                                onClick = { method = historyValue; showMethodHistory = false },
                                trailingIcon = {
                                    Icon(
                                        Icons.Outlined.Close,
                                        contentDescription = strings.removePaymentMethodHistory,
                                        tint = GooGooseColors.textMuted,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable { viewModel.removePaymentMethodHistory(historyValue) },
                                    )
                                },
                            )
                        }
                    }
                }
            }
            Column {
                FieldLabel(strings.paymentStatus)
                SegmentedControl(
                    options = listOf(true to strings.paidLabel, false to strings.unpaidLabel),
                    selected = paid,
                    onSelect = { paid = it },
                )
            }
            Column {
                FieldLabel(strings.dateLabel)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(GooGooseColors.surface)
                        .border(1.dp, GooGooseColors.divider, RoundedCornerShape(8.dp))
                        .clickable { showDatePicker = true }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                ) {
                    Text(DateFormat.isoDate(occurredAt), style = GooGooseType.bodySmall, color = GooGooseColors.text)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            ) {
                SecondaryButton(strings.cancel, onClick = viewModel::closeSheet)
                PrimaryTextButton(
                    strings.save,
                    onClick = {
                        if (amountValid && descriptionValid && category.isNotBlank()) {
                            viewModel.addTransaction(description.trim(), category, amountValue, occurredAt, method.trim(), paid)
                        } else {
                            showValidation = true
                        }
                    },
                )
            }
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = occurredAt)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { occurredAt = DateFormat.fromDatePickerSelection(it) }
                    showDatePicker = false
                }) { Text(strings.confirm) }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text(strings.cancel) } },
        ) {
            DatePicker(state = pickerState)
        }
    }
}
