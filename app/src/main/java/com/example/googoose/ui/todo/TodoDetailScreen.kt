package com.example.googoose.ui.todo

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.googoose.data.Strings
import com.example.googoose.data.model.TodoItem
import com.example.googoose.ui.components.DangerButton
import com.example.googoose.ui.components.FieldLabel
import com.example.googoose.ui.components.GhostIconButton
import com.example.googoose.ui.components.GooGooseCard
import com.example.googoose.ui.components.GooGooseTextField
import com.example.googoose.ui.components.PrimaryTextButton
import com.example.googoose.ui.components.SecondaryButton
import com.example.googoose.ui.theme.GooGooseColors
import com.example.googoose.ui.theme.GooGooseType
import com.example.googoose.viewmodel.GooGooseViewModel

/**
 * Full-screen "edit task" overlay, reached by tapping a card in TodoTab.
 * Title/description are direct-edit fields buffered locally
 * (remember(item.id), same pattern as Stock/Edit-transaction detail).
 * Mark done/undone stays a card-only action, unchanged. Delete only shows
 * for completed tasks — same rule the card's own delete icon used to
 * enforce (see GooGooseViewModel.deleteTodo).
 */
@Composable
fun TodoDetailScreen(item: TodoItem, strings: Strings, viewModel: GooGooseViewModel) {
    var title by remember(item.id) { mutableStateOf(item.title) }
    var desc by remember(item.id) { mutableStateOf(item.desc) }
    var showDeleteConfirm by remember(item.id) { mutableStateOf(false) }

    val canSave = title.isNotBlank()

    Column(modifier = Modifier.fillMaxSize().background(GooGooseColors.background)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            GhostIconButton(Icons.AutoMirrored.Outlined.ArrowBack, "Back", onClick = viewModel::closeTodoDetail)
            Text(strings.editTodo, style = GooGooseType.headerBrand, color = GooGooseColors.text)
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
                FieldLabel(strings.titleLabel)
                GooGooseTextField(value = title, onValueChange = { title = it }, modifier = Modifier.fillMaxWidth())
            }
            Column {
                FieldLabel(strings.descriptionLabel)
                GooGooseTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    singleLine = false,
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (item.done) {
                    DangerButton(strings.removeTodo, onClick = { showDeleteConfirm = true })
                } else {
                    Row {} // keeps Cancel/Save pinned to the end even with no delete button
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SecondaryButton(strings.cancel, onClick = viewModel::closeTodoDetail)
                    PrimaryTextButton(
                        strings.save,
                        onClick = { if (canSave) viewModel.saveTodoDetail(item.id, title.trim(), desc) },
                    )
                }
            }
        }
    }

    if (showDeleteConfirm) {
        Dialog(onDismissRequest = { showDeleteConfirm = false }) {
            GooGooseCard(modifier = Modifier.fillMaxWidth()) {
                Text(strings.deleteTodoTitle, style = GooGooseType.dialogTitle, color = GooGooseColors.text)
                Text(strings.deleteTodoWarning, style = GooGooseType.bodySmall, color = GooGooseColors.text)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                ) {
                    SecondaryButton(strings.cancel, onClick = { showDeleteConfirm = false })
                    DangerButton(
                        strings.removeTodo,
                        onClick = {
                            showDeleteConfirm = false
                            viewModel.deleteTodo(item.id)
                        },
                    )
                }
            }
        }
    }
}
