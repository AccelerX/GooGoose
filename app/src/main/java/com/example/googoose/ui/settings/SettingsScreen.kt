package com.example.googoose.ui.settings

import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.googoose.data.Language
import com.example.googoose.data.Strings
import com.example.googoose.data.TextSizePreset
import com.example.googoose.ui.components.FieldLabel
import com.example.googoose.ui.components.GhostIconButton
import com.example.googoose.ui.components.GooGooseCard
import com.example.googoose.ui.components.GooGooseTextField
import com.example.googoose.ui.components.PrimaryTextButton
import com.example.googoose.ui.components.SecondaryButton
import com.example.googoose.ui.theme.GooGooseColors
import com.example.googoose.ui.theme.GooGooseType
import com.example.googoose.viewmodel.GooGooseUiState
import com.example.googoose.viewmodel.GooGooseViewModel
import kotlinx.coroutines.launch

private val currencyOptions = listOf(
    "USD" to "USD ($)",
    "EUR" to "EUR (€)",
    "GBP" to "GBP (£)",
    "CAD" to "CAD ($)",
    "CNY" to "CNY (¥)",
)
private val languageOptions = listOf(Language.EN to "English", Language.ZH_CN to "简体中文")

/**
 * Full-screen Settings overlay, backed by Room via [viewModel] (Phase 2).
 * Export/Import round-trip the full dataset — transactions, stock, todos,
 * categories, business name, currency, language. Import is a full replace,
 * so it's gated behind a confirmation dialog (below).
 */
@Composable
fun SettingsScreen(state: GooGooseUiState, strings: Strings, viewModel: GooGooseViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showImportConfirm by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            scope.launch {
                val json = viewModel.buildExportJson()
                runCatching {
                    context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
                }
            }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val fileName = queryDisplayName(context, uri) ?: uri.lastPathSegment ?: "file.json"
            val text = runCatching {
                context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            }.getOrNull()
            viewModel.importData(fileName, text ?: "")
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(GooGooseColors.background)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            GhostIconButton(Icons.AutoMirrored.Outlined.ArrowBack, "Back", onClick = viewModel::closeSettings)
            Text(strings.settingsTitle, style = GooGooseType.headerBrand, color = GooGooseColors.text)
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
                FieldLabel(strings.businessNameLabel)
                GooGooseTextField(
                    value = state.settingsName,
                    onValueChange = viewModel::setSettingsName,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Column {
                FieldLabel(strings.currencyLabel)
                DropdownField(
                    selectedLabel = currencyOptions.first { it.first == state.settingsCurrency }.second,
                    options = currencyOptions,
                    onSelect = viewModel::setSettingsCurrency,
                )
            }
            Column {
                FieldLabel(strings.languageLabel)
                DropdownField(
                    selectedLabel = languageOptions.first { it.first == state.language }.second,
                    options = languageOptions,
                    onSelect = viewModel::setSettingsLanguage,
                )
            }
            Column {
                FieldLabel(strings.textSizeLabel)
                val textSizeOptions = listOf(
                    TextSizePreset.SMALL to strings.textSizeSmall,
                    TextSizePreset.STANDARD to strings.textSizeStandard,
                    TextSizePreset.LARGE to strings.textSizeLarge,
                )
                DropdownField(
                    selectedLabel = textSizeOptions.first { it.first == state.textSize }.second,
                    options = textSizeOptions,
                    onSelect = viewModel::setTextSize,
                )
            }
            HorizontalDivider(color = GooGooseColors.divider, thickness = 1.dp)
            Column {
                FieldLabel(strings.dataLabel)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SecondaryButton(
                        strings.exportLabel,
                        onClick = { exportLauncher.launch("till-data.json") },
                        leadingIcon = Icons.Outlined.FileDownload,
                        modifier = Modifier.weight(1f),
                    )
                    SecondaryButton(
                        strings.importLabel,
                        onClick = { showImportConfirm = true },
                        leadingIcon = Icons.Outlined.FileUpload,
                        modifier = Modifier.weight(1f),
                    )
                }
                Text(strings.importWarning, style = GooGooseType.caption, color = GooGooseColors.error)
                val importMessage = state.importMessage
                if (importMessage != null) {
                    Text(importMessage, style = GooGooseType.caption, color = GooGooseColors.textMuted)
                }
            }
        }
    }

    if (showImportConfirm) {
        Dialog(onDismissRequest = { showImportConfirm = false }) {
            GooGooseCard(modifier = Modifier.fillMaxWidth()) {
                Text(strings.importWarningTitle, style = GooGooseType.dialogTitle, color = GooGooseColors.text)
                Text(strings.importWarning, style = GooGooseType.bodySmall, color = GooGooseColors.text)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                ) {
                    SecondaryButton(strings.cancel, onClick = { showImportConfirm = false })
                    PrimaryTextButton(
                        strings.importLabel,
                        onClick = {
                            showImportConfirm = false
                            importLauncher.launch(arrayOf("application/json"))
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun <T> DropdownField(selectedLabel: String, options: List<Pair<T, String>>, onSelect: (T) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(GooGooseColors.surface)
            .border(1.dp, GooGooseColors.divider, RoundedCornerShape(8.dp))
            .clickable { expanded = true }
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(selectedLabel, style = GooGooseType.bodySmall, color = GooGooseColors.text)
            Icon(Icons.Outlined.ArrowDropDown, contentDescription = null, tint = GooGooseColors.textMuted)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (value, label) ->
                DropdownMenuItem(text = { Text(label) }, onClick = { onSelect(value); expanded = false })
            }
        }
    }
}

private fun queryDisplayName(context: android.content.Context, uri: android.net.Uri): String? {
    return runCatching {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }
    }.getOrNull()
}
