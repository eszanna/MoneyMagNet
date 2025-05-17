
package hu.bme.aut.android.hw.ui.component

import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.location.LocationServices
import hu.bme.aut.android.hw.R
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import hu.bme.aut.android.hw.domain.model.Transaction
import hu.bme.aut.android.hw.viewmodel.CurrencyViewModel
import hu.bme.aut.android.hw.viewmodel.TransactionViewModel
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    onDismiss: () -> Unit,
    viewModel: TransactionViewModel,
    onAdd: (Transaction) -> Unit
) {
    // Local UI state
    var amountText by remember { mutableStateOf("") }
    var descriptionText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Common") }
    var newCategoryText by remember { mutableStateOf("") }

    // Delete-category confirmation
    var toDeleteCategory by remember { mutableStateOf<String?>(null) }

    // Date picker
    val context = LocalContext.current
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    var selectedDate by remember { mutableStateOf(today) }
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")

    // Collect categories from ViewModel
    val categories by viewModel.categories.collectAsState()

    var isIncome          by remember { mutableStateOf(true) }

    var lat by remember { mutableStateOf<Double?>(null) }
    var lon by remember { mutableStateOf<Double?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {

                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    val client = LocationServices
                        .getFusedLocationProviderClient(context)
                    client.lastLocation
                        .addOnSuccessListener { location ->
                            lat = location?.latitude
                            lon = location?.longitude
                        }
                }
            }
        }
    )
    val currencyVm: CurrencyViewModel = hiltViewModel()
    val ratesMap   by currencyVm.rates.collectAsState()       // Flow<Map<String,Double>>
    val rateCodes = ratesMap.keys.sorted()

    val defaultCurr by currencyVm.defaultCurrency.collectAsState()

    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedCurrency = selected ?: defaultCurr

    var searchQuery by remember{ mutableStateOf("")}

    var isDropdownOpen by remember { mutableStateOf(false) }

    val defaultCategoryKeys = listOf("Common", "Food", "Work", "Transport", "Entertainment")


    val categoryLabels = mapOf(
        "Common"       to stringResource(R.string.category_common),
        "Food"         to stringResource(R.string.category_food),
        "Work"         to stringResource(R.string.category_work),
        "Transport"    to stringResource(R.string.category_transport),
        "Entertainment" to stringResource(R.string.category_entertainment),
    )

    // kick it off once, when this dialog first appears
    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_transaction_title)) },
        text = {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                // Amount input
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text(stringResource(R.string.amount_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                // Description input
                OutlinedTextField(
                    value = descriptionText,
                    onValueChange = { descriptionText = it },
                    label = { Text(stringResource(R.string.description_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                /* ─ Income / Expense toggle ─ */
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                    FilterChip(
                        selected = isIncome,
                        onClick  = { isIncome = true },
                        label    = { Text(stringResource(R.string.income)) }
                    )
                    FilterChip(
                        selected = !isIncome,
                        onClick  = { isIncome = false },
                        label    = { Text(stringResource(R.string.expense)) }
                    )
                }
                Spacer(Modifier.height(8.dp))
                // Category selector with delete
                CategorySelector(
                    categories = categories,
                    defaultCategories = defaultCategoryKeys,
                    selectedCategory = selectedCategory,
                    onCategoryChange = { selectedCategory = it },
                    onDeleteCategory = { toDeleteCategory = it }
                )
                Spacer(Modifier.height(8.dp))
                // New category input
                OutlinedTextField(
                    value = newCategoryText,
                    onValueChange = { newCategoryText = it },
                    label = { Text(stringResource(R.string.new_category_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = {
                        val trimmed = newCategoryText.trim()
                        if (trimmed.isNotBlank()) {
                            viewModel.addCategory(trimmed)
                            selectedCategory = trimmed
                            newCategoryText = ""
                        }
                    },
                    enabled = newCategoryText.isNotBlank(),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(R.string.save_category))
                }
                Spacer(Modifier.height(8.dp))
                // Date picker button
                TextButton(onClick = {
                    val javaDate = selectedDate.toJavaLocalDate()
                    DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            selectedDate = LocalDate(year, month + 1, day)
                        },
                        javaDate.year,
                        javaDate.monthValue - 1,
                        javaDate.dayOfMonth
                    ).show()
                }) {
                    Text(
                        text = stringResource(
                            R.string.select_date,
                            selectedDate.toJavaLocalDate().format(dateFormatter)
                        )
                    )
                }

                Text(stringResource(R.string.currency_label))

                Box {
                    Column {
                        OutlinedTextField(
                            value = selectedCurrency,
                            onValueChange = {},
                            label = { Text(stringResource(R.string.currency_label)) },
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { isDropdownOpen = !isDropdownOpen }) {
                                    Icon(
                                        imageVector = if (isDropdownOpen) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    DropdownMenu(
                        expanded = isDropdownOpen,
                        onDismissRequest = { isDropdownOpen = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 250.dp) // scrollable!
                    ) {
                        // Search bar inside dropdown
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text(stringResource(R.string.search)) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        )

                        val filtered = rateCodes.filter {
                            it.contains(searchQuery.trim(), ignoreCase = true)
                        }

                        if (filtered.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.no_match)) },
                                onClick = { }
                            )
                        } else {
                            filtered.forEach { code ->
                                DropdownMenuItem(
                                    text = { Text(code) },
                                    onClick = {
                                        selected = code
                                        isDropdownOpen = false
                                        searchQuery = ""
                                    }
                                )
                            }
                        }
                    }
                }

            }
        },
        confirmButton = {
            TextButton(onClick = {
                // Add transaction
                val raw = amountText.toDoubleOrNull() ?: return@TextButton
                val finalAmount = if (isIncome) raw else -raw

                onAdd(Transaction(
                    id          = System.currentTimeMillis(),
                    sum         = finalAmount,
                    description = descriptionText,
                    date        = selectedDate,
                    category    = selectedCategory,
                    latitude = lat,
                    longitude = lon,
                    currency = selectedCurrency
                ))

                onDismiss()
            }) {
                Text(stringResource(R.string.add_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_button))
            }
        }
    )

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
