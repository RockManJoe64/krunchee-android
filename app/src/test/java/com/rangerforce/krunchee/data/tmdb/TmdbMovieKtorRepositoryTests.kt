package com.rangerforce.krunchee.data.tmdb

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

class TmdbMovieKtorRepositoryTests : StringSpec({

    fun createMockClient(handler: MockRequestHandleScope.(HttpRequestData) -> HttpResponseData): HttpClient {
        return HttpClient(MockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            engine {
                addHandler(handler)
            }
        }
    }

    "given a response of 200 when getPopularMovies should be successful" {
        val mockResponse = """
            {
                "page": 1,
                "results": [],
                "total_pages": 1,
                "total_results": 0
            }
        """.trimIndent()

        val client = createMockClient { _ ->
            respond(
                content = ByteReadChannel(mockResponse),
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type" to listOf("application/json"))
            )
        }

        val repository = TmdbMovieKtorRepository(client, TmdbApiConfig)
        val response = runBlocking { repository.getPopularMovies(1, "en-US", "US") }

        response.page shouldBe 1
        response.results.size shouldBe 0
        response.totalPages shouldBe 1
        response.totalResults shouldBe 0
    }

    "given a valid response when getPopularMovies should parse response correctly" {
        val mockResponse = """
            {
                "page": 1,
                "results": [
                    {
                        "adult": false,
                        "backdrop_path": "/yDHYTfA3R0jFYba16jBB1ef8oIt.jpg",
                        "genre_ids": [28, 35, 878],
                        "id": 533535,
                        "original_language": "en",
                        "original_title": "Deadpool & Wolverine",
                        "overview": "A listless Wade Wilson toils away in civilian life with his days as the morally flexible mercenary, Deadpool, behind him. But when his homeworld faces an existential threat, Wade must reluctantly suit-up again with an even more reluctant Wolverine.",
                        "popularity": 4256.172,
                        "poster_path": "/8cdWjvZQUExUUTzyp4t6EDMubfO.jpg",
                        "release_date": "2024-07-24",
                        "title": "Deadpool & Wolverine",
                        "video": false,
                        "vote_average": 7.713,
                        "vote_count": 4431
                    }
                ],
                "total_pages": 1,
                "total_results": 1
            }
        """.trimIndent()

        val client = createMockClient { _ ->
            respond(
                content = ByteReadChannel(mockResponse),
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type" to listOf("application/json"))
            )
        }

        val repository = TmdbMovieKtorRepository(client, TmdbApiConfig)
        val response = runBlocking { repository.getPopularMovies(1, "en-US", "US") }

        response.results.size shouldBe 1
        response.results[0].id shouldBe 533535u
        response.results[0].title shouldBe "Deadpool & Wolverine"
        response.results[0].posterPath shouldBe "/8cdWjvZQUExUUTzyp4t6EDMubfO.jpg"
        response.results[0].releaseDate shouldBe "2024-07-24"
    }

    "given a 4xx error when getPopularMovies should then throw client request exception" {
        forAll(
            row(HttpStatusCode.BadRequest, "Failed to get popular movies due to bad request"),
            row(HttpStatusCode.Unauthorized, "Failed to get popular movies due to unauthorized"),
            row(HttpStatusCode.NotFound, "Failed to get popular movies due to not found"),
            row(HttpStatusCode.MethodNotAllowed, "Failed to get popular movies due to client request"),
        ) { status, expectedMessage ->
            val client = createMockClient { _ ->
                respond(
                    content = ByteReadChannel(""),
                    status = status,
                    headers = headersOf("Content-Type" to listOf("application/json"))
                )
            }

            val repository = TmdbMovieKtorRepository(client, TmdbApiConfig)

            val exception = shouldThrow<TmdbApiException> {
                runBlocking { repository.getPopularMovies(1, "en-US", "US") }
            }

            exception.message shouldBe expectedMessage
        }
    }

    "given a 500 error when getPopularMovies should then throw client request exception" {
        val client = createMockClient { _ ->
            respond(
                content = ByteReadChannel(""),
                status = HttpStatusCode.InternalServerError,
                headers = headersOf("Content-Type" to listOf("application/json"))
            )
        }

        val repository = TmdbMovieKtorRepository(client, TmdbApiConfig)

        val exception = shouldThrow<TmdbApiException> {
            runBlocking { repository.getPopularMovies(1, "en-US", "US") }
        }

        exception.message shouldBe "Failed to get popular movies due to server error"
    }
})