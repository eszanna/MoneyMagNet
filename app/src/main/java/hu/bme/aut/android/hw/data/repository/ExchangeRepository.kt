package hu.bme.aut.android.hw.data.repository


import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import hu.bme.aut.android.hw.ExchangeService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExchangeRepository @Inject constructor(
    private val service: ExchangeService,
    private val dataStore: DataStore<Preferences>
) {
    private val apiKey = "ba18723282ca73d2177b19f5"

    private val LAST_FETCH_KEY = longPreferencesKey("cached_rates_last_fetch")
    private val RATES_KEY_PREFIX = "rate_"

    private val cacheDurationMillis = TimeUnit.HOURS.toMillis(24)

    val lastFetchTime = dataStore.data.map { prefs ->
        prefs[LAST_FETCH_KEY] ?: 0L
    }

    suspend fun getRates(base: String = "USD"): Map<String, Double> {
        val prefs = dataStore.data.first()
        val lastFetch = prefs[LAST_FETCH_KEY] ?: 0L
        val now = System.currentTimeMillis()

        val shouldFetch = now - lastFetch > cacheDurationMillis



        return if (shouldFetch) {
            try {
                val response = service.getRates(apiKey, base)
                cacheRates(response.conversion_rates, now)
                response.conversion_rates
            } catch (e: Exception) {
                Log.e("ExchangeRepository", "Fetch failed, using cache", e)
                var showFallbackMessage = true
                readCachedRates()
            }
        } else {
            readCachedRates()
        }
    }

    private suspend fun cacheRates(rates: Map<String, Double>, timestamp: Long) {
        dataStore.edit { prefs ->
            prefs[LAST_FETCH_KEY] = timestamp
            rates.forEach { (code, rate) ->
                prefs[doublePreferencesKey("$RATES_KEY_PREFIX$code")] = rate
            }
        }
    }

    private suspend fun readCachedRates(): Map<String, Double> {
        val prefs = dataStore.data.first()
        return prefs.asMap()
            .filterKeys { it.name.startsWith(RATES_KEY_PREFIX) && it is Preferences.Key<*> }
            .mapNotNull { (k, v) ->
                if (k is Preferences.Key<*> && v is Double) {
                    k.name.removePrefix(RATES_KEY_PREFIX) to v
                } else null
            }
            .toMap()
    }

}
