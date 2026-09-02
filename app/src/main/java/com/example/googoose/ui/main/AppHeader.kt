package com.example.googoose.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.googoose.data.Strings
import com.example.googoose.ui.components.GhostIconButton
import com.example.googoose.ui.theme.GooGooseColors
import com.example.googoose.ui.theme.GooGooseType

/** App name + settings gear, current tab title, business name — with the divider spanning full width below. */
@Composable
fun AppHeader(
    appName: String,
    tabTitle: String,
    businessName: String,
    strings: Strings,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(appName, style = GooGooseType.headerBrand, color = GooGooseColors.accent)
                GhostIconButton(
                    icon = Icons.Outlined.Settings,
                    contentDescription = strings.settingsTitle,
                    onClick = onSettingsClick,
                )
            }
            Text(
                tabTitle,
                style = GooGooseType.h3,
                color = GooGooseColors.text,
                modifier = Modifier.padding(top = 6.dp, bottom = 1.dp),
            )
            Text(businessName, style = GooGooseType.caption, color = GooGooseColors.textMuted)
        }
        HorizontalDivider(color = GooGooseColors.divider, thickness = 1.dp)
    }
}
