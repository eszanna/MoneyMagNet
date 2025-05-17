package hu.bme.aut.android.hw.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import hu.bme.aut.android.hw.R

@Composable
fun getCategoryLabels(): Map<String, String> = mapOf(
    "Common"        to stringResource(R.string.category_common),
    "Food"          to stringResource(R.string.category_food),
    "Work"          to stringResource(R.string.category_work),
    "Transport"     to stringResource(R.string.category_transport),
    "Entertainment" to stringResource(R.string.category_entertainment),
)