package hu.bme.aut.android.hw.utils

import java.text.DecimalFormat

fun formatAmount(amount: Number): String {
    val formatter = DecimalFormat("#,##0.00").apply {
        decimalFormatSymbols = decimalFormatSymbols.apply {
            groupingSeparator = ' '
        }
    }
    return formatter.format(amount)
}
