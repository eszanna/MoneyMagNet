package hu.bme.aut.android.hw.ui.screen.transactions

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hu.bme.aut.android.hw.ui.component.AddTransactionDialog
import hu.bme.aut.android.hw.viewmodel.TransactionViewModel

@Composable
fun TransactionScreenHost(
    viewModel: TransactionViewModel,
    onTransactionClick: (Long) -> Unit,
    onMoneyRainTrigger: () -> Unit,
    ) {
    var showDialog by remember { mutableStateOf(false) }

    val transactions by viewModel.uiState.collectAsStateWithLifecycle()

    // Show Add Transaction dialog when requested
    if (showDialog) {
        AddTransactionDialog(
            onDismiss = { showDialog = false },
            viewModel = viewModel,
            onAdd     = { tx ->
                Log.d("MoneyRain", "Triggering rain overlay")
                viewModel.add(tx)
                if (tx.sum >= 0)  onMoneyRainTrigger()
                showDialog = false
            }
        )
    }

    // Display the main TransactionScreen
    TransactionScreen(
        transactions = transactions,
        onAddTransactionClick = { showDialog = true },
        onTransactionClick = onTransactionClick,
        viewModel = viewModel,
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 0.dp),

    )

}
