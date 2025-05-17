package hu.bme.aut.android.hw.ui.screen


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bme.aut.android.hw.R
import hu.bme.aut.android.hw.viewmodel.CurrencyViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExchangeRatesScreen(
    onBack: () -> Unit
) {
    val currencyVm: CurrencyViewModel = hiltViewModel()
    val rates by currencyVm.rates.collectAsState()
    val defaultCurrency by currencyVm.defaultCurrency.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    val baseRate = rates[defaultCurrency] ?: 1.0
    val filteredRates = rates
        .filterKeys { it != defaultCurrency }
        .filterKeys { it.contains(searchQuery.trim(), ignoreCase = true) }
        .toList()
        .sortedBy { it.first }

    val lastFetch by currencyVm.lastFetchTime.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.exchange_rates_for, defaultCurrency)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { inner->
        Column(
            modifier = Modifier
            .fillMaxSize()
            .padding(inner),
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text(stringResource(R.string.search)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                singleLine = true
            )

            lastFetch?.let {
                val formatted = Instant.ofEpochMilli(it)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
                    .format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"))

                Text(
                    text = stringResource(R.string.last_updated, formatted),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                        .align(Alignment.Start)
                )
            }

            LazyColumn(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredRates) { (currency, rate) ->
                    val converted = baseRate / rate
                    Text(
                        text = "1 $currency = %.4f $defaultCurrency".format(converted),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}