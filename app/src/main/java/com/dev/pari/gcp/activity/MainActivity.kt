package com.dev.pari.gcp.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.dev.pari.gcp.R
import com.dev.pari.gcp.common.Utils
import com.dev.pari.gcp.databinding.ActivityMainBinding
import com.dev.pari.gcp.service_utils.inappreview.InAppReviewManager
import com.dev.pari.gcp.service_utils.inappupdate.InAppUpdateManager


class MainActivity : AppCompatActivity(), View.OnClickListener,
    InAppUpdateManager.InAppUpdateCallBack {

    private lateinit var binding: ActivityMainBinding
    private lateinit var utils: Utils
    private lateinit var inAppUpdateManager: InAppUpdateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialization()
    }

    private fun initialization() {
        utils = Utils(this)
        inAppUpdateManager = InAppUpdateManager(this, this)
        utils.printHashKey(this)
        binding.locationBtn.setOnClickListener(this)
        binding.inAppUpdateBtn.setOnClickListener(this)
        binding.inAppReviewBtn.setOnClickListener(this)
        binding.socialMediaLoginBtn.setOnClickListener(this)
        binding.androidServiceBtn.setOnClickListener(this)
        binding.fileUploadBtn.setOnClickListener(this)
        binding.paypalBtn.setOnClickListener(this)
        binding.phonePayBtn.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.locationBtn -> {
                utils.moveNextActivity(this, Intent(this, LocationActivity::class.java), false)
            }

            R.id.phonePayBtn -> {
                utils.moveNextActivity(this, Intent(this, PhonePeActivity::class.java), false)
            }

            R.id.inAppUpdateBtn -> {
                inAppUpdateManager.checkUpdateAvailable()
            }

            R.id.inAppReviewBtn -> {
                InAppReviewManager(this, object : InAppReviewManager.InAppReviewCallBack {
                    override fun isNotReview() {
                        println("--->>>> isNotReview")
                    }

                    override fun alreadyReviewed() {
                        println("--->>>> alreadyReviewed")
                    }

                    override fun postReviewSuccess() {
                        println("--->>>> postReviewSuccess")
                    }

                    override fun postReviewFailure() {
                        println("--->>>> postReviewFailure")
                    }
                }).checkInAppReview()
            }

            R.id.socialMediaLoginBtn -> {
                utils.moveNextActivity(
                    this,
                    Intent(this, SocialMediaLoginActivity::class.java),
                    false
                )
            }

            R.id.androidServiceBtn -> {
                utils.moveNextActivity(
                    this,
                    Intent(this, AndroidServiceActivity::class.java),
                    false
                )
            }

            R.id.fileUploadBtn -> {
                utils.moveNextActivity(this, Intent(this, FileUploadActivity::class.java), false)
            }

            R.id.paypalBtn -> {
                utils.moveNextActivity(this, Intent(this, CardPaymentActivity::class.java), false)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        inAppUpdateManager.onActivityResult(requestCode, resultCode, data)
    }

    override fun isNotUpdateAvailable() {

    }

    override fun inAppUpdateFailure() {

    }
}