package hu.bme.aut.android.hw.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import hu.bme.aut.android.hw.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAll(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: TransactionEntity)            // single API = add/edit

    @Delete
    suspend fun delete(entity: TransactionEntity)



    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun get(id: Long): TransactionEntity?

    @Query("UPDATE transactions SET category = :newCat WHERE category = :oldCat")
    suspend fun reassignCategory(oldCat: String, newCat: String)

    @Query("SELECT * FROM transactions WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<TransactionEntity?>
}
