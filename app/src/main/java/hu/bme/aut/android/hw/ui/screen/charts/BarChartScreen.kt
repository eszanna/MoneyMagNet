package hu.bme.aut.android.hw.ui.screen.charts

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bme.aut.android.hw.R
import hu.bme.aut.android.hw.domain.model.MonthBars
import hu.bme.aut.android.hw.viewmodel.CurrencyViewModel
import hu.bme.aut.android.hw.viewmodel.TransactionViewModel
import kotlinx.datetime.toJavaLocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarChartScreen(
    viewModel: TransactionViewModel,
    onBack: () -> Unit
) {

    var expanded by remember { mutableStateOf(false) }

    val currencyVm: CurrencyViewModel = hiltViewModel()

    val transactions by viewModel.repo.transactions.collectAsState(emptyList())
    val defaultCurrency by currencyVm.defaultCurrency.collectAsState()
    val rates by currencyVm.rates.collectAsState()

    val monthlyBars = remember(rates, defaultCurrency, transactions) {
        transactions
            .groupBy { YearMonth.from(it.date.toJavaLocalDate()) }
            .map { (month, txList) ->
                val income = txList.filter { it.sum >= 0 }.sumOf { tx ->
                    val from = rates[tx.currency] ?: 1.0
                    val to = rates[defaultCurrency] ?: 1.0
                    tx.sum * (to / from)
                }
                val expense = txList.filter { it.sum < 0 }.sumOf { tx ->
                    val from = rates[tx.currency] ?: 1.0
                    val to = rates[defaultCurrency] ?: 1.0
                    -tx.sum * (to / from)
                }

                MonthBars(month, income, expense, currency = defaultCurrency)
            }
            .sortedBy { it.month }
    }

    LaunchedEffect(transactions) {
        Log.d("BarChartDebug", "First TX currency: ${transactions.firstOrNull()?.currency}")
    }

    val allBars = monthlyBars

    if (allBars.isEmpty()) {
        EmptyChart(onBack); return
    }

    var picked by remember { mutableStateOf(allBars.last()) }

    var pickedMonth by remember { mutableStateOf(allBars.last().month) }

    val selectedBar = monthlyBars.firstOrNull { it.month == pickedMonth } ?: return
    val rateFrom = rates[selectedBar.currency] ?: 1.0
    val rateTo = rates[defaultCurrency] ?: 1.0

    val convertedBar = selectedBar.copy(
        income = selectedBar.income * (rateTo / rateFrom),
        expense = selectedBar.expense * (rateTo / rateFrom),
        currency = defaultCurrency
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.income_vs_expense)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            /* 3 ── Month chooser */
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = pickedMonth.formatMonth(),
                    onValueChange = {},
                    label = { Text(stringResource(R.string.month)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.menuAnchor()
                )
                ExposedDropdownMenu(expanded, onDismissRequest = { expanded = false }) {
                    allBars.forEach { bar ->
                        DropdownMenuItem(
                            text = { Text(bar.month.formatMonth()) },
                            onClick = {
                                pickedMonth = bar.month
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            IncomeExpenseBars(bar = convertedBar, defaultCurrency = defaultCurrency)

            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendChip(label = stringResource(R.string.income),   color = Color(0xFF4CAF50))
                LegendChip(label = stringResource(R.string.expense),  color = Color(0xFFF44336))
            }
        }
    }
}

@Composable
private fun IncomeExpenseBars(bar: MonthBars,   defaultCurrency: String, modifier: Modifier = Modifier.height(200.dp)) {
    val max = maxOf(bar.income, bar.expense, 1.0)
    val measurer = rememberTextMeasurer()

    val animatedIncome = remember { Animatable(0f) }
    val animatedExpense = remember { Animatable(0f) }

    LaunchedEffect(bar) {
        animatedIncome.animateTo(
            targetValue = bar.income.toFloat(),
            animationSpec = tween(durationMillis = 1000)
        )
        animatedExpense.animateTo(
            targetValue = bar.expense.toFloat(),
            animationSpec = tween(durationMillis = 1000)
        )
    }

    Canvas(modifier.fillMaxWidth()) {
        val totalWidth = size.width
        val barWidth   = totalWidth / 6f          // ⅙ of width each
        val gap        = barWidth                 // ⅓ width between bars

        fun barHeight(value: Double) = size.height * (value / max).toFloat()

        val incH = barHeight(animatedIncome.value.toDouble())
        val incY = size.height - incH

        // income (green, left)
        drawRect(
            color = Color(0xFF4CAF50),
            topLeft = Offset(gap, size.height - barHeight(bar.income)),
            size = Size(barWidth, barHeight(bar.income))
        )
        // amount label
        //val incTxt = measurer.measure(AnnotatedString("%.2f ${bar.currency}".format(bar.income)))
        val incTxt = measurer.measure(AnnotatedString("%.2f $defaultCurrency".format(bar.income)))

        drawText(
            textLayoutResult = incTxt,
            color            = Color.Black,          // ← add this
            topLeft          = Offset(
                x = gap + barWidth / 2 - incTxt.size.width / 2,
                y = incY - 4.dp.toPx() - incTxt.size.height
            )
        )
        val expH = barHeight(animatedExpense.value.toDouble())
        val expX = 2 * gap + barWidth
        val expY = size.height - expH

        // expense (red, right)
        drawRect(
            color = Color(0xFFF44336),
            topLeft = Offset(2 * gap + barWidth, size.height - barHeight(bar.expense)),
            size = Size(barWidth, barHeight(bar.expense))
        )

        //val expTxt = measurer.measure(AnnotatedString("%.2f ${bar.currency}".format(bar.expense)))
        val expTxt = measurer.measure(AnnotatedString("%.2f $defaultCurrency".format(bar.expense)))
        drawText(
            textLayoutResult = expTxt,
            color            = Color.Black,
            topLeft          = Offset(
                x = expX + barWidth / 2 - expTxt.size.width / 2,
                y = expY - 4.dp.toPx() - expTxt.size.height
            )
        )
    }
}

private fun YearMonth.formatMonth(): String =
    "${month.getDisplayName(TextStyle.SHORT, Locale.getDefault())} $year"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmptyChart(onBack: () -> Unit) = Scaffold(
    topBar = {
        CenterAlignedTopAppBar(
            title = { Text(stringResource(R.string.income_vs_expense)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
                }
            }
        )
    }
) { inner ->
    Box(Modifier.fillMaxSize().padding(inner), contentAlignment = Alignment.Center) {
        Text(stringResource(R.string.no_transactions))
    }
}

@Composable
private fun LegendChip(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(12.dp).background(color, CircleShape))
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}
