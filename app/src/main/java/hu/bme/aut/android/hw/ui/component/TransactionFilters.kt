package hu.bme.aut.android.hw.ui.component


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import hu.bme.aut.android.hw.R
import hu.bme.aut.android.hw.utils.TxFilter
import hu.bme.aut.android.hw.utils.TypeFilter
import hu.bme.aut.android.hw.utils.getCategoryLabels


@Composable
fun FilterRow(
    modifier: Modifier = Modifier,
    filter: TxFilter,
    categories: List<String>,
    onTypeClick: (TypeFilter) -> Unit,
    onCategoryClick: (String?) -> Unit
) {
    var typeExpanded by remember { mutableStateOf(false) }
    var catExpanded  by remember { mutableStateOf(false) }
    val categoryLabels = getCategoryLabels()

    Row(
        modifier            = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        // ↓ single “Type” dropdown chip ↓
        Box(Modifier.wrapContentSize(Alignment.TopStart)) {
            AssistChip(
                //modifier = Modifier.menuAnchor(),
                onClick  = { typeExpanded = true },
                label    = {
                    Text(stringResource(filter.type.labelRes))
                }
            )
            DropdownMenu(
                expanded         = typeExpanded,
                onDismissRequest = { typeExpanded = false }
            ) {
                TypeFilter.entries.forEach { type ->
                    DropdownMenuItem(
                        text    = { Text(stringResource(type.labelRes)) },
                        onClick = {
                            onTypeClick(type)
                            typeExpanded = false
                        }
                    )
                }
            }
        }

        // ↓ single “Category” dropdown chip ↓
        Box(Modifier.wrapContentSize(Alignment.TopStart)) {
            AssistChip(
                //modifier = Modifier.menuAnchor(),
                onClick  = { catExpanded = true },
                label    = {
                    Box(Modifier.widthIn(max = 120.dp)) {
                        Text(
                            text = filter.category?.let { categoryLabels[it] ?: it } ?: stringResource(R.string.any),
                            maxLines  = 1,
                            overflow  = TextOverflow.Ellipsis
                        )
                    }
                }
            )
            DropdownMenu(
                expanded         = catExpanded,
                onDismissRequest = { catExpanded = false }
            ) {
                DropdownMenuItem(
                    text    = { Text(stringResource(R.string.any)) },
                    onClick = {
                        onCategoryClick(null)
                        catExpanded = false
                    }
                )
                categories.forEach { cat ->
                    DropdownMenuItem(
                        text = { Text(categoryLabels[cat] ?: cat) },
                        onClick = {
                            onCategoryClick(cat)
                            catExpanded = false
                        }
                    )
                }
            }
        }
    }
}