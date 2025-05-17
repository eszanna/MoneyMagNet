package hu.bme.aut.android.hw.data.local.db

import android.util.Log
import androidx.room.Query
import hu.bme.aut.android.hw.data.local.dao.TransactionDao
import hu.bme.aut.android.hw.data.local.entity.TransactionEntity
import hu.bme.aut.android.hw.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.toJavaLocalDate
import javax.inject.Inject

class TransactionRepository @Inject constructor(
    private val dao: TransactionDao
) {
    fun getTransactionFlow(id: Long): Flow<Transaction?> =
        dao.getByIdFlow(id).map { e ->
            val tx = e?.toDomain()
            Log.d("GeoRepo", "getByIdFlow($id) → $tx")
            tx
        }



    val transactions: Flow<List<Transaction>> =
        dao.getAll()
            .map { entities ->
                entities.map { e ->
                    val tx = e.toDomain()
                    Log.d("GeoRepo", "Repo mapped tx #${tx.id}: lat=${tx.latitude} lon=${tx.longitude}")
                    tx
                }
            }



    suspend fun upsert(tx: Transaction) = dao.upsert(tx.toEntity())
    suspend fun delete(tx: Transaction) = dao.delete(tx.toEntity())

    suspend fun get(id: Long): Transaction? {
        val e = dao.get(id)
        return e?.toDomain()

    }

    /** Move every transaction in [oldCategory] to [newCategory] */
    suspend fun reassignCategory(oldCategory: String, newCategory: String) {
        dao.reassignCategory(oldCategory, newCategory)
    }

}

/* --- mapping helpers --- */
private fun TransactionEntity.toDomain() = Transaction(
    id, sum, description, date, category,
    latitude,
    longitude, currency ?: "EUR"
)
private fun Transaction.toEntity()       = TransactionEntity(id, sum, description, date, category, latitude, longitude, currency )
