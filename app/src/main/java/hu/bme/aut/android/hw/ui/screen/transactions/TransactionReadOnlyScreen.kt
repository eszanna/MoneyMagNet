package hu.bme.aut.android.hw.ui.screen.transactions

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hu.bme.aut.android.hw.domain.model.Transaction
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import hu.bme.aut.android.hw.R
import hu.bme.aut.android.hw.utils.getCategoryLabels
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionReadOnlyScreen(
    transaction: Transaction,
    onEditClick: () -> Unit,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val categoryLabels = getCategoryLabels()

    //debug
    LaunchedEffect(transaction) {
        Log.d("GeoUI", "Detail sees tx #${transaction.id}: lat=${transaction.latitude} lon=${transaction.longitude}")
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.transaction_details),
                    style = MaterialTheme.typography.titleMedium,
                    )
                       },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit_title))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(WindowInsets.statusBars.asPaddingValues())
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")

            TransactionDetailCard(label = stringResource(R.string.description_label), emoji = "📝", value = transaction.description)

            TransactionDetailCard(label = stringResource(R.string.amount_label), emoji = "💰" ) {
                Text(
                    text = stringResource(
                        R.string.amount_formatted,
                        transaction.sum,
                        transaction.currency
                    ),
                    style = MaterialTheme.typography.titleLarge,
                    color = if (transaction.sum >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }

            TransactionDetailCard(label = stringResource(R.string.type_label),  emoji = "🔄") {
                Text(
                    text = if (transaction.sum >= 0) stringResource(R.string.income) else stringResource(R.string.expense),
                    style = MaterialTheme.typography.bodyLarge
                )

            }

            TransactionDetailCard(
                label = stringResource(R.string.date),
                emoji = "📅",
                value = transaction.date.toJavaLocalDate().format(dateFormatter)
            )

            //TransactionDetailCard(label = stringResource(R.string.category_label), emoji = "🏷️", value = transaction.category)

            val translatedCategory = categoryLabels[transaction.category] ?: transaction.category
            TransactionDetailCard(
                label = stringResource(R.string.category_label),
                emoji = "🏷️",
                value = translatedCategory
            )

            if (transaction.latitude != null && transaction.longitude != null) {
                TransactionDetailCard(label = stringResource(R.string.location_label), emoji = "📍") {
                    Text(
                        text = String.format(
                            Locale.getDefault(),
                            "%.5f, %.5f",
                            transaction.latitude,
                            transaction.longitude
                        ),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Button(onClick = {
                        // build a geo: URI and launch the implicit intent
                        val uri = Uri.parse(
                            "geo:${transaction.latitude},${transaction.longitude}" +
                                    "?q=${transaction.latitude},${transaction.longitude}"
                        )
                        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                            `package` = "com.google.android.apps.maps" // optional: restrict to Google Maps
                        }
                        // fallback: if Maps isn’t installed, remove the package
                        if (intent.resolveActivity(ctx.packageManager) == null) {
                            intent.`package` = null
                        }
                        ctx.startActivity(intent)
                    }) {
                        Text(stringResource(R.string.show_on_map))
                    }
                }
            } else {
                TransactionDetailCard(label = stringResource(R.string.location_label) , emoji = "📍", value = stringResource(R.string.no_location))
            }
        }
    }
}

@Composable
fun TransactionDetailCard(
    label: String,
    emoji: String? = null,
    value: String? = null,
    content: @Composable (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "${emoji.orEmpty()} $label",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1
            )

            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f))

            if (value != null) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                content?.invoke()
            }
        }
    }
}

