package com.dev.pari.gcp.paypal

import com.google.gson.annotations.SerializedName

data class CreateOrderReq(

	@field:SerializedName("purchase_units")
	val purchaseUnits: List<PurchaseUnitsItem?>? = null,

	@field:SerializedName("intent")
	val intent: String? = null
)

data class PurchaseUnitsItem(

	@field:SerializedName("amount")
	val amount: Amount? = null
)

data class Amount(

	@field:SerializedName("value")
	val value: String? = null,

	@field:SerializedName("currency_code")
	val currencyCode: String? = null
)
