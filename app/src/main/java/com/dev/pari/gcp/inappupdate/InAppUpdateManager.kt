package com.dev.pari.gcp.inappupdate

import android.content.Context
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.dev.pari.gcp.common.Constants
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability

class InAppUpdateManager(context: Context, inAppUpdateCallBack: InAppUpdateCallBack) :
    AppCompatActivity() {

    private val updateCallBack = inAppUpdateCallBack
    private val mContext = context

    fun checkUpdateAvailable() {
        try {
            if (Constants.isInAppUpdateEnabled) {
                val updateManager = AppUpdateManagerFactory.create(mContext)
                val updateInfoTask = updateManager.appUpdateInfo
                if (Constants.needImmediateUpdate)
                    updateInfoTask.addOnSuccessListener {
                        if (it.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && it.isUpdateTypeAllowed(
                                AppUpdateType.IMMEDIATE
                            )
                        ) {
                            updateCallBack.isUpdateAvailable()
//                            requestAppUpdate(it, mContext)
                        } else
                            updateCallBack.isNotUpdateAvailable()
                    }
                else if (Constants.needFlexibleUpdate)
                    updateInfoTask.addOnSuccessListener {
                        if (it.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                            && it.clientVersionStalenessDays() != null
                            && it.clientVersionStalenessDays()!! >= Constants.DAYS_FOR_FLEXIBLE_UPDATE
                            && it.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                        ) {
                            updateCallBack.isUpdateAvailable()
//                            requestAppUpdate(it, mContext)
                        } else
                            updateCallBack.isNotUpdateAvailable()
                    }
                else
                    throw RuntimeException(
                        " 'needFlexibleUpdate' and 'needImmediateUpdate' both variable value was present in false " +
                                "kindly check 'Constants' file and change any one variable value is 'true'"
                    )

                updateInfoTask.addOnFailureListener {
                    println("---->>>>> ${it.message}")
                    updateCallBack.inAppUpdateFailure()
                }
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

    private fun requestAppUpdate(appUpdateInfo: AppUpdateInfo, context: Context) {
        val updateManager = AppUpdateManagerFactory.create(context)
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

        updateManager.startUpdateFlowForResult(
            // Pass the intent that is returned by 'getAppUpdateInfo()'.
            appUpdateInfo,
            // an activity result launcher registered via registerForActivityResult
            activityResultLauncher,
            // Or pass 'AppUpdateType.FLEXIBLE' to newBuilder() for
            // flexible updates.
            updateOption
        )
    }

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            if (it.resultCode == RESULT_OK)
                updateCallBack.inAppUpdateSuccess()
            else
                updateCallBack.inAppUpdateFailure()
        }
}