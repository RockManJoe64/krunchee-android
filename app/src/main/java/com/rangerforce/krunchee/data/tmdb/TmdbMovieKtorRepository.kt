package com.rangerforce.krunchee.data.tmdb

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.path

class TmdbMovieKtorRepository(private val client: HttpClient) : TmdbMovieRepository {
    override suspend fun getPopularMovies(
        page: Int,
        language: String,
        region: String
    ): PagedResponse<MovieDetail> {
        return try {
            client.get {
                url {
                    path(HttpRoutes.POPULAR_MOVIES)
                    parameter("page", page)
                    parameter("language", language)
                    parameter("region", region)
                }
            }.body<PagedResponse<MovieDetail>>()
        } catch (e: RedirectResponseException) {
            throw TmdbApiException("Failed to get popular movies", e)
        } catch (e: ClientRequestException) {
            throw TmdbApiException("Failed to get popular movies", e)
        } catch (e: ServerResponseException) {
            throw TmdbApiException("Failed to get popular movies", e)
        }
    }
}
