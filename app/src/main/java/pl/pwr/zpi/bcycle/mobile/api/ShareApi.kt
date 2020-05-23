package pl.pwr.zpi.bcycle.mobile.api

import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.*

interface ShareApi {
    @POST("share/{id}")
    fun startSharing(@Path("id") id: Int): Single<ApiResultContainer<String>>

    @DELETE("share/{id}")
    fun stopSharing(@Path("id") id: Int): Completable
}