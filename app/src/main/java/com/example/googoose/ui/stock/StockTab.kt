package com.example.googoose.ui.stock

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.googoose.data.Strings
import com.example.googoose.data.model.StockItem
import com.example.googoose.ui.components.GhostIconButton
import com.example.googoose.ui.components.GooGooseCard
import com.example.googoose.ui.components.GooGooseTextField
import com.example.googoose.ui.components.SecondaryButton
import com.example.googoose.ui.components.TagOutline
import com.example.googoose.ui.theme.GooGooseColors
import com.example.googoose.ui.theme.GooGooseType
import com.example.googoose.util.formatQty
import com.example.googoose.viewmodel.GooGooseLogic
import com.example.googoose.viewmodel.GooGooseUiState
import com.example.googoose.viewmodel.GooGooseViewModel

@Composable
fun StockTab(state: GooGooseUiState, strings: Strings, viewModel: GooGooseViewModel, modifier: Modifier = Modifier) {
    val lowCount = GooGooseLogic.lowStockCount(state.stock)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("$lowCount ${strings.lowSuffix}", style = GooGooseType.caption, color = GooGooseColors.textMuted)
                SecondaryButton(strings.addItem, onClick = viewModel::openAddStock, leadingIcon = Icons.Outlined.Add)
            }
        }
        items(state.stock, key = { it.id }) { item ->
            StockCard(
                item = item,
                low = item.qty <= item.low,
                amount = state.stockAmounts[item.id] ?: "1",
                strings = strings,
                onUnitChange = { viewModel.setStockUnit(item.id, it) },
                onAmountChange = { viewModel.setStockAmount(item.id, it) },
                onIncrease = { viewModel.requestIncrease(item.id) },
                onDecrease = { viewModel.requestDecrease(item.id) },
                onRemove = { viewModel.removeStock(item.id) },
            )
        }
    }
}

@Composable
private fun StockCard(
    item: StockItem,
    low: Boolean,
    amount: String,
    strings: Strings,
    onUnitChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit,
) {
    GooGooseCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(item.name, style = GooGooseType.cardTitle, color = if (low) GooGooseColors.error else GooGooseColors.text)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (low) TagOutline(strings.lowTag)
                GhostIconButton(Icons.Outlined.Delete, strings.removeItem, onRemove)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                formatQty(item.qty),
                style = GooGooseType.cardTitle.copy(fontSize = 17.sp, fontWeight = FontWeight.SemiBold),
                color = GooGooseColors.text,
            )
            GooGooseTextField(
                value = item.unit,
                onValueChange = onUnitChange,
                modifier = Modifier.width(52.dp),
                textAlign = TextAlign.Center,
            )
        }
        Text(
            strings.lowThresholdLabel("${formatQty(item.low)} ${item.unit}"),
            style = GooGooseType.caption,
            color = if (low) GooGooseColors.error else GooGooseColors.textMuted,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StepperButton("−", onDecrease)
            GooGooseTextField(
                value = amount,
                onValueChange = onAmountChange,
                modifier = Modifier.width(64.dp),
                textAlign = TextAlign.Center,
                keyboardType = KeyboardType.Decimal,
            )
            StepperButton("+", onIncrease)
        }
    }
}

@Composable
private fun StepperButton(symbol: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, GooGooseColors.divider, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(symbol, style = GooGooseType.h5, color = GooGooseColors.text)
    }
}
