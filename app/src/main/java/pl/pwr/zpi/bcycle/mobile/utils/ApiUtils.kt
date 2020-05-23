package pl.pwr.zpi.bcycle.mobile.utils

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

fun <T> Single<T>.background(): Single<T> = this
    .subscribeOn(Schedulers.io())
    .observeOn(AndroidSchedulers.mainThread())

fun Completable.background(): Completable = this
    .subscribeOn(Schedulers.io())
    .observeOn(AndroidSchedulers.mainThread())

