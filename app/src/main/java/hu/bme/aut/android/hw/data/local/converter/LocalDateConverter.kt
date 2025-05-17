package hu.bme.aut.android.hw.data.local.converter

import androidx.room.TypeConverter
import kotlinx.datetime.LocalDate

// db/LocalDateConverter.kt
class LocalDateConverter {
    @TypeConverter
    fun fromLocalDate(date: LocalDate): String = date.toString()          // 2025-04-21
    @TypeConverter
    fun toLocalDate(value: String): LocalDate = LocalDate.parse(value)
}
