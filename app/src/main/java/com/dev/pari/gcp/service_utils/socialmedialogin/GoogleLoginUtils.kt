package com.dev.pari.gcp.service_utils.socialmedialogin

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import androidx.lifecycle.MediatorLiveData
import com.dev.pari.gcp.common.Constants.GOOGLE_SIGN_IN_CODE
import com.dev.pari.gcp.common.Utils
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class GoogleLoginUtils(private val mContext: Context, private val mActivity: Activity) {

    private lateinit var googleSignInOpt: GoogleSignInOptions
    private lateinit var googleSignClient: GoogleSignInClient
    private var responseCallBack = MediatorLiveData<AccountDetails>()
    private var utils: Utils = Utils(mContext)
    val googleSignInResponse = responseCallBack

    // Initialize Google SignIn Opt and Client
    // Before call this method you must add your application in google console -->>>> https://console.cloud.google.com
    fun login() {
        googleSignInOpt =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail()
                .build()
        googleSignClient = GoogleSignIn.getClient(mContext, googleSignInOpt)
        if (utils.isNetworkAvailable)
            mActivity.startActivityForResult(googleSignClient.signInIntent, GOOGLE_SIGN_IN_CODE)
        else
            utils.shortToast("Please check your internet connections")
    }

    // Get current account details
    fun getAccountDetails() {
        val account = GoogleSignIn.getLastSignedInAccount(mContext)!!
        responseCallBack.postValue(
            AccountDetails(
                loginStatus = true,
                logOutStatus = false,
                error = null,
                userName = account.displayName,
                userEmail = account.email,
                profileImage = account.photoUrl.toString()
            )
        )
    }

    // Check if already signed or not
    fun ifAlreadyLogin(): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(mContext)
        return account != null
    }

    // Current user sign out
    fun signOutCurrentLogin() {
        googleSignInOpt =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail()
                .build()
        googleSignClient = GoogleSignIn.getClient(mContext, googleSignInOpt)
        googleSignClient.signOut().addOnSuccessListener {
            responseCallBack.postValue(
                AccountDetails(
                    loginStatus = false,
                    logOutStatus = true,
                    error = null,
                    userName = null,
                    userEmail = null,
                    profileImage = null
                )
            )
        }.addOnFailureListener {
            responseCallBack.postValue(
                AccountDetails(
                    loginStatus = false,
                    logOutStatus = false,
                    error = it.message,
                    userName = null,
                    userEmail = null,
                    profileImage = null
                )
            )
        }
    }

    // add your activity or fragment below onActivityResult Place
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == GOOGLE_SIGN_IN_CODE)
            if (resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    val response = task.getResult(ApiException::class.java)
                    responseCallBack.postValue(
                        AccountDetails(
                            loginStatus = true,
                            logOutStatus = false,
                            error = null,
                            userName = response.displayName,
                            userEmail = response.email,
                            profileImage = response.photoUrl.toString()
                        )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    googleSignInResponse.postValue(
                        AccountDetails(
                            loginStatus = false,
                            logOutStatus = false,
                            error = e.message,
                            userName = null,
                            userEmail = null,
                            profileImage = null
                        )
                    )
                    utils.shortToast("Failed")
                }
            }
    }

    data class AccountDetails(
        val loginStatus: Boolean,
        val logOutStatus: Boolean,
        val error: String?,
        val userName: String?,
        val userEmail: String?,
        val profileImage: String?,
    )

}