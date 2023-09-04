package com.dev.pari.gcp.permissionutils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dev.pari.gcp.common.Constants
import com.dev.pari.gcp.location.GprsPermissionCallBack
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.SettingsClient

object PermissionUtils {

    fun isGprsEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    fun raiseGprsPermission(
        gprsClient: SettingsClient,
        locationSettingRequest: LocationSettingsRequest,
        callBack: GprsPermissionCallBack
    ) {
        gprsClient.checkLocationSettings(locationSettingRequest).addOnSuccessListener {
            if (it.locationSettingsStates!!.isLocationPresent) {
                callBack.onPermissionResult(it,null)
            }
        }.addOnFailureListener {
            callBack.onPermissionResult(null,(it as ApiException))
        }
    }

    fun isLocationPermissionEnable(context: Context): Boolean {
        return (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)
    }

    fun raiseLocationPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            Constants.LOCATION_PERMISSION_CODE
        )
    }

    fun isLocationPermissionCancelled(activity: Activity) : Boolean{
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION) && ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    fun isCameraPermissionEnable(context: Context): Boolean{
       return ContextCompat.checkSelfPermission(context,Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    fun raiseCameraPermission(activity: Activity){
        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.CAMERA),Constants.CAMERA_PERMISSION_CODE)
    }

    // Android 12 or Below version read external storage permission check given below
    fun isReadStoragePermissionEnable(context : Context) :Boolean {
        return if (Build.VERSION.SDK_INT==33)
            (ContextCompat.checkSelfPermission(context,Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED)
        else
            (ContextCompat.checkSelfPermission(context,Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
    }
    fun isReadStorageStorageDenied(activity: Activity):Boolean{
        return ActivityCompat.shouldShowRequestPermissionRationale(activity,Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
    fun raiseReadStoragePermission(activity: Activity){
        if (Build.VERSION.SDK_INT ==33)
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                Constants.STORAGE_PERMISSION_CODE)
        else
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),Constants.STORAGE_PERMISSION_CODE)
    }

    fun isExternalStoragePermissionEnable(context: Context): Boolean{
        return ContextCompat.checkSelfPermission(context,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    fun raiseExternalStoragePermission(activity: Activity){
        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),Constants.EXTERNAL_STORAGE_PERMISSION_CODE)
    }

    fun isExternalStorageDenied(activity: Activity):Boolean{
        return ActivityCompat.shouldShowRequestPermissionRationale(activity,Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    // Android 13 or Above version write external storage and read external storage permission check given below
    fun isMediaPermissionEnable(context: Context): Boolean{
        return ContextCompat.checkSelfPermission(context,Manifest.permission.MEDIA_CONTENT_CONTROL) == PackageManager.PERMISSION_GRANTED
    }
    fun raiseMediaPermission(activity: Activity){
        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.MEDIA_CONTENT_CONTROL),Constants.EXTERNAL_STORAGE_PERMISSION_CODE)
    }
    fun isMediaPermissionDenied(activity: Activity):Boolean{
        return ActivityCompat.shouldShowRequestPermissionRationale(activity,Manifest.permission.MEDIA_CONTENT_CONTROL)
    }

    // Android 13 or Above version notification permission
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun isNotificationPermissionEnable(context: Context): Boolean{
        return ContextCompat.checkSelfPermission(context,Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun isNotificationPermissionDenied(activity: Activity):Boolean{
        return ActivityCompat.shouldShowRequestPermissionRationale(activity,Manifest.permission.POST_NOTIFICATIONS)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun raiseNotificationPermission(activity: Activity){
        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.POST_NOTIFICATIONS),Constants.NOTIFICATION_PERMISSION_CODE)
    }


}