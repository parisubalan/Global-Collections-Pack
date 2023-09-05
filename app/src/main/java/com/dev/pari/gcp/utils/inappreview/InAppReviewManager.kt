package com.dev.pari.gcp.utils.inappreview

import android.app.Activity
import com.dev.pari.gcp.common.Constants
import com.dev.pari.gcp.common.Utils
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.model.ReviewErrorCode

class InAppReviewManager(context: Activity, inAppReviewCallBack: InAppReviewCallBack) {

    private val reviewCallBack = inAppReviewCallBack
    private val mContext = context
    private val utils = Utils(mContext)
    private val reviewManager = ReviewManagerFactory.create(mContext)

    fun checkInAppReview() {
        try {
            if (Constants.isInAppReviewEnabled) {
                val reviewReq = reviewManager.requestReviewFlow()
                reviewReq.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val reviewObject = task.result
                        val reviewReqFlow = reviewManager.launchReviewFlow(mContext, reviewObject)
                        reviewReqFlow.addOnCompleteListener {
                            try {
                                if (it.isComplete)
                                    reviewCallBack.postReviewSuccess()
                                else if (it.isSuccessful)
                                    reviewCallBack.postReviewSuccess()
                                else if (it.isCanceled)
                                    reviewCallBack.postReviewFailure()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    } else {
                        // There was some problem, log or handle the error code.
                        @ReviewErrorCode val reviewErrorCode =
                            (task.exception as ReviewException).errorCode
                        println("--->>>>>> In App Review Failure : ${(task.exception as ReviewException).message}")
                        reviewCallBack.postReviewFailure()
                    }
                }
            } else
                throw RuntimeException(
                    " 'isInAppReviewEnabled' variable value was present in false " +
                            "kindly check 'Constants' file and change a variable value is 'true'"
                )
        } catch (e: Exception) {
            utils.shortToast(e.message)
            e.printStackTrace()
            println("--->>>> In App Review Exception : " + e.message)
        }
    }
}