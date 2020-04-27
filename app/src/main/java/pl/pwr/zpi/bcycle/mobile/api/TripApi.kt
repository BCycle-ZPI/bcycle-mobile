package pl.pwr.zpi.bcycle.mobile.api

import io.reactivex.Completable
import io.reactivex.Single
import pl.pwr.zpi.bcycle.mobile.models.Trip
import retrofit2.http.*

interface TripApi {
    @GET("trips")
    fun getAll(): Single<ApiResultContainer<List<Trip>>>

    @GET("trips/{id}")
    fun get(@Path("id") id: Int): Single<ApiResultContainer<Trip>>

    @POST("trips")
    fun post(@Body trip: Trip): Single<ApiResultContainer<Int>>

    @PUT("trips/{id}/photo")
    fun putPhoto(@Path("id") id: Int, @Body image: ByteArray): Single<ApiResultContainer<String>>

    @DELETE("trips/{id}")
    fun delete(@Path("id") id: Int): Completable
}