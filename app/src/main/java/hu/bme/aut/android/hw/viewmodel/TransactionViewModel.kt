package hu.bme.aut.android.hw.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hu.bme.aut.android.hw.data.local.dao.GoalDao
import hu.bme.aut.android.hw.data.local.db.CategoryRepository
import hu.bme.aut.android.hw.data.local.db.TransactionRepository
import hu.bme.aut.android.hw.data.local.entity.GoalEntity
import hu.bme.aut.android.hw.domain.model.CategorySlice
import hu.bme.aut.android.hw.domain.model.MonthBars
import hu.bme.aut.android.hw.domain.model.Transaction

import hu.bme.aut.android.hw.utils.SortOption
import hu.bme.aut.android.hw.utils.TxFilter
import hu.bme.aut.android.hw.utils.TypeFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.toJavaLocalDate
import java.time.YearMonth
import javax.inject.Inject
import kotlin.math.absoluteValue

@HiltViewModel
class TransactionViewModel @Inject constructor(
    val repo: TransactionRepository,
    private val catRepo: CategoryRepository,
    private val goalDao: GoalDao,
) : ViewModel() {

    val goal: StateFlow<GoalEntity?> = goalDao.getGoal()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val goalAmount: StateFlow<Float> = goal.map { it?.amount ?: 0f }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0f)

    val goalCurrency: StateFlow<String> = goal.map { it?.currency ?: "EUR" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "EUR")

    fun setGoalAmount(amount: Float, currency: String) {
        viewModelScope.launch {
            goalDao.upsertGoal(GoalEntity(amount = amount, currency = currency))
        }
    }

    fun resetGoal() {
        viewModelScope.launch {
            goalDao.clearGoal()
        }
    }

    private val _sort = MutableStateFlow(SortOption.DATE_DESC)
    val sort: StateFlow<SortOption> = _sort.asStateFlow()

    fun setSort(opt: SortOption) { _sort.value = opt }

    val transactions = repo.transactions

    private val _filter = MutableStateFlow(TxFilter())
    val  filter: StateFlow<TxFilter> = _filter.asStateFlow()

    private val _monthFilter = MutableStateFlow<YearMonth?>(null)
    val monthFilter: StateFlow<YearMonth?> = _monthFilter.asStateFlow()
    fun setMonthFilter(month: YearMonth?) { _monthFilter.value = month }

    fun setType(type: TypeFilter){ _filter.update { it.copy(type = type           ) } }
    fun setCategory(cat: String?){ _filter.update { it.copy(category = cat)       } }

    fun getTransactionFlow(id: Long): Flow<Transaction?> =
        repo.getTransactionFlow(id)

    private val DEFAULT_CATEGORIES = listOf(
        "Common", "Food", "Work", "Transport", "Entertainment"
    )

    val uiState: StateFlow<List<Transaction>> =
        combine(transactions, _sort, _filter, _monthFilter) { list, sort, filter, month ->

            val byMonth = month?.let { m ->
                list.filter {
                    val ym = YearMonth.from(it.date.toJavaLocalDate())
                    ym == m
                }
            } ?: list

            /* ---------- 1) apply filter ---------- */
            val filtered = byMonth.filter { tx ->
                (filter.type == TypeFilter.ALL ||
                        (filter.type == TypeFilter.INCOME  && tx.sum >= 0) ||
                        (filter.type == TypeFilter.EXPENSE && tx.sum <  0)) &&
                        (filter.category == null || tx.category == filter.category)
            }

            /* ---------- 2) apply sort (same as before) ---------- */
            val sorted = when (sort) {
                SortOption.DATE_DESC     -> filtered.sortedByDescending { it.date }
                SortOption.DATE_ASC      -> filtered.sortedBy { it.date }
                SortOption.INCOME_FIRST  -> filtered.sortedByDescending { it.sum }
                SortOption.EXPENSE_FIRST -> filtered.sortedBy { it.sum }
                SortOption.CATEGORY      -> filtered.sortedBy { it.category }
                SortOption.AMOUNT_DESC   -> filtered.sortedByDescending { it.sum }
                SortOption.AMOUNT_ASC    -> filtered.sortedBy { it.sum }
            }
            sorted
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    /** total of every row, unaffected by filters */
    val totalSum: StateFlow<Double> =
        repo.transactions.map { list -> list.sumOf { it.sum } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), .0)

    /** sum of currently visible rows */
    val filteredSum: StateFlow<Double> =
        uiState.map { list -> list.sumOf { it.sum } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), .0)

    val categories: StateFlow<List<String>> =
        catRepo.categories                    // Flow<List<String>> from DAO
            .map { dbList ->
                (dbList + DEFAULT_CATEGORIES)
                    .distinct()
                    .sorted()
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = DEFAULT_CATEGORIES      // shows immediately
            )

    fun addCategory(name: String) = viewModelScope.launch {
        catRepo.add(name)
    }

    fun deleteCategory(name: String) = viewModelScope.launch {
        repo.reassignCategory(oldCategory = name, newCategory = "Common")
        catRepo.delete(name)
    }

    suspend fun get(id: Long): Transaction? = repo.get(id)
    fun add(tx: Transaction)  = viewModelScope.launch { repo.upsert(tx) }
    fun delete(tx: Transaction)= viewModelScope.launch { repo.delete(tx) }

    val monthlyBars: StateFlow<List<MonthBars>> =
        repo.transactions
            .map { list ->
                // bucket rows by calendar month
                list.groupBy { YearMonth.from(it.date.toJavaLocalDate()) }
                    .toSortedMap()                        // oldest → newest
                    .map { (ym, rows) ->
                        val inc = rows.filter { it.sum >= 0 }.sumOf { it.sum }
                        val exp = rows.filter { it.sum < 0 }.sumOf { it.sum.absoluteValue }
                        val currency = rows.firstOrNull()?.currency ?: "EUR"
                        MonthBars(ym, inc, exp, currency)
                    }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            )
}
