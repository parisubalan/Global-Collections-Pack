package com.dev.pari.gcp.service_utils.location

import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.LocationSettingsResponse

interface GprsPermissionCallBack {
    fun onPermissionResult(response: LocationSettingsResponse?, exception: ApiException?)
}