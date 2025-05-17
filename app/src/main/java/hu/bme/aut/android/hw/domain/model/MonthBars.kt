package hu.bme.aut.android.hw.domain.model

import java.time.YearMonth

data class MonthBars(
    val month: YearMonth,
    val income: Double,
    val expense: Double ,        // positive number
    val currency: String
)
