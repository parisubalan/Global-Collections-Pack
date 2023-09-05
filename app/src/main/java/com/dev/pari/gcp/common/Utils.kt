package com.dev.pari.gcp.common

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.text.format.DateFormat
import android.util.Base64
import android.util.Log
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import com.dev.pari.gcp.R
import com.dev.pari.gcp.utils.interfaces.AlertListener
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Locale

class Utils(private val context: Context) {
    var progressBar: Dialog

    init {
        progressBar = customProgressDialog()
    }

    private fun customProgressDialog(): Dialog {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.progress_bar_dialog)
        if (dialog.window != null) {
            dialog.window!!.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    fun shortToast(message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun moveNextActivity(context: Context, intent: Intent?, needToFinish: Boolean) {
        context.startActivity(intent)
        if (needToFinish) {
            (context as Activity).finish()
        }
    }

    val isNetworkAvailable: Boolean
        get() {
            val isConnect: Boolean
            val manager: ConnectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            isConnect = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
                ?.state == NetworkInfo.State.CONNECTED || manager.getNetworkInfo(
                ConnectivityManager.TYPE_WIFI
            )?.state == NetworkInfo.State.CONNECTED
            return isConnect
        }

    fun restartActivity(activity: Activity) {
        if (Build.VERSION.SDK_INT >= 22) {
            activity.startActivity(activity.intent)
            activity.finish()
            activity.overridePendingTransition(0, 0)
        } else {
            activity.finish()
            activity.startActivity(activity.intent)
        }
    }

    fun getCurrentTime(): String {
        return DateFormat.format("HH:mm:ss", System.currentTimeMillis()).toString()
    }

    fun getCurrentDate(): String? {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(System.currentTimeMillis())
    }

    fun getTimeStamp(): String? {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(System.currentTimeMillis())
    }

    fun decimalFormat(value: Float): String? {
        val decimalFormat = DecimalFormat("0.00")
        return decimalFormat.format(value.toDouble())
    }

    fun dateFormat(value: String): String? {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(value)
    }

    fun showProgressBar() {
        progressBar.show()
    }

    fun dismissProgressBar() {
        if (progressBar.isShowing)
            progressBar.dismiss()
    }

    fun alertYesOrNoType(
        context: Context,
        title: String?,
        message: String?,
        listener: AlertListener
    ) {
        val alert = AlertDialog.Builder(context)
        alert.setTitle(title)
        alert.setMessage(message)
        alert.setPositiveButton(
            context.resources.getText(R.string.lbl_yes)
        ) { dialogInterface: DialogInterface, i: Int ->
            dialogInterface.dismiss()
            listener.actionYes()
        }
        alert.setNegativeButton(
            context.resources.getString(R.string.lbl_no)
        ) { dialogInterface: DialogInterface, i: Int ->
            dialogInterface.dismiss()
            listener.actionNo()
        }
        val dialog: Dialog = alert.create()
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    fun alertOkType(
        context: Context,
        title: String?,
        message: String?,
        listener: AlertListener
    ) {
        val alert = AlertDialog.Builder(context)
        alert.setTitle(title)
        alert.setMessage(message)
        alert.setPositiveButton(
            context.resources.getText(R.string.lbl_ok)
        ) { dialogInterface: DialogInterface, i: Int ->
            dialogInterface.dismiss()
            listener.actionYes()
        }
        val dialog: Dialog = alert.create()
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    fun printHashKey(pContext: Context) {
        try {
            val info = pContext.packageManager.getPackageInfo(
                pContext.packageName, PackageManager.GET_SIGNATURES
            )
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hashKey = String(Base64.encode(md.digest(), 0))
                println("---->>>>> printHashKey() Hash Key: $hashKey")
                Log.i(ContentValues.TAG, "printHashKey() Hash Key: $hashKey")
            }
        } catch (e: NoSuchAlgorithmException) {
            Log.e(ContentValues.TAG, "printHashKey()", e)
        } catch (e: Exception) {
            Log.e(ContentValues.TAG, "printHashKey()", e)
        }
    }

}