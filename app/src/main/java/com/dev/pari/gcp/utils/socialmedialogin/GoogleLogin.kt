package com.dev.pari.gcp.utils.socialmedialogin

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.dev.pari.gcp.common.Utils
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class GoogleLogin(private val mContext: Context, val mActivity: Activity ) {
    private lateinit var googleSignInOpt: GoogleSignInOptions
    private lateinit var googleSignClient: GoogleSignInClient
    private var responseCallBack = MediatorLiveData<GoogleSignInAccount>()
    private var utils: Utils = Utils(mContext)
    val loginResponse = responseCallBack

    // Initialize Google SignIn Opt and Client
    // Before call this method you must add your application in google console -->>>> https://console.cloud.google.com
    fun login() {
        googleSignInOpt =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail()
                .build()
        googleSignClient = GoogleSignIn.getClient(mContext, googleSignInOpt)
        if (utils.isNetworkAvailable)
            mActivity.startActivityForResult(googleSignClient.signInIntent, 100)
        else
            utils.shortToast("Please check your internet connections")
    }

    // Check if already signed or not here then get last signed account details
    fun checkAlreadyLoginOrNot(): LiveData<GoogleSignInAccount?> {
        signOutCurrentLogin()
        val data = MediatorLiveData<GoogleSignInAccount?>()
        val account = GoogleSignIn.getLastSignedInAccount(mContext)
        if (account != null)
            data.postValue(account)
        else
            data.postValue(null)
        return data
    }

    // Get current account details
    fun getLoginAccountDetails(): LiveData<GoogleSignInAccount?> {
        val data = MediatorLiveData<GoogleSignInAccount?>()
        val account = GoogleSignIn.getLastSignedInAccount(mContext)
        if (account != null)
            data.postValue(account)
        else
            data.postValue(null)
        return data
    }

    // Check if already signed or not
    fun ifAlreadyLogin(): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(mContext)
        return account != null
    }

    // Current user sign out
    fun signOutCurrentLogin(): LiveData<Boolean> {
        val data = MediatorLiveData<Boolean>()
        googleSignInOpt =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail()
                .build()
        googleSignClient = GoogleSignIn.getClient(mContext, googleSignInOpt)
        googleSignClient.signOut().addOnSuccessListener {
            data.postValue(true)
        }.addOnFailureListener {
            data.postValue(false)
        }
        return data
    }

    // add your activity or fragment below onActivityResult Place
     fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val response = task.getResult(ApiException::class.java)
                responseCallBack.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
                responseCallBack.postValue(null)
                utils.shortToast("Failed")
            }
        }
    }

}