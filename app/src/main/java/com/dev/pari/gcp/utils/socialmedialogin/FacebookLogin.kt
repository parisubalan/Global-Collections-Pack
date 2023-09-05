package com.dev.pari.gcp.utils.socialmedialogin

import android.app.Activity
import android.content.Context
import com.facebook.CallbackManager
import com.facebook.FacebookSdk
import com.facebook.login.LoginManager




class FacebookLogin(val mContext : Context, val mActivity : Activity) {

    lateinit var callBackManager : CallbackManager
    private val loginManager: LoginManager? = null
}