package com.example.googoose.ui.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.googoose.data.Strings
import com.example.googoose.data.model.Transaction
import com.example.googoose.ui.components.DangerButton
import com.example.googoose.ui.components.FieldLabel
import com.example.googoose.ui.components.GhostIconButton
import com.example.googoose.ui.components.GooGooseCard
import com.example.googoose.ui.components.GooGooseTextField
import com.example.googoose.ui.components.PrimaryTextButton
import com.example.googoose.ui.components.SecondaryButton
import com.example.googoose.ui.components.SegmentedControl
import com.example.googoose.ui.theme.GooGooseColors
import com.example.googoose.ui.theme.GooGooseType
import com.example.googoose.util.DateFormat
import com.example.googoose.util.formatMoney
import com.example.googoose.viewmodel.GooGooseUiState
import com.example.googoose.viewmodel.GooGooseViewModel

/**
 * Full-screen "edit transaction" overlay. Amount/description/remarks are
 * [remember]'d here keyed by [Transaction.id] (so switching to a different
 * item resets them to that item's own values), but Save now actually writes
 * them back (Phase 2) — only Cancel discards. Category chips, payment method
 * and paid status were already real ViewModel state from Phase 1.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditTransactionScreen(
    item: Transaction,
    state: GooGooseUiState,
    strings: Strings,
    viewModel: GooGooseViewModel,
) {
    var amountText by remember(item.id) { mutableStateOf(formatMoney(item.amount)) }
    var descriptionText by remember(item.id) { mutableStateOf(item.name) }
    var remarksText by remember(item.id) { mutableStateOf(item.remarks) }
    var showValidation by remember(item.id) { mutableStateOf(false) }
    var showDeleteConfirm by remember(item.id) { mutableStateOf(false) }

    val amountValue = amountText.toDoubleOrNull()
    val amountValid = amountValue != null && amountValue > 0.0
    val descriptionValid = descriptionText.isNotBlank()

    Column(modifier = Modifier.fillMaxSize().background(GooGooseColors.background)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            GhostIconButton(Icons.AutoMirrored.Outlined.ArrowBack, "Back", onClick = viewModel::closeDetail)
            Text(strings.editTransaction, style = GooGooseType.headerBrand, color = GooGooseColors.text)
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
                FieldLabel(strings.amountLabel)
                GooGooseTextField(amountText, { amountText = it }, modifier = Modifier.fillMaxWidth())
                if (showValidation && !amountValid) {
                    Text(strings.amountRequired, style = GooGooseType.caption, color = GooGooseColors.error)
                }
            }
            Column {
                FieldLabel(strings.descriptionLabel)
                GooGooseTextField(descriptionText, { descriptionText = it }, modifier = Modifier.fillMaxWidth())
                if (showValidation && !descriptionValid) {
                    Text(strings.descriptionRequired, style = GooGooseType.caption, color = GooGooseColors.error)
                }
            }
            Column {
                FieldLabel(strings.categoryLabel)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    state.categories.forEach { cat ->
                        CategoryChip(
                            name = cat,
                            selected = cat == state.detailCategory,
                            deleteDescription = strings.deleteCategory,
                            onSelect = { viewModel.selectCategory(cat) },
                            onRemove = { viewModel.removeCategory(cat) },
                        )
                    }
                    if (state.addingCategory) {
                        NewCategoryField(
                            value = state.newCategoryName,
                            onValueChange = viewModel::setNewCategoryName,
                            onConfirm = viewModel::confirmAddCategory,
                        )
                    } else {
                        NewCategoryButton(label = strings.newCat, onClick = viewModel::startAddCategory)
                    }
                }
            }
            Column {
                FieldLabel(strings.paymentMethod)
                GooGooseTextField(
                    value = state.detailMethod,
                    onValueChange = viewModel::setDetailMethod,
                    placeholder = "e.g. Card, Cash, Bank transfer",
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Column {
                FieldLabel(strings.paymentStatus)
                SegmentedControl(
                    options = listOf(true to strings.paidLabel, false to strings.unpaidLabel),
                    selected = state.detailPaid ?: true,
                    onSelect = viewModel::setDetailPaid,
                )
            }
            Column {
                FieldLabel(strings.remarksLabel)
                GooGooseTextField(
                    value = remarksText,
                    onValueChange = { remarksText = it },
                    placeholder = strings.addNote,
                    singleLine = false,
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            HorizontalDivider(color = GooGooseColors.divider, thickness = 1.dp)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                MetaRow(strings.created, DateFormat.dateTime(item.createdAt, state.language))
                MetaRow(strings.lastModified, DateFormat.dateTime(item.modifiedAt, state.language))
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DangerButton(strings.deleteTransaction, onClick = { showDeleteConfirm = true })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SecondaryButton(strings.cancel, onClick = viewModel::closeDetail)
                    PrimaryTextButton(
                        strings.save,
                        onClick = {
                            if (amountValid && descriptionValid) {
                                viewModel.saveDetail(amountValue!!, descriptionText.trim(), remarksText)
                            } else {
                                showValidation = true
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
                Text(strings.deleteTransactionTitle, style = GooGooseType.dialogTitle, color = GooGooseColors.text)
                Text(strings.deleteTransactionWarning, style = GooGooseType.bodySmall, color = GooGooseColors.text)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                ) {
                    SecondaryButton(strings.cancel, onClick = { showDeleteConfirm = false })
                    DangerButton(
                        strings.deleteTransaction,
                        onClick = {
                            showDeleteConfirm = false
                            viewModel.deleteTransaction()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun MetaRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = GooGooseType.caption, color = GooGooseColors.textMuted)
        Text(value, style = GooGooseType.caption, color = GooGooseColors.text)
    }
}

@Composable
private fun CategoryChip(
    name: String,
    selected: Boolean,
    deleteDescription: String,
    onSelect: () -> Unit,
    onRemove: () -> Unit,
) {
    val bg = if (selected) GooGooseColors.tagAccentBg else GooGooseColors.tagNeutralBg
    val fg = if (selected) GooGooseColors.tagAccentFg else GooGooseColors.tagNeutralFg
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .clickable(onClick = onSelect)
            .padding(start = 10.dp, top = 5.dp, bottom = 5.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(name, style = GooGooseType.tag, color = fg)
        Box(
            modifier = Modifier
                .size(15.dp)
                .clip(CircleShape)
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Outlined.Close,
                contentDescription = deleteDescription,
                tint = fg.copy(alpha = 0.7f),
                modifier = Modifier.size(11.dp),
            )
        }
    }
}

@Composable
private fun NewCategoryButton(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .border(1.dp, GooGooseColors.accent, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(start = 10.dp, top = 5.dp, bottom = 5.dp, end = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(Icons.Outlined.Add, contentDescription = null, tint = GooGooseColors.accent, modifier = Modifier.size(11.dp))
        Text(label, style = GooGooseType.tag, color = GooGooseColors.accent)
    }
}

/** Enter/Done confirms, matching the mockup's onKeyDown-Enter path (onBlur-confirm is not replicated). */
@Composable
private fun NewCategoryField(value: String, onValueChange: (String) -> Unit, onConfirm: () -> Unit) {
    Box(
        modifier = Modifier
            .width(140.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(GooGooseColors.surface)
            .border(1.dp, GooGooseColors.accent, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = GooGooseType.bodySmall.copy(color = GooGooseColors.text),
            cursorBrush = SolidColor(GooGooseColors.accent),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onConfirm() }),
        )
    }
}
