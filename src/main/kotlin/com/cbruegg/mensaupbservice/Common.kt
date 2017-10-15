package com.cbruegg.mensaupbservice

import kotlinx.coroutines.experimental.CoroutineDispatcher
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.asCoroutineDispatcher
import kotlinx.coroutines.experimental.async
import okhttp3.OkHttpClient
import org.funktionale.either.Either
import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.http.ContentType
import org.jetbrains.ktor.http.HttpStatusCode
import org.jetbrains.ktor.response.respondText
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

// URLs

private fun baseUrl(apiId: String) = "http://www.studierendenwerk-pb.de/fileadmin/shareddata/access2.php?id=$apiId"
fun restaurantsUrl(apiId: String) = "${baseUrl(apiId)}&getrestaurants=1"
fun dishesUrl(restaurantId: String, date: Date, apiId: String): String {
  val dateFormat = SimpleDateFormat("yyyy-MM-dd")
  return "${baseUrl(apiId)}&date=${dateFormat.format(date)}&restaurant=$restaurantId"
}

// Network utilities

typealias IOEither<T> = Either<IOException, T>

const val TIMEOUT_MS = 10_000L
val httpClient = OkHttpClient()

private val IOPool = Executors.newCachedThreadPool().asCoroutineDispatcher()
/**
 * Perform the action with the [dispatcher] and wrap it in [eitherTryIo].
 */
fun <T : Any> networkAsync(dispatcher: CoroutineDispatcher = IOPool, f: suspend () -> T): Deferred<IOEither<T>> =
    async(dispatcher) {
      eitherTryIo {
        f()
      }
    }

/**
 * Return either the desired result on the [Either.Right] side
 * or a caught [IOException] on the [Either.Left] side.
 */
private inline fun <T : Any> eitherTryIo(f: () -> T): Either<IOException, T> =
    try {
      Either.Right(f())
    } catch (e: IOException) {
      Either.Left(e)
    }

data class HttpResponseData(val contentType: ContentType, val body: String, val statusCode: HttpStatusCode)
suspend fun ApplicationCall.respond(data: HttpResponseData) = respondText(data.body, data.contentType, data.statusCode)