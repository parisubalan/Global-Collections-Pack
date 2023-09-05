package com.dev.pari.gcp.activity

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.dev.pari.gcp.databinding.ActivitySocialMediaLoginBinding
import com.facebook.CallbackManager
import com.facebook.CallbackManager.Factory.create
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult


class FaceBookLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySocialMediaLoginBinding
    private lateinit var callBackManger: CallbackManager
    private lateinit var loginManager: LoginManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySocialMediaLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialization()
    }

    private fun initialization() {
        binding.googleSignBtn.visibility = ViewGroup.GONE
        binding.tvProfileEmail.visibility = ViewGroup.GONE
        binding.tvProfileName.visibility = ViewGroup.GONE

        callBackManger = CallbackManager.Factory.create()
        facebookLogin()

        binding.faceBookLoginBtn.setOnClickListener {
            loginManager.logInWithReadPermissions(
                this,
                listOf(
                    "public_profile","email"
                )
            )
        }
    }

    private fun facebookLogin() {
        loginManager = LoginManager.getInstance()
        callBackManger = create()
        loginManager
            .registerCallback(
                callBackManger,
                object : FacebookCallback<LoginResult> {
                    override fun onCancel() {

                    }

                    override fun onError(error: FacebookException) {

                    }

                    override fun onSuccess(result: LoginResult) {
                        println("-->>> Access T oken : ${result.accessToken}")
                        println("-->>> authentication token : ${result.authenticationToken}")
                    }

                })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callBackManger.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

}