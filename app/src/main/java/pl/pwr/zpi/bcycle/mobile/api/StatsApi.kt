package pl.pwr.zpi.bcycle.mobile.api

import io.reactivex.Single
import pl.pwr.zpi.bcycle.mobile.models.UserStats
import retrofit2.http.*

interface StatsApi {
    @GET("stats")
    fun getUserStats(): Single<ApiResultContainer<UserStats>>
}
