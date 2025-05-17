package hu.bme.aut.android.hw.data.remote.model

data class ExchangeResponse(
    val base_code: String,
    val conversion_rates: Map<String, Double>
)