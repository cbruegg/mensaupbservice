package com.cbruegg.mensaupbservice

import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.host.embeddedServer
import org.jetbrains.ktor.http.HttpStatusCode
import org.jetbrains.ktor.netty.Netty
import org.jetbrains.ktor.response.respondText
import org.jetbrains.ktor.routing.get
import org.jetbrains.ktor.routing.routing
import java.text.SimpleDateFormat

fun main(args: Array<String>) {
  embeddedServer(Netty, 50678) {
    routing {
      get("/restaurants") {
        val apiId = call.request.queryParameters["apiId"] ?: return@get call.missingParam("apiId")

        call.respond(getRestaurants(apiId))
      }

      get("/dishes") {
        val apiId = call.request.queryParameters["apiId"] ?: return@get call.missingParam("apiId")
        val restaurantId = call.request.queryParameters["restaurantId"] ?: return@get call.missingParam("restaurantId")
        val dateStr = call.request.queryParameters["date"] ?: return@get call.missingParam("date")
        val date = SimpleDateFormat("yyyy-MM-dd").parse(dateStr)

        call.respond(getDishes(apiId, restaurantId, date))
      }
    }
  }.start(wait = true)
}

private suspend fun ApplicationCall.missingParam(name: String) =
    respondText("Missing $name!", status = HttpStatusCode.BadRequest)