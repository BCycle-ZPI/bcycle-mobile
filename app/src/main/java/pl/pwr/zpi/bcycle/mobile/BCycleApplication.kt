package pl.pwr.zpi.bcycle.mobile

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen

class BCycleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
    }
}
