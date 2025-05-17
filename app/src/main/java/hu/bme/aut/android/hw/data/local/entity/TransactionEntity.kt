package hu.bme.aut.android.hw.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate

// db/TransactionEntity.kt
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: Long = 0L,
    val sum: Double,
    val description: String,
    val date: LocalDate,
    val category: String = "Common",
    val latitude: Double?,
    val longitude: Double?,
    val currency: String?
)
