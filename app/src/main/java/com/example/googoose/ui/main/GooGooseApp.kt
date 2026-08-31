package com.example.googoose.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.googoose.data.stringsFor
import com.example.googoose.ui.reports.ReportsTab
import com.example.googoose.ui.settings.SettingsScreen
import com.example.googoose.ui.stock.AddStockItemSheet
import com.example.googoose.ui.stock.StockConfirmDialog
import com.example.googoose.ui.stock.StockTab
import com.example.googoose.ui.theme.GooGooseColors
import com.example.googoose.ui.todo.AddTodoSheet
import com.example.googoose.ui.todo.TodoTab
import com.example.googoose.ui.transactions.AddTransactionSheet
import com.example.googoose.ui.transactions.EditTransactionScreen
import com.example.googoose.ui.transactions.TransactionsTab
import com.example.googoose.viewmodel.GooGooseViewModel

/**
 * Root composable — mirrors Till.dc.html's own layering: 4 tabs behind the
 * bottom nav, then the two full-screen overlays (Settings/Edit transaction),
 * then the sheets and the stock-confirm dialog on top of everything.
 */
@Composable
fun GooGooseApp(viewModel: GooGooseViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.state.collectAsState()
    val strings = stringsFor(state.language)

    val tabTitle = when (state.tab) {
        1 -> strings.stockTab
        2 -> strings.reports
        3 -> strings.todoTab
        else -> strings.transactions
    }

    // enableEdgeToEdge() (in MainActivity) draws behind the system bars, so the
    // background fills edge-to-edge but content is inset from status/nav bars.
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(GooGooseColors.background)
            .windowInsetsPadding(WindowInsets.systemBars),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppHeader(
                appName = strings.appName,
                tabTitle = tabTitle,
                businessName = state.settingsName,
                strings = strings,
                onSettingsClick = viewModel::openSettings,
            )
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (state.tab) {
                    1 -> StockTab(state = state, strings = strings, viewModel = viewModel)
                    2 -> ReportsTab(state = state, strings = strings, viewModel = viewModel)
                    3 -> TodoTab(state = state, strings = strings, viewModel = viewModel)
                    else -> TransactionsTab(state = state, strings = strings, viewModel = viewModel)
                }
            }
            BottomNavBar(
                currentTab = state.tab,
                onTabClick = viewModel::goTab,
                onFabClick = viewModel::openSheet,
                strings = strings,
            )
        }

        if (state.showSettings) {
            BackHandler(onBack = viewModel::closeSettings)
            SettingsScreen(state = state, strings = strings, viewModel = viewModel)
        }

        val detailItem = state.detailItem
        if (detailItem != null) {
            BackHandler(onBack = viewModel::closeDetail)
            EditTransactionScreen(item = detailItem, state = state, strings = strings, viewModel = viewModel)
        }

        if (state.showSheet) {
            AddTransactionSheet(state = state, strings = strings, viewModel = viewModel)
        }

        if (state.showAddStock) {
            AddStockItemSheet(state = state, strings = strings, viewModel = viewModel)
        }

        if (state.showAddTodo) {
            AddTodoSheet(state = state, strings = strings, viewModel = viewModel)
        }

        val stockConfirm = state.stockConfirm
        if (stockConfirm != null) {
            StockConfirmDialog(confirm = stockConfirm, strings = strings, viewModel = viewModel)
        }
    }
}
