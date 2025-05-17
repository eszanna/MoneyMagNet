package hu.bme.aut.android.hw.data.local.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import hu.bme.aut.android.hw.ExchangeService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val ds: DataStore<Preferences>,
    private val api: ExchangeService


) {
    private val RATES_KEY = stringPreferencesKey("cached_rates_json")
    private val LAST_FETCH_KEY = longPreferencesKey("cached_rates_last_fetch")

    private val CURRENCY_KEY = stringPreferencesKey("default_currency")

    val defaultCurrency: Flow<String> = ds.data.map {
        it[CURRENCY_KEY] ?: "EUR"
    }

    suspend fun setDefaultCurrency(code: String) {
        ds.edit { it[CURRENCY_KEY] = code }
    }

    suspend fun getRates(base: String = "USD"): Map<String, Double> {
        val now = System.currentTimeMillis()

        val prefs = ds.data.first()

        val lastFetch = prefs[LAST_FETCH_KEY] ?: 0L
        val json = prefs[RATES_KEY]

        // Only fetch if more than 24h passed or no cache
        return if (json != null && now - lastFetch < 24 * 60 * 60 * 1000) {
            Json.decodeFromString<Map<String, Double>>(json)
        } else {
            val response = api.getRates("ba18723282ca73d2177b19f5", base)
            ds.edit {
                it[RATES_KEY] = Json.encodeToString(response.conversion_rates)
                it[LAST_FETCH_KEY] = now
            }
            response.conversion_rates
        }
    }

    val lastFetchTime: Flow<Long?> = ds.data.map { it[LAST_FETCH_KEY] }
}
