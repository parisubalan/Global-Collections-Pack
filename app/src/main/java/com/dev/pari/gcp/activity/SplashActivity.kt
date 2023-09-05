package com.dev.pari.gcp.activity

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.dev.pari.gcp.common.Constants
import com.dev.pari.gcp.common.Utils
import com.dev.pari.gcp.databinding.ActivitySplashBinding
import com.dev.pari.gcp.utils.inappupdate.InAppUpdateCallBack
import com.dev.pari.gcp.utils.inappupdate.InAppUpdateManager
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.concurrent.TimeUnit

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var utils: Utils
    private lateinit var splashTimer: CompositeDisposable
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialization()
    }

    private fun initialization() {
        splashTimer = CompositeDisposable()
        utils = Utils(this)
        utils.printHashKey(this)
        changeActivity()
        if (Constants.isInAppUpdateEnabled) {
            InAppUpdateManager(this, object : InAppUpdateCallBack {
                override fun isNotUpdateAvailable() {
                    changeActivity()
                }

                override fun isUpdateAvailable() {

                }

                override fun inAppUpdateSuccess() {
                    changeActivity()
                }

                override fun inAppUpdateFailure() {

                }

            }).checkUpdateAvailable()
        }
    }

    private fun changeActivity() {
        splashTimer = CompositeDisposable()
        splashTimer.add(
            Observable.interval(0, 2, TimeUnit.SECONDS).observeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread()).subscribe {
                    if (it.toString() == "2") {
                        splashTimer.dispose()
                        println("--->>>>> Final Count : $it")
                        utils.moveNextActivity(this, Intent(this, MainActivity::class.java), true)
                        FacebookSdk.sdkInitialize(applicationContext);
                        AppEventsLogger.activateApp(application);
                    }
                    println("--->>>>> Count : $it")
                }
        )
    }

    override fun onStop() {
        super.onStop()
        splashTimer.dispose()
    }

    override fun onDestroy() {
        super.onDestroy()
        splashTimer.dispose()
    }


}