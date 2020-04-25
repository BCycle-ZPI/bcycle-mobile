package pl.pwr.zpi.bcycle.mobile

import android.os.Parcel
import android.os.Parcelable

class MyMarker(var latitude:Double, var longitude:Double) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readDouble(),
        parcel.readDouble()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MyMarker> {
        override fun createFromParcel(parcel: Parcel): MyMarker {
            return MyMarker(parcel)
        }

        override fun newArray(size: Int): Array<MyMarker?> {
            return arrayOfNulls(size)
        }
    }
}