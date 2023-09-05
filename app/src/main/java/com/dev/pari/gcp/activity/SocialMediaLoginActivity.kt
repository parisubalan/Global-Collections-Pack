package com.dev.pari.gcp.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.dev.pari.gcp.R
import com.dev.pari.gcp.databinding.ActivitySocialMediaLoginBinding
import com.dev.pari.gcp.utils.socialmedialogin.GoogleLogin
import com.google.android.gms.auth.api.signin.GoogleSignInAccount


class SocialMediaLoginActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivitySocialMediaLoginBinding
    private lateinit var googleLogin: GoogleLogin


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySocialMediaLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialization()
    }

    private fun initialization() {
        googleLogin = GoogleLogin(this, this)
        binding.googleSignBtn.setOnClickListener(this)
        binding.googleSignOutBtn.setOnClickListener(this)
//        binding.faceBookLoginBtn.setOnClickListener(this)

        googleLogin.loginResponse.observe(this) {
            if (it != null) {
                parseGoogleProfileUI(it)
            } else {
                binding.googleSignBtn.visibility = ViewGroup.VISIBLE
                binding.googleSignOutBtn.visibility = ViewGroup.GONE
            }
        }
    }

    private fun parseGoogleProfileUI(it: GoogleSignInAccount) {
        binding.googleSignBtn.visibility = ViewGroup.GONE
        binding.googleSignOutBtn.visibility = ViewGroup.VISIBLE
        binding.tvProfileName.text = it.displayName
        binding.tvProfileEmail.text = it.email
        Glide.with(this).load(it.photoUrl).into(binding.googleProfileImage)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        googleLogin.onActivityResult(requestCode, resultCode, data)
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.googleSignBtn -> {
                if (googleLogin.ifAlreadyLogin()) {
                    googleLogin.checkAlreadyLoginOrNot().observe(this) {
                        if (it != null)
                            parseGoogleProfileUI(it)
                    }
                } else
                    googleLogin.login()
            }

            R.id.googleSignOutBtn -> {
                googleLogin.signOutCurrentLogin().observe(this) {
                    if (it == true) {
                        binding.tvProfileName.text = ""
                        binding.tvProfileEmail.text = ""
                        Glide.with(this).load("").into(binding.googleProfileImage)
                        binding.googleSignBtn.visibility = ViewGroup.VISIBLE
                        binding.googleSignOutBtn.visibility = ViewGroup.GONE
                    }
                }
            }
        }
    }
}