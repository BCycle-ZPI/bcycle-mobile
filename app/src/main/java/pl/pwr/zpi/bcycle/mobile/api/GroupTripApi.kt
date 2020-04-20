package pl.pwr.zpi.bcycle.mobile.api

import io.reactivex.Completable
import io.reactivex.Single
import pl.pwr.zpi.bcycle.mobile.models.GroupTrip
import retrofit2.http.*

interface GroupTripApi {
    @POST("group-trips")
    fun create(@Body groupTrip: GroupTrip): Single<ApiResultContainer<Int>>

    @GET("group-trips/{id}")
    fun get(@Path("id") id: Int): Single<ApiResultContainer<GroupTrip>>

    @PUT("group-trips/{id}")
    fun update(@Path("id") id: Int, @Body groupTrip: GroupTrip): Completable

    @DELETE("group-trips/{id}")
    fun delete(@Path("id") id: Int): Completable

    @POST("group-trips/join/{code}")
    fun join(@Path("code") code: String): Completable

    @POST("group-trips/{tripId}/requests/{userId}")
    fun acceptParticipant(@Path("tripId") tripId: Int, @Path("userId") userId: Int): Completable

    @DELETE("group-trips/{tripId}/requests/{userId}")
    fun rejectParticipant(@Path("tripId") tripId: Int, @Path("userId") userId: Int): Completable

    @DELETE("group-trips/{tripId}/participants/{userId}")
    fun removeParticipant(@Path("tripId") tripId: Int, @Path("userId") userId: Int): Completable
}