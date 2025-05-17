package hu.bme.aut.android.hw.utils

import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter

fun formatLocalDate(date: LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
    return date.toJavaLocalDate().format(formatter)
}