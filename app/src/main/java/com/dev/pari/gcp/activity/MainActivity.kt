package com.dev.pari.gcp.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.dev.pari.gcp.R
import com.dev.pari.gcp.common.Utils
import com.dev.pari.gcp.databinding.ActivityMainBinding
import com.dev.pari.gcp.utils.inappreview.InAppReviewCallBack
import com.dev.pari.gcp.utils.inappreview.InAppReviewManager
import com.dev.pari.gcp.utils.inappupdate.InAppUpdateCallBack
import com.dev.pari.gcp.utils.inappupdate.InAppUpdateManager


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var utils: Utils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialization()
    }

    private fun initialization() {
        utils = Utils(this)
        utils.printHashKey(this)
        binding.locationBtn.setOnClickListener(this)
        binding.inAppUpdateBtn.setOnClickListener(this)
        binding.inAppReviewBtn.setOnClickListener(this)
        binding.socialMediaLoginBtn.setOnClickListener(this)
        binding.fileUploadBtn.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.locationBtn -> {
                utils.moveNextActivity(this, Intent(this, LocationActivity::class.java), false)
            }

            R.id.inAppUpdateBtn -> {
                InAppUpdateManager(this, object : InAppUpdateCallBack {
                    override fun isNotUpdateAvailable() {
                        println("--->>>> isNotUpdateAvailable")
                    }

                    override fun isUpdateAvailable() {
                        println("--->>>> isUpdateAvailable")
                    }

                    override fun inAppUpdateSuccess() {
                        println("--->>>> inAppUpdateSuccess")
                    }

                    override fun inAppUpdateFailure() {
                        println("--->>>> inAppUpdateFailure")
                    }

                }).checkUpdateAvailable()
            }

            R.id.inAppReviewBtn -> {
                InAppReviewManager(this, object : InAppReviewCallBack {
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

            R.id.fileUploadBtn -> {
                utils.moveNextActivity(this, Intent(this, FileUploadActivity::class.java), false)
            }
        }
    }
}