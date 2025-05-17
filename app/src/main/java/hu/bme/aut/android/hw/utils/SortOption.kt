package hu.bme.aut.android.hw.utils

import androidx.annotation.StringRes
import hu.bme.aut.android.hw.R

enum class SortOption(@StringRes val labelRes: Int) {
    DATE_DESC    (R.string.sort_newest),
    DATE_ASC     (R.string.sort_oldest),
    INCOME_FIRST (R.string.sort_income_first),
    EXPENSE_FIRST(R.string.sort_expense_first),
    CATEGORY     (R.string.sort_category),
    AMOUNT_DESC  (R.string.sort_amount_desc),
    AMOUNT_ASC   (R.string.sort_amount_asc)
}
