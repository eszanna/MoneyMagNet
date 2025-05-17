package hu.bme.aut.android.hw.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import hu.bme.aut.android.hw.data.local.converter.LocalDateConverter
import hu.bme.aut.android.hw.data.local.dao.CategoryDao
import hu.bme.aut.android.hw.data.local.dao.GoalDao
import hu.bme.aut.android.hw.data.local.dao.TransactionDao
import hu.bme.aut.android.hw.data.local.entity.CategoryEntity
import hu.bme.aut.android.hw.data.local.entity.GoalEntity
import hu.bme.aut.android.hw.data.local.entity.TransactionEntity

@Database(
    entities = [TransactionEntity::class, CategoryEntity::class, GoalEntity::class],
    version = 6,
    exportSchema = false
)
@TypeConverters(LocalDateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun goalDao(): GoalDao
}
