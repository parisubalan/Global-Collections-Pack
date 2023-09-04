package com.dev.pari.gcp.inappupdate

interface InAppUpdateCallBack {
    fun isNotUpdateAvailable()
    fun isUpdateAvailable()
    fun inAppUpdateSuccess()
    fun inAppUpdateFailure()
}