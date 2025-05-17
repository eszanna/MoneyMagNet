package hu.bme.aut.android.hw.data.local.db

import hu.bme.aut.android.hw.data.local.dao.CategoryDao
import hu.bme.aut.android.hw.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CategoryRepository @Inject constructor(
    private val dao: CategoryDao
) {
    val categories: Flow<List<String>> =
        dao.getAll().map { list -> list.map { it.name } }

    suspend fun add(name: String) =
        dao.insert(CategoryEntity(name = name.trim()))

    suspend fun delete(name: String) {
        dao.delete(name)
    }
}
