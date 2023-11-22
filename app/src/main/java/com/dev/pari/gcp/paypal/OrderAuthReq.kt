package com.dev.pari.gcp.paypal

import com.google.gson.annotations.SerializedName

data class OrderAuthReq(

	@field:SerializedName("api_type")
	val apiType: String? = null
)
