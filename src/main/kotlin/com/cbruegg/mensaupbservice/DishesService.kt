package com.cbruegg.mensaupbservice

import com.cbruegg.mensaupbservice.api.Badge
import com.cbruegg.mensaupbservice.api.Dish
import com.cbruegg.mensaupbservice.api.DishesServiceResult
import com.squareup.moshi.Json
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.withTimeoutOrNull
import kotlinx.serialization.protobuf.ProtoBuf
import okhttp3.Request
import org.jetbrains.ktor.http.ContentType
import org.jetbrains.ktor.http.HttpStatusCode
import java.io.IOException
import java.util.*

suspend fun getDishes(apiId: String, restaurantId: String, date: Date): HttpResponseData {
  val result = downloadDishesAsync(restaurantId, date, apiId)
      .await()
      .fold({ DishesServiceResult(null) }) {
        DishesServiceResult(DishesServiceResult.Success(it))
      }

  val status = if (result.success != null) HttpStatusCode.OK else HttpStatusCode.BadGateway
  return HttpResponseData(ContentType.parse("application/octet-stream"), ProtoBuf.dumps(result), status)
}


private fun downloadDishesAsync(restaurantId: String, date: Date, apiId: String): Deferred<IOEither<List<Dish>>> = networkAsync {
  withTimeoutOrNull(TIMEOUT_MS) {
    val request = Request.Builder().url(dishesUrl(restaurantId, date, apiId)).build()
    val response = httpClient.newCall(request).await()
    val jsonDishes = MoshiProvider.provideListJsonAdapter<JsonDish>().fromJson(response.body()!!.source())!!
    jsonDishes.map { it.toDish() }
  } ?: throw IOException("Network timeout!")
}

/**
 * Model representing a dish object returned by the API.
 */
private data class JsonDish(
    @Json(name = "date") val date: Date,
    @Json(name = "name_de") val nameDE: String,
    @Json(name = "name_en") val nameEN: String,
    @Json(name = "description_de") val descriptionDE: String?,
    @Json(name = "description_en") val descriptionEN: String?,
    @Json(name = "category") val category: String,
    @Json(name = "category_de") val categoryDE: String,
    @Json(name = "category_en") val categoryEN: String,
    @Json(name = "subcategory_de") val subcategoryDE: String,
    @Json(name = "subcategory_en") val subcategoryEN: String,
    @Json(name = "priceStudents") val studentPrice: Double,
    @Json(name = "priceWorkers") val workerPrice: Double,
    @Json(name = "priceGuests") val guestPrice: Double,
    @Json(name = "allergens") val allergens: List<String>,
    @Json(name = "order_info") val orderInfo: Int,
    @Json(name = "badges") val badgesStrings: List<String>?,
    @Json(name = "restaurant") val restaurantId: String,
    @Json(name = "pricetype") val priceType: PriceType,
    @Json(name = "image") val imageUrl: String?,
    @Json(name = "thumbnail") val thumbnailImageUrl: String?
) {
  @delegate:Transient
  val badges by lazy { badgesStrings?.mapNotNull { Badge.findById(it) } ?: emptyList() }

  fun toDish() = Dish(
      date, nameDE, nameEN, descriptionDE, descriptionEN, category, categoryDE, categoryEN,
      subcategoryDE, subcategoryEN, studentPrice, workerPrice, guestPrice, allergens, orderInfo,
      badges, restaurantId, priceType.toApiPriceType(), imageUrl, thumbnailImageUrl
  )
}

private enum class PriceType {
  @Json(name = "weighted")
  WEIGHTED,
  @Json(name = "fixed")
  FIXED;

  fun toApiPriceType() = when (this) {
    PriceType.WEIGHTED -> com.cbruegg.mensaupbservice.api.PriceType.WEIGHTED
    PriceType.FIXED -> com.cbruegg.mensaupbservice.api.PriceType.FIXED
  }
}