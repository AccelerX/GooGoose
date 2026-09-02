package com.example.googoose.ui.todo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.googoose.data.Strings
import com.example.googoose.data.model.TodoItem
import com.example.googoose.ui.components.GooGooseCard
import com.example.googoose.ui.components.PrimaryTextButton
import com.example.googoose.ui.components.SecondaryButton
import com.example.googoose.ui.theme.GooGooseColors
import com.example.googoose.ui.theme.GooGooseType
import com.example.googoose.viewmodel.GooGooseLogic
import com.example.googoose.viewmodel.GooGooseUiState
import com.example.googoose.viewmodel.GooGooseViewModel

@Composable
fun TodoTab(state: GooGooseUiState, strings: Strings, viewModel: GooGooseViewModel, modifier: Modifier = Modifier) {
    // Undone first (original order), done appended after (original order) — see GooGooseLogic.sortedTodos.
    val rows = GooGooseLogic.sortedTodos(state.todos)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            SecondaryButton(
                strings.addTask,
                onClick = viewModel::openAddTodo,
                leadingIcon = Icons.Outlined.Add,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (rows.isEmpty()) {
            item {
                Text(strings.emptyTodos, style = GooGooseType.bodySmall, color = GooGooseColors.textMuted)
            }
        }
        items(rows, key = { it.id }) { todo ->
            TodoCard(
                todo = todo,
                strings = strings,
                onToggle = { viewModel.toggleTodo(todo.id) },
                onOpenDetail = { viewModel.openTodoDetail(todo.id) },
            )
        }
    }
}

@Composable
private fun TodoCard(todo: TodoItem, strings: Strings, onToggle: () -> Unit, onOpenDetail: () -> Unit) {
    GooGooseCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenDetail)) {
        Text(
            todo.title,
            style = GooGooseType.cardTitle,
            // textMuted is text-color-at-55%-alpha, which is exactly the mockup's `opacity:0.55`.
            color = if (todo.done) GooGooseColors.textMuted else GooGooseColors.text,
            textDecoration = if (todo.done) TextDecoration.LineThrough else null,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            todo.desc,
            style = GooGooseType.bodySmall.copy(fontSize = 13.sp),
            color = GooGooseColors.textMuted,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (todo.done) {
                PrimaryTextButton(strings.doneLabel, onClick = onToggle)
            } else {
                SecondaryButton(strings.markDone, onClick = onToggle)
            }
        }
    }
}
