package hu.bme.aut.android.hw.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import hu.bme.aut.android.hw.R

/*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelector(
    categories: List<String>,
    selectedCategory: String,
    onCategoryChange: (String) -> Unit,
    defaultCategories: List<String>,
    onDeleteCategory: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            readOnly = true,
            value = selectedCategory,
            onValueChange = {},
            label = { Text(stringResource(R.string.cat)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { cat ->
                DropdownMenuItem(
                    text = { Text(cat) },
                    onClick = {
                        onCategoryChange(cat)
                        expanded = false
                    },
                    trailingIcon = {
                        if(cat !in defaultCategories){
                            IconButton(
                                onClick = {
                                    onDeleteCategory(cat)
                                    expanded = false
                                }
                            ) {
                                Icon(
                                imageVector   = Icons.Default.Delete,
                                contentDescription = "Delete $cat"
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}

*/


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelector(
    categories: List<String>,
    selectedCategory: String,
    onCategoryChange: (String) -> Unit,
    defaultCategories: List<String>,
    onDeleteCategory: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val defaultCategoryKeys = listOf("Common", "Food", "Work", "Transport", "Entertainment")


    val categoryLabels = mapOf(
        "Common"       to stringResource(R.string.category_common),
        "Food"         to stringResource(R.string.category_food),
        "Work"         to stringResource(R.string.category_work),
        "Transport"    to stringResource(R.string.category_transport),
        "Entertainment" to stringResource(R.string.category_entertainment),
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            readOnly = true,
            value = categoryLabels[selectedCategory] ?: selectedCategory,
            onValueChange = {},
            label = { Text(stringResource(R.string.cat)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { cat ->
                val isDefault = cat in defaultCategoryKeys
                val label = categoryLabels[cat] ?: cat
                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(label)
                            if (!isDefault) {
                                IconButton(
                                    onClick = {
                                        onDeleteCategory(cat)
                                        expanded = false
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete $label"
                                    )
                                }
                            }
                        }
                    },
                    onClick = {
                        onCategoryChange(cat)
                        expanded = false
                    }
                )
            }
        }
    }
}
