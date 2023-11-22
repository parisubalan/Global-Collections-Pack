package com.dev.pari.gcp.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dev.pari.gcp.paypal.Amount
import com.dev.pari.gcp.paypal.ApiInterface
import com.dev.pari.gcp.paypal.CreateOrderReq
import com.dev.pari.gcp.paypal.CreateOrderResponse
import com.dev.pari.gcp.paypal.OrderAuthReq
import com.dev.pari.gcp.paypal.PurchaseUnitsItem
import com.dev.pari.gcp.databinding.ActivityCardPaymentBinding
import com.dev.pari.gcp.preference.MySharedPreference
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.paypal.android.cardpayments.ApproveOrderListener
import com.paypal.android.cardpayments.Card
import com.paypal.android.cardpayments.CardClient
import com.paypal.android.cardpayments.CardRequest
import com.paypal.android.cardpayments.CardResult
import com.paypal.android.cardpayments.threedsecure.SCA
import com.paypal.android.corepayments.Address
import com.paypal.android.corepayments.CoreConfig
import com.paypal.android.corepayments.Environment
import com.paypal.android.corepayments.PayPalSDKError
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class CardPaymentActivity : AppCompatActivity(), ApproveOrderListener {

    private lateinit var binding: ActivityCardPaymentBinding
    private var retrofit: Retrofit? = null
    private var apiInterface: ApiInterface? = null
    private var coreConfig: CoreConfig? = null
    private var cardClient: CardClient? = null
    private var testCard: Card? = null
    private var CLIENT_ID =
//        "AY_DOgZWhCGnvCTMcv0VhdgdP3q1Z6kAFKqCmnvCnXp7Q1orCAFBE7XdQtQQfs-Lc6hD03fNSJFsk7pj"
            "Ab8DLNriyiYlraUUMsz_ih6JOKiuOp0H3nArXgiR8pDd0aa44RWUKILVOpRDj03zPwgGjvyy5FaAn854"
    private var createOrderResponse: CreateOrderResponse? = null
    private var accessToken: String? = null
    private var orderId: String? = ""
    private var mySession: MySharedPreference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialization()
    }

    private fun initialization() {
        mySession = MySharedPreference.getInstance(this)
        coreConfig = CoreConfig(CLIENT_ID, environment = Environment.SANDBOX)
        cardClient = CardClient(this, coreConfig!!)
        cardClient?.approveOrderListener = this

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

        binding.createOrderBtn.setOnClickListener {
            getAccessToken(false, "")
        }

        binding.paypalBtn.setOnClickListener {
            if (orderId != null && !orderId.isNullOrEmpty()) {
                testCard = Card(
                    number = "4111111111111111",
                    expirationMonth = "01",
                    expirationYear = "2025",
                    securityCode = "123",
                    billingAddress = Address(
                        streetAddress = "123 Main St.",
                        extendedAddress = "Apt. 1A",
                        locality = "city",
                        region = "IL",
                        postalCode = "12345",
                        countryCode = "US"
                    )
                )
                makeCardRequest(orderId!!, testCard!!)
            } else
                Toast.makeText(this, "Please create a order", Toast.LENGTH_SHORT).show()
        }

    }

    private fun getAccessToken(isPaymentSuccess: Boolean, orderId: String) {
        val getClientIdCall = apiInterface?.getAccessToken()

        getClientIdCall?.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200) {
                    val responseBody = JSONObject(response.body()?.string()!!)
                    accessToken = "Bearer " + responseBody.getString("access_token")
                    mySession?.accessToken = accessToken
                    println("---->>>> Access Token ::: ${mySession?.accessToken}")
                    if (!isPaymentSuccess)
                        createOrder(accessToken!!)
                    else
                        createAuthorizeOrCapture(
                            accessToken!!,
                            orderId,
                            OrderAuthReq(apiType = "capture")
                        )
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

        })
    }

    private fun createOrder(accessToken: String) {
//       use ::: intent = CAPTURE or AUTHORIZE
        val orderReq = CreateOrderReq(
            intent = "CAPTURE", purchaseUnits = listOf(
                PurchaseUnitsItem(Amount(value = "113.00", currencyCode = "USD"))
            )
        )
        val getClientIdCall = apiInterface?.createOrder(accessToken, orderReq)

        getClientIdCall?.enqueue(object : Callback<ResponseBody> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200) {
                    val responseBody = JSONObject(response.body()?.string()!!)
                    createOrderResponse =
                        Gson().fromJson(responseBody.toString(), CreateOrderResponse::class.java)
                    orderId = createOrderResponse?.id
                    binding.tvOrderId.text = "Order ID : ${createOrderResponse?.id}"
                    println("---->>>> Create Order Response ::: $responseBody")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

        })
    }

    private fun makeCardRequest(orderId: String, cardDetail: Card) {
        val cardRequest = CardRequest(
            orderId = orderId,
            card = cardDetail,
            returnUrl = "myapp://return_url/orderId?$orderId/access?${
                accessToken?.replace(
                    " ",
                    ""
                )
            }", // custom url scheme needs to be configured in AndroidManifest.xml (see below)
            sca = SCA.SCA_ALWAYS // default value is SCA_WHEN_REQUIRED
        )
        cardClient?.approveOrder(this, cardRequest)
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
                    Toast.makeText(this@CardPaymentActivity, "Success", Toast.LENGTH_SHORT).show()
                    Toast.makeText(this@CardPaymentActivity, "Response :: ${response.body()?.string()}", Toast.LENGTH_SHORT).show()
                    println("--->>>>> Integration Completed :::::")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

            }

        })
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
//        getAccessToken(true, result.orderId)
        createAuthorizeOrCapture(
            mySession?.accessToken ?: "",
            result.orderId,
            OrderAuthReq(apiType = "capture")
        )
    }

    override fun onApproveOrderThreeDSecureDidFinish() {
        println("--->>>>> onApproveOrderThreeDSecureDidFinish :::: ")
    }

    override fun onApproveOrderThreeDSecureWillLaunch() {
        println("--->>>>> onApproveOrderThreeDSecureWillLaunch :::: ")
    }
}