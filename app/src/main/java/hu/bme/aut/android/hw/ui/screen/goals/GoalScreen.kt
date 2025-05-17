package hu.bme.aut.android.hw.ui.screen.goals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import hu.bme.aut.android.hw.viewmodel.TransactionViewModel
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import hu.bme.aut.android.hw.R
import hu.bme.aut.android.hw.utils.formatAmount
import hu.bme.aut.android.hw.viewmodel.CurrencyViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalScreen(
    viewModel: TransactionViewModel,
    onBack: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var newGoalInput by remember { mutableStateOf("") }

    val totalSum by viewModel.totalSum.collectAsState()
    //val goalAmount by viewModel.goalAmount.collectAsState()  // you need to add this to your ViewModel!


    var showCongrats by remember { mutableStateOf(false) }

    val currencyVm: CurrencyViewModel = hiltViewModel()
    val defaultCurrency by currencyVm.defaultCurrency.collectAsState()
    val rates by currencyVm.rates.collectAsState()

    val goalAmount by viewModel.goalAmount.collectAsState()
    val goalCurrency by viewModel.goalCurrency.collectAsState()

    //val progress = if (goalAmount > 0) (totalSum / goalAmount).coerceIn(0.0, 1.0) else 0f

    val fromRate = rates[goalCurrency] ?: 1.0
    val toRate = rates[defaultCurrency] ?: 1.0

    val convertedGoal = goalAmount * (toRate / fromRate)
    //val convertedTotal = totalSum * (toRate / fromRate)

    val transactions by viewModel.transactions.collectAsState(initial = emptyList())
    val convertedTotal = transactions.sumOf { tx ->
        val from = rates[tx.currency] ?: 1.0
        val to = rates[defaultCurrency] ?: 1.0
        tx.sum * (to / from)
    }

    val progress = if (convertedGoal > 0) (convertedTotal / convertedGoal).coerceIn(0.0, 1.0) else 0f

    LaunchedEffect(convertedTotal, convertedGoal) {
        if (convertedGoal > 0 && convertedTotal >= convertedGoal) {
            showCongrats = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.goals)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = { showDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.set_goal))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,

        ) {
            Text(
                text = if (goalAmount > 0)
                    stringResource(R.string.goal_text, formatAmount(convertedGoal), defaultCurrency)
                else
                    stringResource(R.string.no_goal),
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(Modifier.height(32.dp))


            CircularProgressIndicator(
                progress = progress.toFloat(),
                modifier = Modifier.size(150.dp),
                strokeWidth = 12.dp
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.saved_text, formatAmount(convertedTotal), defaultCurrency),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
    if (showCongrats) {
        CongratulationsOverlay(
            onFinished = {
                viewModel.resetGoal()
                showCongrats = false
            }
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setGoalAmount(
                        amount = newGoalInput.toFloatOrNull() ?: 0f,
                        currency = defaultCurrency // <-- use CurrencyViewModel here!
                    )
                    showDialog = false
                }) { Text(stringResource(R.string.set_goal)) }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text(stringResource(R.string.cancel)) }
            },
            title = { Text(stringResource(R.string.set_new_goal)) },
            text = {
                OutlinedTextField(
                    value = newGoalInput,
                    onValueChange = { newGoalInput = it },
                    label = { Text(stringResource(R.string.goal_amount, defaultCurrency)) },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )
            }
        )
    }
}
