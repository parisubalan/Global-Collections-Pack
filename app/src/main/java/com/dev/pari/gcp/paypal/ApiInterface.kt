package com.dev.pari.gcp.paypal

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiInterface {

    @GET("paypal_mobile/authorization")
    fun getAccessToken(): Call<ResponseBody>

    @POST("paypal_mobile/create_order")
    fun createOrder(@Header("Authorization") accessToken: String, @Body orderReq: CreateOrderReq): Call<ResponseBody>

    @POST("paypal_mobile/order_capture/{orderId}")
    fun createAuthorize(@Header("Authorization") accessToken: String, @Path("orderId") orderId: String, @Body orderAuthReq : OrderAuthReq): Call<ResponseBody>
}