package com.cbruegg.mensaupbservice.api

import kotlinx.serialization.Optional
import kotlinx.serialization.SerialId
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.text.SimpleDateFormat

@Serializable
data class RestaurantsServiceResult(
    @SerialId(1) @Optional val restaurants: List<Restaurant> = emptyList()
)

@Serializable
data class Restaurant(
    @SerialId(1) val id: String,
    @SerialId(2) val name: String,
    @SerialId(3) val location: String,
    @SerialId(4) val isActive: Boolean
)

@Serializable
data class DishesServiceResult(
    @SerialId(1) @Optional val dishes: List<Dish> = emptyList()
)

internal val iso8601 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")

@Serializable
data class Dish(
    @SerialId(1) private val dateStr: String,
    @SerialId(2) val nameDE: String,
    @SerialId(3) val nameEN: String,
    @SerialId(4) val descriptionDE: String?,
    @SerialId(5) val descriptionEN: String?,
    @SerialId(6) val category: String,
    @SerialId(7) val categoryDE: String,
    @SerialId(8) val categoryEN: String,
    @SerialId(9) val subcategoryDE: String,
    @SerialId(10) val subcategoryEN: String,
    @SerialId(11) val studentPrice: Double,
    @SerialId(12) val workerPrice: Double,
    @SerialId(13) val guestPrice: Double,
    @SerialId(14) @Optional val allergens: List<String> = emptyList(),
    @SerialId(15) val orderInfo: Int,
    @SerialId(16) @Optional val badges: List<Badge> = emptyList(),
    @SerialId(17) val restaurantId: String,
    @SerialId(18) val priceType: PriceType,
    @SerialId(19) val imageUrl: String?,
    @SerialId(20) val thumbnailImageUrl: String?
) {
  @Transient
  val date by lazy { iso8601.parse(dateStr) }
}

enum class PriceType {
  WEIGHTED, FIXED
}

enum class Badge(private val id: String) {
  VEGAN("vegan"), VEGETARIAN("vegetarian"),
  NONFAT("nonfat"), LACTOSE_FREE("lactose-free");

  companion object {
    /**
     * Each Badge has an id that is used by the API. This method retrieves a Badge by its id.
     * Return value will be null if there's no matching element.
     */
    fun findById(id: String): Badge? = values().firstOrNull { it.id == id }
  }
}