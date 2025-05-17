package hu.bme.aut.android.hw.domain.model

import hu.bme.aut.android.hw.data.local.entity.TransactionEntity
import kotlinx.datetime.LocalDate

data class Transaction (
    val id: Long = 0L,
    val sum: Double,
    val description: String,
    val date: LocalDate,
    val category: String = "Common",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val currency: String
)

