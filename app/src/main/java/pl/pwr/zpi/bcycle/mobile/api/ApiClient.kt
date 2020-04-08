package pl.pwr.zpi.bcycle.mobile.api

import android.util.Log
import okhttp3.*
import pl.pwr.zpi.bcycle.mobile.API_BASE_URL
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory


object ApiClient {
    var retrofit: Retrofit? = null
        get() {
            if (field == null) {
                field = Retrofit.Builder()
                    .baseUrl(API_BASE_URL)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create()) // TODO handle dates properly
                    .client(okHttpClient)
                    .build()
            }
            return field
        }
        private set
    var tripApi: TripApi? = null
        get() {
            if (field == null) {
                field = retrofit!!.create(TripApi::class.java)
            }
            return field
        }
        private set
    var currentToken: String = "" // TODO provide a valid token


    private var okHttpClient =
        OkHttpClient().newBuilder().addInterceptor { chain ->
            val originalRequest: Request = chain.request()
            val builder: Request.Builder = originalRequest.newBuilder().header(
                "Authorization",
                "Bearer $currentToken"
            )
            val newRequest: Request = builder.build()
            chain.proceed(newRequest)
        }.build()
}