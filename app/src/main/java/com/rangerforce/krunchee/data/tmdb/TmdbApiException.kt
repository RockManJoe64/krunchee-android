package com.rangerforce.krunchee.data.tmdb

class TmdbApiException : Exception {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}