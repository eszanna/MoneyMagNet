package hu.bme.aut.android.hw.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hu.bme.aut.android.hw.R
import hu.bme.aut.android.hw.domain.model.Transaction
import hu.bme.aut.android.hw.utils.formatAmount
import hu.bme.aut.android.hw.utils.formatLocalDate

@Composable
fun TransactionItem(
    transaction: Transaction,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val color = if (transaction.sum >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
    val formattedDate = formatLocalDate(transaction.date)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)          // leave breathing room
            ) {
                Text(
                    text       = transaction.description,
                    fontWeight = FontWeight.Bold,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                Text(
                    text     = formattedDate,
                    fontSize = 12.sp,
                    color    = Color.Gray
                )
            }

            Text(
                text = "${formatAmount(transaction.sum)} ${transaction.currency}",
                color      = color,
                fontWeight = FontWeight.SemiBold,
                modifier   = Modifier.padding(end = if (onDelete != null) 4.dp else 0.dp)
            )

            onDelete?.let {
                IconButton(onClick = it) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                }
            }
        }
    }
}
