package hu.bme.aut.android.hw.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bme.aut.android.hw.R
import hu.bme.aut.android.hw.utils.formatAmount
import hu.bme.aut.android.hw.viewmodel.CurrencyViewModel
import hu.bme.aut.android.hw.viewmodel.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainSummaryScreen(
    totalBalance: Double,
    onViewTransactionsClick: () -> Unit,
    onBack: () -> Unit,

) {

    val composition by rememberLottieComposition(
        LottieCompositionSpec.Asset("piggybank.json")
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        speed = 1.0f,
        restartOnPlay = true
    )

    val currencyVm: CurrencyViewModel = hiltViewModel()
    val defaultCurrency by currencyVm.defaultCurrency.collectAsState()
    val rates by currencyVm.rates.collectAsState()

    val transactionVm: TransactionViewModel = hiltViewModel()
    val transactions by transactionVm.uiState.collectAsState()

    val groupedSums = transactions
        .groupBy { it.currency }
        .mapValues { (_, list) -> list.sumOf { it.sum } }

    // Step 2: Convert each currency to the defaultCurrency
    val convertedTotal = groupedSums.entries.sumOf { (currency, sum) ->
        if (currency == defaultCurrency) {
            sum
        } else {
            val fromRate = rates[currency]
            val toRate = rates[defaultCurrency]
            if (fromRate != null && toRate != null) {
                sum * (toRate / fromRate)
            } else 0.0
        }
    }


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.summary_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LottieAnimation(
                composition = composition,
                progress = progress,
                modifier = Modifier.size(200.dp)
            )

            Spacer(Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.your_balance),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // Scrollable currency list
            Column(
                modifier = Modifier
                    .weight(1f) // makes it scrollable by filling available space
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                groupedSums.forEach { (currency, sum) ->
                    Text(
                        text = "${formatAmount(sum)} $currency",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Divider()
            Spacer(Modifier.height(12.dp))

            // Total balance label (separate line)
            Text(
                text = stringResource(R.string.total_balance_label), // "Total Balance"
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // Total balance value (separate line)
            Text(
                text = stringResource(
                    R.string.amount_with_currency,
                    formatAmount(convertedTotal),
                    defaultCurrency
                ),
                style = MaterialTheme.typography.displaySmall,
                color = if (convertedTotal >= 0) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            Button(onClick = onViewTransactionsClick) {
                Text(stringResource(R.string.view_transactions))
            }
        }
    }
}