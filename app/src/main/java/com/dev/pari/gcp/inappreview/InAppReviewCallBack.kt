package com.dev.pari.gcp.inappreview

interface InAppReviewCallBack {
    fun isNotReview()
    fun alreadyReviewed()
    fun postReviewSuccess()
    fun postReviewFailure()
}