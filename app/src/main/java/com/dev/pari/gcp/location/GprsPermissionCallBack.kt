package com.dev.pari.gcp.location

import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.LocationSettingsResponse
import java.lang.Exception

interface GprsPermissionCallBack {
    fun onPermissionResult(response: LocationSettingsResponse?, exception: ApiException?)
}