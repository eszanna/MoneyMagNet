package hu.bme.aut.android.hw.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey val id: Int = 1,  // always a single goal (id = 1)
    val amount: Float,
    val currency: String
)