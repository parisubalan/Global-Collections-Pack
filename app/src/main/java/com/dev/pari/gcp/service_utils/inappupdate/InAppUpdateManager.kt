package com.dev.pari.gcp.service_utils.inappupdate

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import com.dev.pari.gcp.common.Constants
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability

class InAppUpdateManager(
    private val mContext: Activity,
    private val updateCallBack: InAppUpdateCallBack
) {

    private val updateManager = AppUpdateManagerFactory.create(mContext)
    private var updatePriority = 0

    fun checkUpdateAvailable() {
        try {
            if (Constants.isInAppUpdateEnabled) {
                val updateInfoTask = updateManager.appUpdateInfo
                if (Constants.needImmediateUpdate)
                    updateInfoTask.addOnSuccessListener {
                        println("--->>> Priority --- ${it.updatePriority()}")
                        updatePriority = it.updatePriority()
                        if (it.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && it.isUpdateTypeAllowed(
                                AppUpdateType.IMMEDIATE
                            )
                        )
                            requestAppUpdate(updateManager, it)
                        else if (it.updateAvailability() == UpdateAvailability.UPDATE_NOT_AVAILABLE)
                            updateCallBack.isNotUpdateAvailable()
                        else if (it.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS)
                            requestAppUpdate(updateManager, it)
                    }.addOnFailureListener {
                        println("--->>> In app update failure : ${it.message}")
                        updateCallBack.inAppUpdateFailure()
                    }
                else if (Constants.needFlexibleUpdate)
                    updateInfoTask.addOnSuccessListener {
                        if (it.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                            && it.clientVersionStalenessDays() != null
                            && it.clientVersionStalenessDays()!! >= Constants.DAYS_FOR_FLEXIBLE_UPDATE
                            && it.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                        ) {
                            requestAppUpdate(updateManager, it)
                        } else
                            updateCallBack.isNotUpdateAvailable()
                    }.addOnFailureListener {
                        println("--->>> In app update failure : ${it.message}")
                        updateCallBack.inAppUpdateFailure()
                    }
                else
                    throw RuntimeException(
                        " 'needFlexibleUpdate' and 'needImmediateUpdate' both variable value was present in false " +
                                "kindly check 'Constants' file and change any one variable value is 'true'"
                    )
            } else
                throw RuntimeException(
                    " 'isInAppUpdateEnabled' variable value was present in false " +
                            "kindly check 'Constants' file and change a variable value is 'true'"
                )
        } catch (e: Exception) {
            e.printStackTrace()
            println("--->>>> In App Update Exception : " + e.message)
        }

    }

    private fun requestAppUpdate(appUpdateManger: AppUpdateManager, appUpdateInfo: AppUpdateInfo) {
        try {
            val updateOption = if (Constants.needImmediateUpdate)
                AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE)
                    .setAllowAssetPackDeletion(true)
                    .build()
            else if (Constants.needFlexibleUpdate)
                AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE)
                    .setAllowAssetPackDeletion(true)
                    .build()
            else
                throw RuntimeException(
                    " 'needFlexibleUpdate' and 'needImmediateUpdate' both variable value was present in false " +
                            "kindly check 'Constants' file and change any one variable value is 'true'"
                )
            appUpdateManger.startUpdateFlowForResult(
                appUpdateInfo,
                AppUpdateType.IMMEDIATE,
                mContext,
                Constants.IN_APP_UPDATE_REQ_CODE
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onResume() {
        updateManager.appUpdateInfo.addOnSuccessListener {
            if (it.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && it.isUpdateTypeAllowed(
                    AppUpdateType.IMMEDIATE
                )
            ) {
                requestAppUpdate(updateManager, it)
            }
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Constants.IN_APP_UPDATE_REQ_CODE)
            if (resultCode != RESULT_OK)
                checkUpdateAvailable()
    }

    interface InAppUpdateCallBack {
        fun isNotUpdateAvailable()
        fun inAppUpdateFailure()
    }
}