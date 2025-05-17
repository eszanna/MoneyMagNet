package hu.bme.aut.android.hw.utils

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import hu.bme.aut.android.hw.R

enum class TypeFilter(@StringRes val labelRes: Int) {
    ALL     (R.string.filter_all),
    INCOME  (R.string.filter_income),
    EXPENSE (R.string.filter_expense)
}

data class TxFilter(
    val type: TypeFilter = TypeFilter.ALL,
    val category: String? = null               // null = any category
)


