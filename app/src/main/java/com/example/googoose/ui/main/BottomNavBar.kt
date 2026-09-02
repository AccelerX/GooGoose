package com.example.googoose.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.googoose.data.Strings
import com.example.googoose.ui.theme.GooGooseColors
import com.example.googoose.ui.theme.GooGooseType

/** 4 tab buttons split around a centered floating "add transaction" button. */
@Composable
fun BottomNavBar(
    currentTab: Int,
    onTabClick: (Int) -> Unit,
    onFabClick: () -> Unit,
    strings: Strings,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(color = GooGooseColors.divider, thickness = 1.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(GooGooseColors.surface)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    NavButton(Icons.Outlined.Receipt, strings.transactions, currentTab == 0) { onTabClick(0) }
                    NavButton(Icons.AutoMirrored.Outlined.ListAlt, strings.todoTab, currentTab == 3) { onTabClick(3) }
                }
                Spacer(Modifier.width(56.dp))
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    NavButton(Icons.Outlined.BarChart, strings.reports, currentTab == 2) { onTabClick(2) }
                    NavButton(Icons.Outlined.Inventory, strings.stockTab, currentTab == 1) { onTabClick(1) }
                }
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-24).dp)
                .size(52.dp)
                .clip(CircleShape)
                .background(GooGooseColors.surface)
                .border(1.5.dp, GooGooseColors.accent, CircleShape)
                .clickable(onClick = onFabClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Outlined.Add,
                contentDescription = strings.addTransaction,
                tint = GooGooseColors.accent,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun NavButton(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    val color = if (selected) GooGooseColors.accent else GooGooseColors.textMuted
    Column(
        modifier = Modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(22.5.dp)) // 18dp × 1.25
        Text(label, style = GooGooseType.tabLabel, color = color)
    }
}
