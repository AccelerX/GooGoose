package com.example.googoose.ui.stock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.googoose.data.Strings
import com.example.googoose.ui.components.GooGooseCard
import com.example.googoose.ui.components.PrimaryTextButton
import com.example.googoose.ui.components.SecondaryButton
import com.example.googoose.ui.theme.GooGooseColors
import com.example.googoose.ui.theme.GooGooseType
import com.example.googoose.util.formatQty
import com.example.googoose.viewmodel.GooGooseViewModel
import com.example.googoose.viewmodel.StockConfirm
import kotlin.math.abs

/** Centered modal shown before every +/- stock edit is actually applied. */
@Composable
fun StockConfirmDialog(confirm: StockConfirm, strings: Strings, viewModel: GooGooseViewModel) {
    val verb = if (confirm.isIncrease) strings.increaseVerb else strings.decreaseVerb
    val title = if (confirm.isIncrease) strings.increaseStock else strings.decreaseStock
    val message = strings.stockMsg(verb, confirm.name, formatQty(abs(confirm.delta)), confirm.unit, formatQty(confirm.newQty))

    Dialog(onDismissRequest = viewModel::cancelStockChange) {
        GooGooseCard(modifier = Modifier.fillMaxWidth()) {
            Text(title, style = GooGooseType.dialogTitle, color = GooGooseColors.text)
            Text(message, style = GooGooseType.bodySmall, color = GooGooseColors.text)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            ) {
                SecondaryButton(strings.cancel, onClick = viewModel::cancelStockChange)
                PrimaryTextButton(strings.confirm, onClick = viewModel::confirmStockChange)
            }
        }
    }
}
