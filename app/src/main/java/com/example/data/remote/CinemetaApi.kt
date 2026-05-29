package com.example.data.remote

import com.squareup.moshi.Json
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

// Data models for Stremio Cinemeta API
data class CinemetaSearchResponse(
    @Json(name = "metas") val metas: List<CinemetaMeta>?
)

data class CinemetaMeta(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "poster") val poster: String?,
    @Json(name = "year") val year: String?,
    @Json(name = "type") val type: String?,
    @Json(name = "background") val background: String?,
    @Json(name = "description") val description: String?,
    @Json(name = "imdbRating") val imdbRating: String?,
    @Json(name = "runtime") val runtime: String?,
    @Json(name = "genres") val genres: List<String>?
)

data class CinemetaDetailResponse(
    @Json(name = "meta") val meta: CinemetaMeta?
)

interface CinemetaApi {
    @GET("catalog/movie/top/search={query}.json")
    suspend fun searchMovies(
        @Path("query") query: String
    ): CinemetaSearchResponse

    @GET("catalog/movie/top.json")
    suspend fun getPopularMovies(): CinemetaSearchResponse

    @GET("meta/movie/{imdb_id}.json")
    suspend fun getMovieDetails(
        @Path("imdb_id") imdbId: String
    ): CinemetaDetailResponse

    companion object {
        private const val BASE_URL = "https://v3-cinemeta.strem.io/"

        fun create(): CinemetaApi {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build()

            // Construct Moshi with standard adapter factory to serialize safely
            val moshi = com.squareup.moshi.Moshi.Builder()
                .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(CinemetaApi::class.java)
        }
    }
}
