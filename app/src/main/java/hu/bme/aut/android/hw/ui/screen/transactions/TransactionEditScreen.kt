package hu.bme.aut.android.hw.ui.screen.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bme.aut.android.hw.R
import hu.bme.aut.android.hw.domain.model.Transaction
import hu.bme.aut.android.hw.ui.component.CategorySelector
import hu.bme.aut.android.hw.utils.getCategoryLabels
import hu.bme.aut.android.hw.viewmodel.CurrencyViewModel
import hu.bme.aut.android.hw.viewmodel.TransactionViewModel
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionEditScreen(
    transaction: Transaction,
    viewModel: TransactionViewModel,
    onSave: (Transaction) -> Unit,
    onCancel: () -> Unit
) {
    val categories by viewModel.categories.collectAsState()
    var category by remember { mutableStateOf(transaction.category) }

    var selectedCurrency by remember { mutableStateOf(transaction.currency) }

    val currencyVm: CurrencyViewModel = hiltViewModel()
    val rates by currencyVm.rates.collectAsState()


    var description by remember { mutableStateOf(transaction.description) }
    var amountText by remember { mutableStateOf(transaction.sum.absoluteValue.toString()) }
    var isIncome by remember { mutableStateOf(transaction.sum >= 0) }
    var selectedDate by remember { mutableStateOf(transaction.date) }

    var newCategoryText by remember { mutableStateOf("") }

    val dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }
    // Delete-category confirmation
    var toDeleteCategory by remember { mutableStateOf<String?>(null) }

    val defaultCategoryKeys = listOf("Common", "Food", "Work", "Transport", "Entertainment")
    val categoryLabels = mapOf(
        "Common"       to stringResource(R.string.category_common),
        "Food"         to stringResource(R.string.category_food),
        "Work"         to stringResource(R.string.category_work),
        "Transport"    to stringResource(R.string.category_transport),
        "Entertainment" to stringResource(R.string.category_entertainment),
    )
    var selectedCategory by remember { mutableStateOf("Common") }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_transaction)) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.description_label)) },
                modifier = Modifier.fillMaxWidth()
            )
            // Amount
            OutlinedTextField(
                value = amountText,
                onValueChange = { new ->
                    if (new.isEmpty() || new.matches("\\d*\\.?\\d*".toRegex())) {
                        amountText = new
                    }
                },
                label = { Text(stringResource(R.string.amount_label)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = amountText.toDoubleOrNull()?.let { it <= 0 } ?: false,
                supportingText = {
                    if (amountText.isNotEmpty() && amountText.toDoubleOrNull()?.let { it <= 0 } == true) {
                        Text(stringResource(R.string.enter_pos))
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            // Currency selector
            // Currency selector (custom dropdown)
            var currencyExpanded by remember { mutableStateOf(false) }
            var searchQuery by remember { mutableStateOf("") }

            Box {
                Column {
                    OutlinedTextField(
                        value = selectedCurrency,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.currency_label)) },
                        trailingIcon = {
                            IconButton(onClick = { currencyExpanded = !currencyExpanded }) {
                                Icon(
                                    imageVector = if (currencyExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                DropdownMenu(
                    expanded = currencyExpanded,
                    onDismissRequest = { currencyExpanded = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 250.dp) // scrollable
                ) {
                    // Search bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text(stringResource(R.string.search)) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )

                    val filtered = rates.keys
                        .sorted()
                        .filter { it.contains(searchQuery.trim(), ignoreCase = true) }

                    if (filtered.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.no_match)) },
                            onClick = {}
                        )
                    } else {
                        filtered.forEach { code ->
                            DropdownMenuItem(
                                text = { Text(code) },
                                onClick = {
                                    selectedCurrency = code
                                    currencyExpanded = false
                                    searchQuery = ""
                                }
                            )
                        }
                    }
                }
            }

            // Income/Expense
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = isIncome,
                    onClick = { isIncome = true },
                    label = { Text(stringResource(R.string.income)) }
                )
                FilterChip(
                    selected = !isIncome,
                    onClick = { isIncome = false },
                    label = { Text(stringResource(R.string.expense)) }
                )
            }
            // Date picker
            TextButton(onClick = {
                val javaDate = selectedDate.toJavaLocalDate()
                DatePickerDialog(
                    context,
                    { _, y, m, d -> selectedDate = LocalDate(y, m + 1, d) },
                    javaDate.year,
                    javaDate.monthValue - 1,
                    javaDate.dayOfMonth
                ).show()
            }) {
                Text(
                    text = stringResource(
                        R.string.date_label,
                        selectedDate.toJavaLocalDate().format(dateFormatter)
                    )
                )
            }
            // Category selector
            CategorySelector(
                categories = categories,
                defaultCategories = defaultCategoryKeys,
                selectedCategory = category,
                onCategoryChange = { category = it },
                onDeleteCategory = { toDeleteCategory = it }
            )
            // New category
            OutlinedTextField(
                value = newCategoryText,
                onValueChange = { newCategoryText = it },
                label = { Text(stringResource(R.string.new_category_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    val trimmed = newCategoryText.trim()
                    if (trimmed.isNotBlank() && trimmed !in categories) {
                        viewModel.addCategory(trimmed)
                        category = trimmed
                        newCategoryText = ""
                    }
                },
                enabled = newCategoryText.isNotBlank(),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(stringResource(R.string.save_category))
            }
            Spacer(Modifier.weight(1f))
            // Save/Cancel
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    val sumVal = amountText.toDoubleOrNull()?.let { if (isIncome) it else -it } ?: return@Button
                    onSave(transaction.copy(
                        sum = sumVal,
                        description = description,
                        date = selectedDate,
                        category = category,
                        currency = selectedCurrency
                    ))
                }) {
                    Text(stringResource(R.string.save))
                }
                OutlinedButton(onClick = onCancel) {
                    Text(stringResource(R.string.cancel_button))
                }
            }
        }
    }

    // Confirm delete category
    if (toDeleteCategory != null && toDeleteCategory !in defaultCategoryKeys) {
        val translatedCat = categoryLabels[toDeleteCategory!!] ?: toDeleteCategory!!
        AlertDialog(
            onDismissRequest = { toDeleteCategory = null },
            title = { Text(stringResource(R.string.delete_category_title, translatedCat) )},
            text = { Text(stringResource(R.string.delete_category_text, translatedCat) ) },
            confirmButton = {
                TextButton(onClick = {val cat = toDeleteCategory!!
                    viewModel.deleteCategory(cat)
                    // if we were showing that category, reset selection
                    if (selectedCategory == cat) {
                        selectedCategory = "Common"
                    }
                    toDeleteCategory = null
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { toDeleteCategory = null }) {
                    Text(stringResource(R.string.cancel_button))
                }
            }
        )
    }
}
