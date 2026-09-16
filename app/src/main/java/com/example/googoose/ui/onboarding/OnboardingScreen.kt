package com.example.googoose.ui.onboarding

import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.googoose.data.Language
import com.example.googoose.data.SeedData
import com.example.googoose.data.stringsFor
import com.example.googoose.ui.components.FieldLabel
import com.example.googoose.ui.components.GooGooseTextField
import com.example.googoose.ui.components.PrimaryTextButton
import com.example.googoose.ui.settings.DropdownField
import com.example.googoose.ui.settings.currencyOptions
import com.example.googoose.ui.settings.languageOptions
import com.example.googoose.ui.theme.GooGooseColors
import com.example.googoose.ui.theme.GooGooseType
import com.example.googoose.viewmodel.GooGooseViewModel

private enum class DataChoice { SAMPLE, EMPTY, IMPORT }

/**
 * First-launch screen — shown instead of the main app while
 * `state.hasOnboarded` is false (see GooGooseApp). Strings are derived from
 * this screen's OWN [language] selection rather than a passed-in [Strings]
 * — the outer app's language stays at the persisted default (English, on a
 * fresh install) until onboarding actually completes, so reading that would
 * make the language dropdown not visibly do anything until after Save.
 * Currency/language here become the real Settings values on completion; the
 * Sample/Empty choice decides whether demo content gets seeded (default
 * categories always do — see GooGooseRepository.completeOnboarding). The
 * Import choice bypasses all of that and goes through the same restore path
 * Settings' own Import uses, so the imported file's own settings win.
 */
@Composable
fun OnboardingScreen(viewModel: GooGooseViewModel) {
    val context = LocalContext.current
    var language by remember { mutableStateOf(Language.EN) }
    val strings = stringsFor(language)
    var currency by remember { mutableStateOf("USD") }
    var dataChoice by remember { mutableStateOf(DataChoice.SAMPLE) }
    var businessName by remember { mutableStateOf("") }
    var showValidation by remember { mutableStateOf(false) }

    val businessNameValid = businessName.isNotBlank()

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val fileName = runCatching {
                context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) cursor.getString(0) else null
                }
            }.getOrNull() ?: uri.lastPathSegment ?: "file.json"
            val text = runCatching {
                context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            }.getOrNull()
            // restoreAll (behind importData) always sets hasOnboarded = true, so a successful
            // import here exits onboarding on its own — no separate "finish" call needed.
            viewModel.importData(fileName, text ?: "")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GooGooseColors.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(strings.onboardingTitle, style = GooGooseType.dialogTitle, color = GooGooseColors.accent)
            Text(strings.onboardingSubtitle, style = GooGooseType.bodySmall, color = GooGooseColors.textMuted)
        }

        Column {
            FieldLabel(strings.languageLabel)
            DropdownField(
                selectedLabel = languageOptions.first { it.first == language }.second,
                options = languageOptions,
                onSelect = { language = it },
            )
        }
        Column {
            FieldLabel(strings.currencyLabel)
            DropdownField(
                selectedLabel = currencyOptions.first { it.first == currency }.second,
                options = currencyOptions,
                onSelect = { currency = it },
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FieldLabel(strings.onboardingDataSectionLabel)
            DataChoiceOption(
                title = strings.onboardingSampleTitle,
                description = strings.onboardingSampleDesc,
                selected = dataChoice == DataChoice.SAMPLE,
                onClick = { dataChoice = DataChoice.SAMPLE },
            )
            DataChoiceOption(
                title = strings.onboardingEmptyTitle,
                description = strings.onboardingEmptyDesc,
                selected = dataChoice == DataChoice.EMPTY,
                onClick = { dataChoice = DataChoice.EMPTY },
            )
            DataChoiceOption(
                title = strings.onboardingImportTitle,
                description = strings.onboardingImportDesc,
                selected = dataChoice == DataChoice.IMPORT,
                onClick = { dataChoice = DataChoice.IMPORT },
            )
        }

        // Sample data already comes with its own business identity (Riverside Coffee & Goods);
        // starting from scratch should use the user's own name, not that demo one.
        if (dataChoice == DataChoice.EMPTY) {
            Column {
                FieldLabel(strings.businessNameLabel)
                GooGooseTextField(
                    value = businessName,
                    onValueChange = { businessName = it },
                    modifier = Modifier.fillMaxWidth(),
                )
                if (showValidation && !businessNameValid) {
                    Text(strings.nameRequired, style = GooGooseType.caption, color = GooGooseColors.error)
                }
            }
        }

        PrimaryTextButton(
            text = if (dataChoice == DataChoice.IMPORT) strings.onboardingChooseFile else strings.onboardingGetStarted,
            onClick = {
                when (dataChoice) {
                    DataChoice.IMPORT -> importLauncher.launch(arrayOf("application/json"))
                    DataChoice.SAMPLE -> viewModel.completeOnboarding(SeedData.defaultBusinessName, currency, language, seedSampleData = true)
                    DataChoice.EMPTY -> {
                        if (businessNameValid) {
                            viewModel.completeOnboarding(businessName.trim(), currency, language, seedSampleData = false)
                        } else {
                            showValidation = true
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
            textStyle = GooGooseType.bodySmall.let { it.copy(fontSize = it.fontSize * 2) },
        )
    }
}

@Composable
private fun DataChoiceOption(title: String, description: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) GooGooseColors.accent.copy(alpha = 0.14f) else GooGooseColors.surface)
            .border(1.dp, if (selected) GooGooseColors.accent else GooGooseColors.divider, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(title, style = GooGooseType.cardTitle, color = if (selected) GooGooseColors.accent else GooGooseColors.text)
        Text(description, style = GooGooseType.caption, color = GooGooseColors.textMuted)
    }
}
