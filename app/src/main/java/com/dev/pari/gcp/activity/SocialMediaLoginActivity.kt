package com.dev.pari.gcp.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.dev.pari.gcp.R
import com.dev.pari.gcp.common.Constants
import com.dev.pari.gcp.common.Utils
import com.dev.pari.gcp.databinding.ActivitySocialMediaLoginBinding
import com.dev.pari.gcp.service_utils.socialmedialogin.FaceBookLoginUtils
import com.dev.pari.gcp.service_utils.socialmedialogin.GoogleLoginUtils


class SocialMediaLoginActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivitySocialMediaLoginBinding
    private lateinit var googleLoginUtils: GoogleLoginUtils
    private lateinit var facebookLogin: FaceBookLoginUtils
    private lateinit var utils: Utils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySocialMediaLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialization()
    }

    private fun initialization() {
        utils = Utils(this)
        googleLoginUtils = GoogleLoginUtils(this, this)
        facebookLogin = FaceBookLoginUtils(this)

        binding.googleSignBtn.setOnClickListener(this)
        binding.googleSignOutBtn.setOnClickListener(this)
        binding.faceBookLoginBtn.setOnClickListener(this)
        binding.faceBookLogoutBtn.setOnClickListener(this)

        googleLoginUtils.googleSignInResponse.observe(this) {
            if (it != null)
                if (it.loginStatus) {
                    binding.gmailDetailLay.visibility = ViewGroup.VISIBLE
                    binding.googleSignBtn.visibility = ViewGroup.GONE
                    binding.googleSignOutBtn.visibility = ViewGroup.VISIBLE
                    binding.tvProfileName.text = it.userName
                    binding.tvProfileEmail.text = it.userEmail
                    Glide.with(this).load(it.profileImage).into(binding.googleProfileImage)
                } else if (it.logOutStatus) {
                    binding.googleSignBtn.visibility = ViewGroup.VISIBLE
                    binding.googleSignOutBtn.visibility = ViewGroup.GONE
                    binding.gmailDetailLay.visibility = ViewGroup.GONE
                } else if (it.error!!.isNotEmpty())
                    utils.shortToast(it.error)
        }

        facebookLogin.facebookLoginResponse.observe(this) {
            if (it != null) {
                if (it.loginStatus) {
                    binding.fbDetailLay.visibility = ViewGroup.VISIBLE
                    binding.faceBookLoginBtn.visibility = ViewGroup.GONE
                    binding.faceBookLogoutBtn.visibility = ViewGroup.VISIBLE
                    binding.tvFacebookProfileName.text = it.userName
                    binding.tvFacebookProfileEmail.text = it.email
                    Glide.with(this@SocialMediaLoginActivity).load(it.profileImageUrl)
                        .into(binding.facebookProfileImage)
                } else if (it.logoutStatus) {
                    binding.faceBookLoginBtn.visibility = ViewGroup.VISIBLE
                    binding.faceBookLogoutBtn.visibility = ViewGroup.GONE
                    binding.fbDetailLay.visibility = ViewGroup.GONE
                } else if (it.isCancelled)
                    utils.shortToast("Login cancelled")
                else if (it.error!!.isNotEmpty())
                    utils.shortToast(it.error)
            }
        }
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Constants.GOOGLE_SIGN_IN_CODE -> {
                googleLoginUtils.onActivityResult(requestCode, resultCode, data)
            }

            Constants.FACEBOOK_SIGN_IN_CODE -> {
                facebookLogin.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.googleSignBtn -> {
                if (googleLoginUtils.ifAlreadyLogin()) {
                    googleLoginUtils.getAccountDetails()
                } else
                    googleLoginUtils.login()
            }

            R.id.googleSignOutBtn -> {
                googleLoginUtils.signOutCurrentLogin()
            }

            R.id.faceBookLoginBtn -> {
                if (facebookLogin.isAlreadyLogin())
                    facebookLogin.loadPreviousUser()
                else
                    facebookLogin.initiateLogin()
            }

            R.id.faceBookLogoutBtn -> {
                facebookLogin.logoutCurrentUser()
            }
        }
    }
}