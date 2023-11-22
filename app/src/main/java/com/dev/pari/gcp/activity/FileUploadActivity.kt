package com.dev.pari.gcp.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.dev.pari.gcp.paypal.ApiInterface
import com.dev.pari.gcp.paypal.OrderAuthReq
import com.dev.pari.gcp.common.Utils
import com.dev.pari.gcp.databinding.ActivityFileUploadBinding
import com.dev.pari.gcp.service_utils.DeviceFileUtils
import com.google.gson.GsonBuilder
import com.paypal.android.cardpayments.ApproveOrderListener
import com.paypal.android.cardpayments.CardResult
import com.paypal.android.corepayments.PayPalSDKError
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

class FileUploadActivity : AppCompatActivity(), ApproveOrderListener {

    private lateinit var binding: ActivityFileUploadBinding
    private lateinit var fileUtils: DeviceFileUtils
    private lateinit var utils: Utils
    private var apiInterface: ApiInterface? = null
    private var retrofit: Retrofit? = null
    private var orderId = ""
    private var accessToken = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFileUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialization()
    }

    override fun onNewIntent(newIntent: Intent?) {
        super.onNewIntent(newIntent)
        println("--->>>>> onNewIntent  :::::")
        val data = intent.data // Get the data from the intent

        if (data != null) {
            orderId = data.toString().split("/")[3].split("orderId?")[1]
            val token = data.toString().split("/")[4].split("access?")[1].replace("Bearer","")
            accessToken = "Bearer ${token.split("&error")[0]}"
            println("--->>>> ORDER ID ::: $orderId")
            println("--->>>> ORDER STATUS ::: $accessToken")
        }
    }

    private fun initialization() {
        onNewIntent(intent)
        utils = Utils(this)
        fileUtils = DeviceFileUtils(this)
//        binding.uploadBtn.setOnClickListener {
////            val intent = Intent(Intent.ACTION_GET_CONTENT)
////            intent.action = Intent.ACTION_GET_CONTENT
////            intent.type = "application/pdf"
////            resultLauncher.launch(intent)
//
//        }

        if (retrofit == null) {
            val gson = GsonBuilder()
                .setLenient()
                .create()
            val httpInterceptor = HttpLoggingInterceptor()
            httpInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            val okHttpClient: OkHttpClient = OkHttpClient.Builder()
                .addInterceptor(httpInterceptor)
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build()
            retrofit = Retrofit.Builder()
                .baseUrl("https://diemapp-node.herokuapp.com/")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
            apiInterface = retrofit?.create(ApiInterface::class.java)
        }

        binding.uploadBtn.setOnClickListener {
            createAuthorizeOrCapture(accessToken, orderId, OrderAuthReq(apiType = "capture"))
        }
    }

    private fun createAuthorizeOrCapture(
        accessToken: String,
        orderId: String,
        orderAuthReq: OrderAuthReq,
    ) {
        val getClientIdCall = apiInterface?.createAuthorize(accessToken, orderId, orderAuthReq)

        getClientIdCall?.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200) {
                    Toast.makeText(this@FileUploadActivity, "Success", Toast.LENGTH_SHORT).show()
                    println("--->>>>> Integration Completed :::::")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

        })
    }


    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val uri = it?.data?.data
                val file = File(uri!!.path!!)
                val originalPath = fileUtils.getPath(uri)
                val originalFile = File(originalPath!!)
                binding.uploadBtn.text = originalFile.name
                utils.shortToast(originalFile.name + "  files was uploaded successfully")
                println("--->>> Path $uri")
                println("--->>> File Path ${file.path}")
                println("--->>> File Name ${file.name}")
                println("--->>> Original File Name ${originalPath}")
            }
        }

    override fun onApproveOrderCanceled() {
        println("--->>>>> onApproveOrderCanceled :::: ")
    }

    override fun onApproveOrderFailure(error: PayPalSDKError) {
        println("--->>>>> onApproveOrderFailure :::: ${error.message}")
    }

    override fun onApproveOrderSuccess(result: CardResult) {
        println("--->>>>> onApproveOrderSuccess :::: ")
        println("--->>>>> onApproveOrderSuccess ORDER ID :::: ${result.orderId}")
    }

    override fun onApproveOrderThreeDSecureDidFinish() {
        println("--->>>>> onApproveOrderThreeDSecureDidFinish :::: ")
    }

    override fun onApproveOrderThreeDSecureWillLaunch() {
        println("--->>>>> onApproveOrderThreeDSecureWillLaunch :::: ")
    }
}