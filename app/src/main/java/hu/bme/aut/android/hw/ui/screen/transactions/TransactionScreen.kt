package hu.bme.aut.android.hw.ui.screen.transactions

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.bme.aut.android.hw.domain.model.Transaction
import hu.bme.aut.android.hw.ui.component.TransactionItem
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import hu.bme.aut.android.hw.viewmodel.TransactionViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.toJavaLocalDate
import java.time.YearMonth
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bme.aut.android.hw.ui.component.FilterRow
import hu.bme.aut.android.hw.utils.formatAmount
import hu.bme.aut.android.hw.R
import hu.bme.aut.android.hw.viewmodel.CurrencyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(
    transactions: List<Transaction>,
    onAddTransactionClick: () -> Unit,
    onTransactionClick: (Long) -> Unit,
    viewModel: TransactionViewModel,
    modifier: Modifier = Modifier
) {

    val monthFilter by viewModel.monthFilter.collectAsState()
    val months = remember(transactions) {
        transactions
            .map { YearMonth.from(it.date.toJavaLocalDate()) }
            .distinct()
            .sorted()
    }
    var expanded by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope             = rememberCoroutineScope()
    val filter by viewModel.filter.collectAsState()
    val categories by viewModel.categories.collectAsState()

    val currencyVm: CurrencyViewModel = hiltViewModel()
    val currency by currencyVm.defaultCurrency.collectAsState()

    val filteredTxs by viewModel.uiState.collectAsState()
    val defaultCurrency by currencyVm.defaultCurrency.collectAsState()
    val rates by currencyVm.rates.collectAsState()

    val convertedFilteredSum = filteredTxs.sumOf { tx ->
        val fromRate = rates[tx.currency] ?: 1.0
        val toRate = rates[defaultCurrency] ?: 1.0
        tx.sum * (toRate / fromRate)
    }

    Scaffold(

        modifier       = modifier,
        contentWindowInsets  = WindowInsets(0),
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTransactionClick) { Text("+") }
        },
                floatingActionButtonPosition = FabPosition.Center

    ) { innerPadding ->

        val filteredSum   by viewModel.filteredSum.collectAsState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(top = 0.dp)
                .padding(horizontal = 16.dp),
        ) {

            FilterRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                filter     = filter,
                categories = categories,
                onTypeClick     = viewModel::setType,
                onCategoryClick = { viewModel.setCategory(it) }
            )

            Spacer(Modifier.height(4.dp))

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    readOnly      = true,
                    value         = monthFilter?.toString() ?: stringResource(R.string.all_months),
                    onValueChange = {},
                    label         = { Text(stringResource(R.string.month)) },
                    trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier      = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded         = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text    = { Text(stringResource(R.string.all_months)) },
                        onClick = { viewModel.setMonthFilter(null); expanded = false }
                    )
                    months.forEach { m ->
                        DropdownMenuItem(
                            text    = { Text(m.toString()) },
                            onClick = { viewModel.setMonthFilter(m); expanded = false }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                    text = stringResource(
                        R.string.filtered_balance_text,
                        formatAmount(convertedFilteredSum),
                        defaultCurrency
                    ),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyColumn(
                modifier       = Modifier.fillMaxWidth() .weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(transactions, key = { it.id }) { tx ->

                    TransactionItem(
                        transaction = tx,
                        onClick     = { onTransactionClick(tx.id) },
                        onDelete    = {
                            // 1) delete immediately
                            viewModel.delete(tx)

                            // 2) snackbar with undo
                            scope.launch {
                                val res = snackbarHostState.showSnackbar(
                                    message     = "Deleted “${tx.description}”",
                                    actionLabel = "UNDO",
                                    duration    = SnackbarDuration.Short
                                )
                                if (res == SnackbarResult.ActionPerformed) {
                                    viewModel.add(tx)    // undo
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
