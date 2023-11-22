package com.dev.pari.gcp.paypal

import com.google.gson.annotations.SerializedName

data class CreateOrderResponse(

	@field:SerializedName("id")
	val id: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)
