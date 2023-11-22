package com.dev.pari.gcp.activity

import android.os.Bundle
import android.util.Base64
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.dev.pari.gcp.databinding.ActivityPhonepeBinding
import com.phonepe.intent.sdk.api.B2BPGRequest
import com.phonepe.intent.sdk.api.B2BPGRequestBuilder
import com.phonepe.intent.sdk.api.PhonePe
import org.json.JSONObject
import java.nio.charset.Charset
import java.security.MessageDigest


class PhonePeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPhonepeBinding
    private val merchantId = "PGTESTPAYUAT" // test
    private val saltKey = "099eb0cd-02cf-4e2a-8aca-3e6c6aff0399" // test
    private val paymentEndPoint = "/pg/v1/pay"
    private var transactionRequest: B2BPGRequest? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhonepeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialization()
    }

    private fun initialization() {
        PhonePe.init(this)
        makePayment()
        binding.payBtn.setOnClickListener {
            if (transactionRequest != null)
                try {
                    paymentLauncher.launch(
                        PhonePe.getImplicitIntent(
                            this@PhonePeActivity,
                            transactionRequest!!, ""
                        )
                    )
                } catch (e: Exception) {
                    println("-->>>> PhonePay Exception ::: ${e.printStackTrace()}")

                }
        }
    }

    private fun makePayment() {
        val data = JSONObject()
        data.put("merchantTransactionId", System.currentTimeMillis().toString())//String. Mandatory
        data.put("merchantUserId", System.currentTimeMillis().toString()) //String. Mandatory
        data.put("merchantId", merchantId) //String. Mandatory
        data.put("amount", 200*100)//Long. Mandatory ( Here amount was convert pisa value )
        data.put("mobileNumber", "7908834635") //String. Optional
        data.put("callbackUrl", "https://webhook.site/2e2fd2ca-0a04-4eed-8b62-43312cb00798") //String. Mandatory

        val paymentInstrument = JSONObject()
        paymentInstrument.put("type", "PAY_PAGE")
        paymentInstrument.put("targetApp", "com.phonepe")
        data.put("paymentInstrument", paymentInstrument)//OBJECT. Mandatory

        val payloadBase64 = Base64.encodeToString(
            data.toString().toByteArray(Charset.defaultCharset()), Base64.NO_WRAP
        )

        val checksum = sha256(payloadBase64 + paymentEndPoint + saltKey)

        transactionRequest = B2BPGRequestBuilder()
            .setData(payloadBase64)
            .setChecksum(checksum)
            .setUrl(paymentEndPoint)
            .build()
    }

    private fun sha256(input: String): String {
        val bytes = input.toByteArray(Charsets.UTF_8)
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        val value = digest.fold("") { str, it -> str + "%02x".format(it) }
        return "$value###1"
    }

    private val paymentLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK)
                println("-->>>> Success")
            else
                println("-->>>> Failure")
        }
}