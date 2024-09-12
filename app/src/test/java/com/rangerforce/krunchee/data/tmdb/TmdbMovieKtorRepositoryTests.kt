package com.rangerforce.krunchee.data.tmdb

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.runBlocking

class TmdbMovieKtorRepositoryTest : StringSpec({

    fun createMockClient(handler: (HttpRequestData) -> HttpResponseData): HttpClient {
        return HttpClient(MockEngine) {
            engine {
                addHandler(handler)
            }
        }
    }

    "test getPopularMovies success" {
        val mockResponse = """
            {
                "page": 1,
                "results": [],
                "total_pages": 1,
                "total_results": 0
            }
        """.trimIndent()

        val client = createMockClient { request ->
            respond(
                content = ByteReadChannel(mockResponse),
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type" to listOf("application/json"))
            )
        }

        val repository = TmdbMovieKtorRepository(client)
        val response = runBlocking { repository.getPopularMovies(1, "en-US", "US") }

        response.page shouldBe 1
        response.results.size shouldBe 0
        response.totalPages shouldBe 1
        response.totalResults shouldBe 0
    }

    "test getPopularMovies client request exception" {
        val client = createMockClient { request ->
            respond(
                content = ByteReadChannel(""),
                status = HttpStatusCode.BadRequest,
                headers = headersOf("Content-Type" to listOf("application/json"))
            )
        }

        val repository = TmdbMovieKtorRepository(client)

        shouldThrow<TmdbApiException> {
            runBlocking { repository.getPopularMovies(1, "en-US", "US") }
        }
    }
})