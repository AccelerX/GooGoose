package com.example.googoose.ui.stock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.googoose.data.Strings
import com.example.googoose.ui.components.FieldLabel
import com.example.googoose.ui.components.GooGooseTextField
import com.example.googoose.ui.components.PrimaryTextButton
import com.example.googoose.ui.components.SecondaryButton
import com.example.googoose.ui.theme.GooGooseColors
import com.example.googoose.ui.theme.GooGooseType
import com.example.googoose.viewmodel.GooGooseUiState
import com.example.googoose.viewmodel.GooGooseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStockItemSheet(state: GooGooseUiState, strings: Strings, viewModel: GooGooseViewModel) {
    ModalBottomSheet(
        onDismissRequest = viewModel::closeAddStock,
        sheetState = rememberModalBottomSheetState(),
        containerColor = GooGooseColors.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, end = 18.dp, bottom = 22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(strings.addStockItem, style = GooGooseType.dialogTitle, color = GooGooseColors.text)
            Column {
                FieldLabel(strings.nameLabel)
                GooGooseTextField(
                    value = state.newStockName,
                    onValueChange = viewModel::setNewStockName,
                    placeholder = strings.vanillaPlaceholder,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Column {
                FieldLabel(strings.startingQty)
                GooGooseTextField(
                    value = state.newStockQty,
                    onValueChange = viewModel::setNewStockQty,
                    placeholder = "0",
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Column {
                FieldLabel(strings.thresholdLabel)
                GooGooseTextField(
                    value = state.newStockLow,
                    onValueChange = viewModel::setNewStockLow,
                    placeholder = strings.thresholdPlaceholder,
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Column {
                FieldLabel(strings.unitLabel)
                GooGooseTextField(
                    value = state.newStockUnit,
                    onValueChange = viewModel::setNewStockUnit,
                    placeholder = strings.unitPlaceholder,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            ) {
                SecondaryButton(strings.cancel, onClick = viewModel::closeAddStock)
                PrimaryTextButton(strings.add, onClick = viewModel::saveNewStock)
            }
        }
    }
}
