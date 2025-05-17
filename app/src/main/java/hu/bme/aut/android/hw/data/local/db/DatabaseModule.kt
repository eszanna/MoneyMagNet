package hu.bme.aut.android.hw.data.local.db

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import hu.bme.aut.android.hw.data.local.dao.CategoryDao
import hu.bme.aut.android.hw.data.local.dao.GoalDao
import hu.bme.aut.android.hw.data.local.dao.TransactionDao
import javax.inject.Singleton

// di/DatabaseModule.kt
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDb(
        @ApplicationContext ctx: Context
    ): AppDatabase = Room.databaseBuilder(ctx, AppDatabase::class.java, "transactions.db")
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    fun provideDao(db: AppDatabase): TransactionDao = db.transactionDao()

    @Provides
    fun provideCategoryDao(db: AppDatabase): CategoryDao =
        db.categoryDao()

    @Provides
    fun provideGoalDao(database: AppDatabase): GoalDao =
        database.goalDao()
}
