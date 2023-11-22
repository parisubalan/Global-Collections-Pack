package com.dev.pari.gcp.service_utils.socialmedialogin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.MediatorLiveData
import com.dev.pari.gcp.common.Constants
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.CallbackManager.Factory.create
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult


class FaceBookLoginUtils(private val mActivity: Activity) {

    private lateinit var callBackManger: CallbackManager
    private lateinit var loginManager: LoginManager
    private val facebookLoginState = MediatorLiveData<UserDetails>()
    val facebookLoginResponse = facebookLoginState

    fun initiateLogin() {
        callBackManger = create()
        loginManager = LoginManager.getInstance()
        val accessToken = AccessToken.getCurrentAccessToken()
        if (accessToken != null && !accessToken.isExpired) {
            requestGetData(accessToken)
        } else {
            loginManager.logInWithReadPermissions(
                mActivity,
                listOf(
                    "public_profile", "email"
                )
            )
            facebookLoginCallBack()
        }
    }

    fun isAlreadyLogin(): Boolean {
        val accessToken = AccessToken.getCurrentAccessToken()
        return accessToken != null && !accessToken.isExpired
    }

    fun loadPreviousUser() {
        val accessToken = AccessToken.getCurrentAccessToken()
        requestGetData(accessToken!!)
    }

    fun logoutCurrentUser() {
        LoginManager.getInstance().logOut()
        facebookLoginState.postValue(
            UserDetails(
                loginStatus = false,
                logoutStatus = true,
                isCancelled = false,
                error = null,
                accessToken = null,
                userId = null,
                userName = null,
                email = null,
                profileImageUrl = null
            )
        )
    }

    private fun requestGetData(accessToken: AccessToken) {
        val request =
            GraphRequest.newMeRequest(
                accessToken
            ) { obj, _ ->
                println("--->>>> Response $obj")
                val id = obj?.getString("id")
                val name = obj?.getString("name")
//                val email = obj?.getString("email") ?: ""
                val profileImageUrl =
                    obj?.getJSONObject("picture")?.getJSONObject("data")?.get("url").toString()

                facebookLoginState.postValue(
                    UserDetails(
                        loginStatus = true,
                        logoutStatus = false,
                        isCancelled = false,
                        error = "",
                        accessToken = accessToken,
                        userId = id,
                        userName = name,
                        email = "email",
                        profileImageUrl = profileImageUrl
                    )
                )
            }
        val parameter = Bundle()
        parameter.putString("fields", "id,name,link,picture.type(large),email")
        request.parameters = parameter

        request.executeAsync()
    }

    private fun facebookLoginCallBack() {
        loginManager.registerCallback(
            callBackManger,
            object : FacebookCallback<LoginResult> {
                override fun onCancel() {
                    facebookLoginResponse.postValue(
                        UserDetails(
                            loginStatus = false,
                            logoutStatus = false,
                            true,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null
                        )
                    )
                    println("--->>>> Login Cancelled....")
                }

                override fun onError(error: FacebookException) {
                    facebookLoginResponse.postValue(
                        UserDetails(
                            loginStatus = false,
                            logoutStatus = false,
                            isCancelled = false,
                            error = error.message,
                            accessToken = null,
                            userId = null,
                            userName = null,
                            email = null,
                            profileImageUrl = null
                        )
                    )
                    println("--->>>> Login Error ${error.message}")
                }

                override fun onSuccess(result: LoginResult) {
//                    accessToken = AccessToken.getCurrentAccessToken()
//                    requestGetData(accessToken!!)
                    val accessToken = result.accessToken
                    requestGetData(accessToken)
                }
            })
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Constants.FACEBOOK_SIGN_IN_CODE)
            if (resultCode == Activity.RESULT_OK) {
                callBackManger.onActivityResult(requestCode, resultCode, data)
            }
    }

    data class UserDetails(
        val loginStatus: Boolean,
        val logoutStatus: Boolean,
        val isCancelled: Boolean,
        val error: String?,
        val accessToken: AccessToken?,
        val userId: String?,
        val userName: String?,
        val email: String?,
        val profileImageUrl: String?
    )
}