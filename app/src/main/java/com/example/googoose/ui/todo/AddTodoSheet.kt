package com.example.googoose.ui.todo

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

/** Bottom sheet for adding a new task — mirrors [com.example.googoose.ui.stock.AddStockItemSheet]'s shape. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTodoSheet(state: GooGooseUiState, strings: Strings, viewModel: GooGooseViewModel) {
    ModalBottomSheet(
        onDismissRequest = viewModel::closeAddTodo,
        sheetState = rememberModalBottomSheetState(),
        containerColor = GooGooseColors.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, end = 18.dp, bottom = 22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(strings.addTask, style = GooGooseType.dialogTitle, color = GooGooseColors.text)
            Column {
                FieldLabel(strings.titleLabel)
                GooGooseTextField(
                    value = state.newTodoTitle,
                    onValueChange = viewModel::setNewTodoTitle,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Column {
                FieldLabel(strings.descriptionLabel)
                GooGooseTextField(
                    value = state.newTodoDesc,
                    onValueChange = viewModel::setNewTodoDesc,
                    singleLine = false,
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            ) {
                SecondaryButton(strings.cancel, onClick = viewModel::closeAddTodo)
                PrimaryTextButton(strings.add, onClick = viewModel::saveNewTodo)
            }
        }
    }
}
