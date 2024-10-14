package com.rangerforce.krunchee.data.tmdb

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.HttpStatusCode
import io.ktor.http.headers
import io.ktor.http.path
import io.ktor.serialization.ContentConvertException

class TmdbMovieKtorRepository(private val client: HttpClient, private val apiConfig: TmdbApiConfig) : TmdbMovieRepository {
    override suspend fun getPopularMovies(
        page: Int,
        language: String,
        region: String
    ): PagedResponse<MovieDetail> {
        return try {
            client.get {
                headers {
                    append("Accept", "application/json")
                    append("Authorization", "Bearer ${apiConfig.apiToken}")
                    append("Content-Type", "application/json")
                }
                url {
                    path(HttpRoutes.POPULAR_MOVIES)
                    parameter("page", page)
                    parameter("language", language)
                    parameter("region", region)
                }
            }.body<PagedResponse<MovieDetail>>()
        } catch (e: ContentConvertException) {
            // TODO avoid this exception
            throw TmdbApiException("Failed to get popular movies due to empty body", e)
        } catch (e: RedirectResponseException) {
            throw TmdbApiException("Failed to get popular movies", e)
        } catch (e: ClientRequestException) {
            when (e.response.status) {
                HttpStatusCode.BadRequest -> throw TmdbApiException("Failed to get popular movies due to bad request", e)
                HttpStatusCode.Unauthorized -> throw TmdbApiException("Failed to get popular movies due to unauthorized", e)
                HttpStatusCode.NotFound -> throw TmdbApiException("Failed to get popular movies due to not found", e)
                else -> throw TmdbApiException("Failed to get popular movies", e)
            }
        } catch (e: ServerResponseException) {
            throw TmdbApiException("Failed to get popular movies", e)
        }
    }
}
