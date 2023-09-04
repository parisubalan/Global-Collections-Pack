package com.dev.pari.gcp.location

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Looper
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.dev.pari.gcp.common.Constants
import com.dev.pari.gcp.common.Utils
import com.dev.pari.gcp.interfaces.AlertListener
import com.dev.pari.gcp.permissionutils.PermissionUtils
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.maps.model.LatLng
import java.util.Locale

class LocationUtils(private val mContext: Context, private val mActivity: Activity?) {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var gprsSettingClient: SettingsClient
    private lateinit var locationSettingRequest: LocationSettingsRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationReq: LocationRequest
    private lateinit var utils: Utils

    /*
        Listen this mutable live data in your activity or fragment
        once getting a location it will send a exact location details
     */
    lateinit var locationState: MutableLiveData<Location>

    fun locationInitialize() {
        utils = Utils(mContext)
        locationState = MutableLiveData<Location>()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext)
        gprsSettingClient = LocationServices.getSettingsClient(mContext)
        locationReq =
            LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 100).build()
        locationSettingRequest =
            LocationSettingsRequest.Builder().addLocationRequest(locationReq).setAlwaysShow(true)
                .build()
        locationCallBack()
    }

    fun getLatAndLong() {
        if (PermissionUtils.isLocationPermissionEnable(mContext)) {
            if (PermissionUtils.isGprsEnabled(mContext)) {
                getCurrentLocation()
            } else
                PermissionUtils.raiseGprsPermission(
                    gprsSettingClient,
                    locationSettingRequest,
                    object : GprsPermissionCallBack {
                        override fun onPermissionResult(
                            response: LocationSettingsResponse?,
                            exception: ApiException?
                        ) {
                            if (response != null) {
                                getCurrentLocation()
                            } else if (exception != null)
                                try {
                                    val statusCode = exception.statusCode
                                    if (statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                                        val rae = exception as ResolvableApiException
                                        rae.startResolutionForResult(
                                            mActivity!!,
                                            Constants.GPRS_PERMISSION_CODE
                                        )
                                    }
                                } catch (sie: IntentSender.SendIntentException) {
                                    sie.printStackTrace()
                                }
                        }
                    })
        } else {
            PermissionUtils.raiseLocationPermission(mContext as Activity)
        }
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == Constants.LOCATION_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else if (PermissionUtils.isLocationPermissionCancelled(mActivity!!))
                Toast.makeText(mContext, "Permission was Denied", Toast.LENGTH_SHORT).show()
            else {
                Toast.makeText(mContext, "Please Turn on location", Toast.LENGTH_SHORT).show()
                utils.alertOkType(mContext,"Alert!", "Must need enable location permission",object : AlertListener{
                    override fun actionYes() {

                    }

                    override fun actionNo() {

                    }

                })
            }
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Constants.GPRS_PERMISSION_CODE) {
            if (resultCode == RESULT_OK)
                getLatAndLong()
            else
                utils.alertOkType(mContext,"Gprs Alert", "Must enable gprs permission",object : AlertListener{
                    override fun actionYes() {

                    }

                    override fun actionNo() {

                    }

                })
        }
    }

    @SuppressLint("MissingPermission")
    fun getCurrentLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener {
            if (it != null) {
                locationState.postValue(it)
                fusedLocationClient.removeLocationUpdates(locationCallback)
            } else {
                fusedLocationClient.requestLocationUpdates(
                    locationReq, locationCallback,
                    Looper.getMainLooper()
                )
                getLatAndLong()
            }
        }
    }

    private fun locationCallBack() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    if (location != null) {
                        locationState.postValue(location)
                        fusedLocationClient.removeLocationUpdates(locationCallback)
                    }
                }
            }
        }
    }

    fun getAddressFromLatLong(latitude: Double, longitude: Double): LiveData<String> {
        val addressLine = MediatorLiveData<String>()
        val current = LatLng(latitude, longitude)
        val geocoder = Geocoder(mContext, Locale.getDefault())
        if (current.latitude != 0.0 && current.longitude != 0.0) {
            try {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(
                        current.latitude,
                        current.longitude,
                        1
                    ) { p0 ->
                        addressLine.postValue(p0[0].getAddressLine(0))
                        println("-->>> Address : " + p0[0].getAddressLine(0))
                    }
                } else {
                    val address =
                        geocoder.getFromLocation(current.latitude, current.longitude, 1)!![0]
                    addressLine.postValue(address.getAddressLine(0))
                    println("-->>> Address : " + address.getAddressLine(0))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return addressLine
    }

    fun getLatLongFromAddress(address: String): LiveData<LatLng> {
        val addressLine = MediatorLiveData<LatLng>()
        val geocoder = Geocoder(mContext, Locale.getDefault())
        if (address.isNotEmpty()) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocationName(
                        address,
                        1
                    ) { p0 ->
                        addressLine.postValue(LatLng(p0[0].latitude, p0[0].longitude))
                        println("-->>> Lat Lat From Address : Lat : ${p0[0].latitude}   Long: ${p0[0].longitude}")
                    }
                } else {
                    val latLng =
                        geocoder.getFromLocationName(address, 1)!![0]
                    addressLine.postValue(LatLng(latLng.latitude, latLng.longitude))
                    println("-->>> Lat Lat From Address : Lat : ${latLng.latitude}   Long: ${latLng.longitude}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return addressLine
    }

    fun getStraightLineDistance(
        startLat: Double,
        startLong: Double,
        endLat: Double,
        endLong: Double
    ): Double {
        val startLocation = Location("startLocation")
        startLocation.latitude = startLat
        startLocation.longitude = startLong

        val endLocation = Location("endLocation")
        endLocation.latitude = endLat
        endLocation.longitude = endLong
        return (startLocation.distanceTo(endLocation) / 1000).toDouble()
    }

    fun onStop() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    fun onDestroy() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

}