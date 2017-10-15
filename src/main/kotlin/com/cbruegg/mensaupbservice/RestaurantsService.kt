package com.cbruegg.mensaupbservice

import com.cbruegg.mensaupbservice.api.Restaurant
import com.cbruegg.mensaupbservice.api.RestaurantsServiceResult
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.withTimeoutOrNull
import kotlinx.serialization.protobuf.ProtoBuf
import okhttp3.Request
import okio.BufferedSource
import org.jetbrains.ktor.http.ContentType
import org.jetbrains.ktor.http.HttpStatusCode
import java.io.IOException

suspend fun getRestaurants(apiId: String): HttpResponseData {
  val result = downloadRestaurantsAsync(apiId)
      .await()
      .fold({ null }) {
        RestaurantsServiceResult(it)
      }

  val status = if (result != null) HttpStatusCode.OK else HttpStatusCode.BadGateway
  return HttpResponseData(ContentType.parse("application/octet-stream"), if (result != null) ProtoBuf.dumps(result) else "", status)
}

/**
 * Parse restaurants from the API response.
 */
private fun parseRestaurantsFromApi(restaurantListSource: BufferedSource): List<Restaurant> {
  val moshi = MoshiProvider.provideJsonAdapter<Map<String, Map<String, *>>>()
  val deserialized = moshi.fromJson(restaurantListSource)
  return deserialized!!.map {
    Restaurant(it.key, it.value["name"] as String, it.value["location"] as String, it.value["active"] as Boolean)
  }
}

private fun downloadRestaurantsAsync(apiId: String): Deferred<IOEither<List<Restaurant>>> = networkAsync {
  withTimeoutOrNull(TIMEOUT_MS) {
    val request = Request.Builder().url(restaurantsUrl(apiId)).build()
    val response = httpClient.newCall(request).await()
    parseRestaurantsFromApi(response.body()!!.source())
  } ?: throw IOException("Network timeout!")
}



