package hu.bme.aut.android.hw.ui.screen.charts

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hu.bme.aut.android.hw.ui.component.CategoryLegend
import hu.bme.aut.android.hw.ui.component.CategoryPieChart
import hu.bme.aut.android.hw.viewmodel.TransactionViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import hu.bme.aut.android.hw.utils.TypeFilter
import kotlin.random.Random
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bme.aut.android.hw.R
import hu.bme.aut.android.hw.domain.model.CategorySlice
import hu.bme.aut.android.hw.utils.getCategoryLabels
import hu.bme.aut.android.hw.viewmodel.CurrencyViewModel
import kotlinx.datetime.toJavaLocalDate
import java.time.YearMonth
import kotlin.math.absoluteValue
enum class ChartType { INCOME, EXPENSE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartsScreen(
    viewModel: TransactionViewModel,
    onBack: () -> Unit
) {

    val allTx     by viewModel.repo.transactions.collectAsState(emptyList())
    val filter    by viewModel.filter.collectAsState()

    var expandedMonth by remember { mutableStateOf(false) }
    var selMonth      by rememberSaveable { mutableStateOf<YearMonth?>(null) }

    // C) build the list of months available
    val months = remember(allTx) {
        allTx.map { YearMonth.from(it.date.toJavaLocalDate()) }
            .distinct().sorted()
    }

    val filtered = remember(allTx, selMonth) {
        selMonth?.let { month ->
            allTx.filter {
                YearMonth.from(it.date.toJavaLocalDate()) == month
            }
        } ?: allTx
    }

    val currencyVm: CurrencyViewModel = hiltViewModel()
    val defaultCurrency by currencyVm.defaultCurrency.collectAsState()
    val rates by currencyVm.rates.collectAsState()


    val forType = when (filter.type) {
        TypeFilter.INCOME  -> filtered.filter { it.sum >= 0 }
        TypeFilter.EXPENSE -> filtered.filter { it.sum <  0 }
        TypeFilter.ALL     -> filtered
    }

    val categoryLabels = getCategoryLabels()

    val slices = remember(forType) {
        forType.groupBy { it.category }
            .entries
            .sortedByDescending { it.value.sumOf { it.sum.absoluteValue } }
            .map { (cat, rows) ->
                val currency = rows.firstOrNull()?.currency ?: defaultCurrency
                val total = rows.sumOf {
                    val from = rates[it.currency] ?: 1.0
                    val to = rates[defaultCurrency] ?: 1.0
                    it.sum.absoluteValue * (to / from)
                }
                CategorySlice(categoryLabels[cat] ?: cat, total, currency)
            }
    }

    val incomeSlices = remember(filtered, rates, defaultCurrency) {
        filtered.filter { it.sum >= 0 }
            .groupBy { it.category }
            .entries
            .sortedByDescending { entry ->
                entry.value.sumOf { tx ->
                    val from = rates[tx.currency] ?: 1.0
                    val to = rates[defaultCurrency] ?: 1.0
                    (tx.sum * (to / from))
                }
            }
            .map { (cat, rows) ->
                val total = rows.sumOf {
                    val from = rates[it.currency] ?: 1.0
                    val to = rates[defaultCurrency] ?: 1.0
                    it.sum * (to / from)
                }
                CategorySlice(categoryLabels[cat] ?: cat, total, defaultCurrency)
            }
    }


    val expenseSlices = remember(filtered, rates, defaultCurrency)  {
        filtered.filter { it.sum < 0 }
            .groupBy { it.category }
            .entries
            .sortedByDescending { entry ->
                entry.value.sumOf { tx ->
                    val from = rates[tx.currency] ?: 1.0
                    val to = rates[defaultCurrency] ?: 1.0
                    tx.sum.absoluteValue * (to / from)  // ✅ Now it's a positive amount
                }
            }
            .map { (cat, rows) ->
                val currency = rows.firstOrNull()?.currency ?: defaultCurrency
                val total = rows.sumOf {
                    val from = rates[it.currency] ?: 1.0
                    val to = rates[defaultCurrency] ?: 1.0
                    it.sum.absoluteValue * (to / from)
                }
                CategorySlice(categoryLabels[cat] ?: cat, total, defaultCurrency)
            }

    }

    val palette = remember(slices) {
        List(slices.size) {
            Color(Random.nextFloat(), Random.nextFloat(), Random.nextFloat(), 1f)
        }
    }

    val incomePalette = remember(incomeSlices) {
        List(incomeSlices.size) {
            Color(Random.nextFloat(), Random.nextFloat(), Random.nextFloat(), 1f)
        }
    }

    val expensePalette = remember(expenseSlices) {
        List(expenseSlices.size) {
            Color(Random.nextFloat(), Random.nextFloat(), Random.nextFloat(), 1f)
        }
    }

    var selectedTab by rememberSaveable { mutableStateOf(ChartType.INCOME) }




    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.charts_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded          = expandedMonth,
                onExpandedChange  = { expandedMonth = !expandedMonth }
            ) {
                OutlinedTextField(
                    readOnly      = true,
                    value         = selMonth?.toString() ?: stringResource(R.string.all_months),
                    onValueChange = {},
                    label         = { Text(stringResource(R.string.month)) },
                    trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expandedMonth) },
                    modifier      = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded         = expandedMonth,
                    onDismissRequest = { expandedMonth = false }
                ) {
                    DropdownMenuItem(
                        text    = { Text(stringResource(R.string.all_months)) },
                        onClick = { selMonth = null; expandedMonth = false }
                    )
                    months.forEach { m ->
                        DropdownMenuItem(
                            text    = { Text(m.toString()) },
                            onClick = { selMonth = m; expandedMonth = false }
                        )
                    }
                }
            }

            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == ChartType.INCOME,
                    onClick = { selectedTab = ChartType.INCOME },
                    text = { Text(stringResource(R.string.income)) }
                )
                Tab(
                    selected = selectedTab == ChartType.EXPENSE,
                    onClick = { selectedTab = ChartType.EXPENSE },
                    text = { Text(stringResource(R.string.expense)) }
                )
            }

            when (selectedTab) {
                ChartType.INCOME -> {
                    Text(
                        stringResource(R.string.income_by_cat),
                        style = MaterialTheme.typography.titleMedium
                    )

                    CategoryPieChart(
                        slices = incomeSlices,
                        colors = incomePalette,
                    )

                    Spacer(Modifier.height(24.dp))

                    CategoryLegend(
                        slices = incomeSlices,
                        colors = incomePalette,
                        defaultCurrency = defaultCurrency,
                        rates = rates
                    )
                }

                ChartType.EXPENSE -> {
                    Text(
                        stringResource(R.string.expenses_by_cat),
                        style = MaterialTheme.typography.titleMedium
                    )

                    CategoryPieChart(
                        slices = expenseSlices,
                        colors = expensePalette,
                    )

                    Spacer(Modifier.height(24.dp))

                    CategoryLegend(
                        slices = expenseSlices,
                        colors = expensePalette,
                        defaultCurrency = defaultCurrency,
                        rates = rates
                    )
                }
            }

        }
    }
}