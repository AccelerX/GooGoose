package com.example.googoose.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.googoose.ui.theme.GooGooseColors
import com.example.googoose.ui.theme.GooGooseSpacing
import com.example.googoose.ui.theme.GooGooseType
import com.example.googoose.ui.theme.NocturneNeutral800

/** `.card.elev-sm` */
@Composable
fun GooGooseCard(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(GooGooseSpacing.space2),
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(GooGooseColors.surface)
            .border(1.dp, NocturneNeutral800, RoundedCornerShape(8.dp))
            .padding(GooGooseSpacing.space3),
        verticalArrangement = verticalArrangement,
    ) { content() }
}

/** `.card-kicker` */
@Composable
fun CardKicker(text: String, modifier: Modifier = Modifier) {
    Text(text.uppercase(), style = GooGooseType.cardKicker, color = GooGooseColors.accent, modifier = modifier)
}

@Composable
private fun BaseTag(
    text: String,
    fg: Color,
    modifier: Modifier = Modifier,
    bg: Color = Color.Transparent,
    outlineColor: Color? = null,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .then(if (outlineColor != null) Modifier.border(1.dp, outlineColor, RoundedCornerShape(6.dp)) else Modifier)
            .padding(horizontal = 10.dp, vertical = 3.dp),
    ) {
        Text(text, style = GooGooseType.tag, color = fg)
    }
}

@Composable
fun TagAccent(text: String, modifier: Modifier = Modifier) =
    BaseTag(text = text, fg = GooGooseColors.tagAccentFg, modifier = modifier, bg = GooGooseColors.tagAccentBg)

@Composable
fun TagNeutral(text: String, modifier: Modifier = Modifier) =
    BaseTag(text = text, fg = GooGooseColors.tagNeutralFg, modifier = modifier, bg = GooGooseColors.tagNeutralBg)

@Composable
fun TagOutline(text: String, modifier: Modifier = Modifier) =
    BaseTag(text = text, fg = GooGooseColors.accent, modifier = modifier, outlineColor = GooGooseColors.accent)

/** `.seg`/`.seg-opt` — generic single-select segmented control. */
@Composable
fun <T> SegmentedControl(
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, GooGooseColors.divider, RoundedCornerShape(8.dp)),
    ) {
        options.forEachIndexed { index, (value, label) ->
            val isSelected = value == selected
            if (index > 0) {
                Box(modifier = Modifier.width(1.dp).height(32.dp).background(GooGooseColors.divider))
            }
            Box(
                modifier = Modifier
                    .background(if (isSelected) GooGooseColors.accent.copy(alpha = 0.14f) else Color.Transparent)
                    .clickable { onSelect(value) }
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    style = GooGooseType.bodySmall.copy(fontSize = 13.sp),
                    color = if (isSelected) GooGooseColors.accent else GooGooseColors.text,
                )
            }
        }
    }
}

/** `.btn.btn-icon.btn-ghost` */
@Composable
fun GhostIconButton(icon: ImageVector, contentDescription: String?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onClick, modifier = modifier.size(36.dp)) {
        Icon(icon, contentDescription = contentDescription, tint = GooGooseColors.accent)
    }
}

/** `.btn.btn-secondary` — padding/font match [PrimaryTextButton]/[DangerButton] exactly so a Cancel/Save (or Cancel/Delete) pair renders the same height. */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, GooGooseColors.divider, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, contentDescription = null, tint = GooGooseColors.text, modifier = Modifier.size(14.dp))
        }
        Text(text, style = GooGooseType.bodySmall, color = GooGooseColors.text)
    }
}

/** `.btn.btn-primary` */
@Composable
fun PrimaryTextButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, GooGooseColors.accent, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(text, style = GooGooseType.bodySmall, color = GooGooseColors.accent)
    }
}

/** [PrimaryTextButton]'s exact shape/sizing in the app's reserved error red — for confirmed-destructive actions only (e.g. delete). */
@Composable
fun DangerButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, GooGooseColors.error, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(text, style = GooGooseType.bodySmall, color = GooGooseColors.error)
    }
}

/** `.field > label` */
@Composable
fun FieldLabel(text: String, modifier: Modifier = Modifier) {
    Text(text, style = GooGooseType.label, color = GooGooseColors.textMuted, modifier = modifier.padding(bottom = 5.dp))
}

/** `.input` — plain bordered box; the label sits above it via [FieldLabel], not floating inside. */
@Composable
fun GooGooseTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
    textAlign: TextAlign = TextAlign.Start,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(GooGooseColors.surface)
            .border(1.dp, GooGooseColors.divider, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        if (value.isEmpty() && placeholder != null) {
            Text(placeholder, style = GooGooseType.bodySmall, color = GooGooseColors.textMuted, textAlign = textAlign)
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = singleLine,
            minLines = minLines,
            textStyle = GooGooseType.bodySmall.copy(color = GooGooseColors.text, textAlign = textAlign),
            cursorBrush = SolidColor(GooGooseColors.accent),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        )
    }
}
