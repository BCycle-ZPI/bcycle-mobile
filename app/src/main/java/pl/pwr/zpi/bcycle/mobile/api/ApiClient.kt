package pl.pwr.zpi.bcycle.mobile.api

import com.google.android.gms.tasks.Tasks
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import okhttp3.OkHttpClient
import okhttp3.Request
import org.threeten.bp.ZonedDateTime
import pl.pwr.zpi.bcycle.mobile.API_BASE_URL
import pl.pwr.zpi.bcycle.mobile.HTTP_TIMEOUT_S
import pl.pwr.zpi.bcycle.mobile.utils.dateFromIso
import pl.pwr.zpi.bcycle.mobile.utils.dateToIso
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object ApiClient {
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .build()
    }

    val tripApi: TripApi by lazy {
        retrofit.create(TripApi::class.java)
    }

    private val gson = GsonBuilder().registerTypeAdapter(ZonedDateTime::class.java, object :
        TypeAdapter<ZonedDateTime>() {
        override fun read(reader: JsonReader): ZonedDateTime? {
            if (reader.peek() === JsonToken.NULL) {
                reader.nextNull()
                return null
            }
            val dtString: String = reader.nextString()
            return dateFromIso(dtString)
        }

        override fun write(writer: JsonWriter, value: ZonedDateTime?) {
            if (value == null) {
                writer.nullValue()
                return
            }
            writer.value(dateToIso(value))
        }
    }).create()

    private fun updateRequestWithToken(request: Request): Request =
        request.newBuilder().header(
            "Authorization",
            "Bearer ${ApiTokenManager.token}"
        ).build()

    private val okHttpClient =
        OkHttpClient().newBuilder()
            .connectTimeout(HTTP_TIMEOUT_S, TimeUnit.SECONDS)
            .readTimeout(HTTP_TIMEOUT_S, TimeUnit.SECONDS)
            .writeTimeout(HTTP_TIMEOUT_S, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val originalRequest: Request = chain.request()
                val newRequest: Request = updateRequestWithToken(originalRequest)
                val response = chain.proceed(newRequest)
                if (response.code() == 401) {
                    Tasks.await(ApiTokenManager.updateToken()!!)
                    chain.proceed(updateRequestWithToken(originalRequest))
                } else {
                    response
                }
            }.build()
}