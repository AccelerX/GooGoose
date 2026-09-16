package com.example.googoose.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.googoose.BuildConfig
import com.example.googoose.data.Strings
import com.example.googoose.ui.components.FieldLabel
import com.example.googoose.ui.components.GhostIconButton
import com.example.googoose.ui.theme.GooGooseColors
import com.example.googoose.ui.theme.GooGooseType

private const val AUTHOR = "AccelerX"
private const val GITHUB_URL = "https://github.com/AccelerX/GooGoose"

/**
 * Settings sub-page — version (real BuildConfig values, not a hand-maintained
 * duplicate), author, and repo link. Nested inside SettingsScreen's own local
 * state (not the app's global overlay stack) since it's a detail view of
 * Settings, not a sibling of it.
 */
@Composable
fun AboutScreen(strings: Strings, onBack: () -> Unit) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().background(GooGooseColors.background)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            GhostIconButton(Icons.AutoMirrored.Outlined.ArrowBack, "Back", onClick = onBack)
            Text(strings.aboutLabel, style = GooGooseType.headerBrand, color = GooGooseColors.text)
        }
        HorizontalDivider(color = GooGooseColors.divider, thickness = 1.dp)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column {
                FieldLabel(strings.versionLabel)
                Text(
                    "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                    style = GooGooseType.bodySmall,
                    color = GooGooseColors.text,
                )
            }
            Column {
                FieldLabel(strings.authorLabel)
                Text(AUTHOR, style = GooGooseType.bodySmall, color = GooGooseColors.text)
            }
            Column {
                FieldLabel(strings.githubLabel)
                Text(
                    GITHUB_URL,
                    style = GooGooseType.bodySmall,
                    color = GooGooseColors.accent,
                    modifier = Modifier.clickable {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL)))
                    },
                )
            }
        }
    }
}
