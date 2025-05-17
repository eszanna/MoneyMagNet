package hu.bme.aut.android.hw

import hu.bme.aut.android.hw.data.remote.model.ExchangeResponse
import retrofit2.http.GET
import retrofit2.http.Path


interface ExchangeService {
    @GET("v6/{apiKey}/latest/{base}")
    suspend fun getRates(
        @Path("apiKey") apiKey: String,
        @Path("base") base: String
    ): ExchangeResponse
}