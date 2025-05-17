package hu.bme.aut.android.hw.utils

/** Accepts `123`, `123.45`, but NOT empty string or just `.`.
 *  Up to two decimals; tweak the regex if you need more. */
private val amountRegex = Regex("""\d+(\.\d{0,2})?""")

/** Returns the double or null when the input is invalid. */
fun String.toValidAmountOrNull(): Double? =
    if (isNotBlank() && matches(amountRegex)) toDouble() else null