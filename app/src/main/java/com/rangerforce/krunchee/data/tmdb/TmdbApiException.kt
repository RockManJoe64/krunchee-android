package com.rangerforce.krunchee.data.tmdb

class TmdbApiException(message: String, cause: Throwable) :
    Exception(message, cause) {
}