package hu.bme.aut.android.hw.data.local.dao


import androidx.room.*
import hu.bme.aut.android.hw.data.local.entity.GoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {

    @Query("SELECT * FROM goals WHERE id = 1")
    fun getGoal(): Flow<GoalEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGoal(goal: GoalEntity)

    @Query("DELETE FROM goals")
    suspend fun clearGoal()
}