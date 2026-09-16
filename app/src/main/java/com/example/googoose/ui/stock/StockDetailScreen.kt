package com.example.googoose.ui.stock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.googoose.data.Strings
import com.example.googoose.data.model.StockItem
import com.example.googoose.ui.components.DangerButton
import com.example.googoose.ui.components.FieldLabel
import com.example.googoose.ui.components.GhostIconButton
import com.example.googoose.ui.components.GooGooseCard
import com.example.googoose.ui.components.GooGooseTextField
import com.example.googoose.ui.components.PrimaryTextButton
import com.example.googoose.ui.components.SecondaryButton
import com.example.googoose.ui.theme.GooGooseColors
import com.example.googoose.ui.theme.GooGooseType
import com.example.googoose.util.formatQty
import com.example.googoose.viewmodel.GooGooseViewModel

/**
 * Full-screen "edit stock item" overlay, reached by tapping a card in
 * StockTab. Name/quantity/unit/threshold are direct-edit fields buffered
 * locally (remember(item.id), same pattern as EditTransactionScreen) and
 * saved together on Save. The card's own -/amount/+ delta stepper is
 * unchanged and lives only on the card — this page is for setting the
 * fields directly (e.g. correcting a miscount), not incremental adjustment.
 */
@Composable
fun StockDetailScreen(item: StockItem, strings: Strings, viewModel: GooGooseViewModel) {
    var name by remember(item.id) { mutableStateOf(item.name) }
    var qtyText by remember(item.id) { mutableStateOf(formatQty(item.qty)) }
    var unit by remember(item.id) { mutableStateOf(item.unit) }
    var lowText by remember(item.id) { mutableStateOf(formatQty(item.low)) }
    var showDeleteConfirm by remember(item.id) { mutableStateOf(false) }

    val qtyValue = qtyText.toDoubleOrNull()
    val lowValue = lowText.toDoubleOrNull()
    val canSave = name.isNotBlank() && qtyValue != null && qtyValue >= 0.0 && lowValue != null && lowValue >= 0.0

    Column(modifier = Modifier.fillMaxSize().background(GooGooseColors.background)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            GhostIconButton(Icons.AutoMirrored.Outlined.ArrowBack, "Back", onClick = viewModel::closeStockDetail)
            Text(strings.editStockItem, style = GooGooseType.headerBrand, color = GooGooseColors.text)
        }
        HorizontalDivider(color = GooGooseColors.divider, thickness = 1.dp)

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column {
                FieldLabel(strings.nameLabel)
                GooGooseTextField(value = name, onValueChange = { name = it }, modifier = Modifier.fillMaxWidth())
            }
            Column {
                FieldLabel(strings.currentQtyLabel)
                GooGooseTextField(
                    value = qtyText,
                    onValueChange = { qtyText = it },
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Column {
                FieldLabel(strings.unitLabel)
                GooGooseTextField(value = unit, onValueChange = { unit = it }, modifier = Modifier.fillMaxWidth())
            }
            Column {
                FieldLabel(strings.thresholdLabel)
                GooGooseTextField(
                    value = lowText,
                    onValueChange = { lowText = it },
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DangerButton(strings.removeItem, onClick = { showDeleteConfirm = true })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SecondaryButton(strings.cancel, onClick = viewModel::closeStockDetail)
                    PrimaryTextButton(
                        strings.save,
                        onClick = {
                            if (canSave) {
                                viewModel.saveStockDetail(item.id, name.trim(), qtyValue!!, unit.ifBlank { "pcs" }, lowValue!!)
                            }
                        },
                    )
                }
            }
        }
    }

    if (showDeleteConfirm) {
        Dialog(onDismissRequest = { showDeleteConfirm = false }) {
            GooGooseCard(modifier = Modifier.fillMaxWidth()) {
                Text(strings.deleteStockItemTitle, style = GooGooseType.dialogTitle, color = GooGooseColors.text)
                Text(strings.deleteStockItemWarning, style = GooGooseType.bodySmall, color = GooGooseColors.text)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                ) {
                    SecondaryButton(strings.cancel, onClick = { showDeleteConfirm = false })
                    DangerButton(
                        strings.removeItem,
                        onClick = {
                            showDeleteConfirm = false
                            viewModel.removeStock(item.id)
                        },
                    )
                }
            }
        }
    }
}
