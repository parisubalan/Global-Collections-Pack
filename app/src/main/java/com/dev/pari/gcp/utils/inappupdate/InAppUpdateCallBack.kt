package com.dev.pari.gcp.utils.inappupdate

interface InAppUpdateCallBack {
    fun isNotUpdateAvailable()
    fun isUpdateAvailable()
    fun inAppUpdateSuccess()
    fun inAppUpdateFailure()
}