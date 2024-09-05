package com.rangerforce.krunchee.data.tmdb

interface TmdbMovieRepository {
    suspend fun getPopularMovies(
        page: Int = 1,
        language: String = "en-US",
        region: String = "US",
    ): PagedResponse<MovieDetail>
}
