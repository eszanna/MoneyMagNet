package hu.bme.aut.android.hw.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import hu.bme.aut.android.hw.R

@Composable
fun HomeScreen(
    onSummary: () -> Unit,
    onTransactions: () -> Unit,
    onCharts: () -> Unit,
    onGoals: () -> Unit,
    onExchangeRates: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MenuCard(stringResource(R.string.summary_title),      Icons.Default.Info,      onSummary)
        MenuCard(stringResource(R.string.transactions_title), Icons.Default.List, onTransactions)
        MenuCard(stringResource(R.string.charts_title),     Icons.Default.PieChart, onCharts)
        MenuCard(stringResource(R.string.goals_title), Icons.Default.Star, onGoals)
        MenuCard(stringResource(R.string.exchange_rates), Icons.Default.Info, onExchangeRates)
    }
}

@Composable
private fun MenuCard(title: String, icon: ImageVector?, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            icon?.let { Icon(it, contentDescription = null) }
            Text(title, style = MaterialTheme.typography.titleMedium)
        }
    }
}