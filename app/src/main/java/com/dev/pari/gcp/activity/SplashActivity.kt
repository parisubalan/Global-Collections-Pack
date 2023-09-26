package com.dev.pari.gcp.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dev.pari.gcp.common.Constants
import com.dev.pari.gcp.common.Utils
import com.dev.pari.gcp.databinding.ActivitySplashBinding
import com.dev.pari.gcp.service_utils.inappupdate.InAppUpdateManager
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity(), InAppUpdateManager.InAppUpdateCallBack {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var utils: Utils
    private lateinit var splashTimer: CompositeDisposable
    private lateinit var inAppUpdateManager: InAppUpdateManager

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
        inAppUpdateManager = InAppUpdateManager(this, this)
        changeActivity()
    }

    private fun changeActivity() {
        splashTimer = CompositeDisposable()
        splashTimer.add(
            Observable.interval(0, 2, TimeUnit.SECONDS).observeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread()).subscribe {
                    if (it.toString() == "2") {
                        splashTimer.dispose()
                        println("--->>>>> Final Count : $it")
                        if (Constants.isInAppUpdateEnabled)
                            inAppUpdateManager.checkUpdateAvailable()
                        else {
                            utils.moveNextActivity(
                                this,
                                Intent(this, MainActivity::class.java),
                                true
                            )
                            FacebookSdk.sdkInitialize(applicationContext);
                            AppEventsLogger.activateApp(application);
                        }
                    }
                    println("--->>>>> Count : $it")
                }
        )
    }

    override fun onResume() {
        super.onResume()
        inAppUpdateManager.onResume()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        inAppUpdateManager.onActivityResult(requestCode, resultCode, data)
    }

    override fun onStop() {
        super.onStop()
        splashTimer.dispose()
    }

    override fun onDestroy() {
        super.onDestroy()
        splashTimer.dispose()
    }

    override fun isNotUpdateAvailable() {
        utils.moveNextActivity(
            this,
            Intent(this, MainActivity::class.java),
            true
        )
    }

    override fun inAppUpdateFailure() {
        utils.moveNextActivity(
            this,
            Intent(this, MainActivity::class.java),
            true
        )
    }


}