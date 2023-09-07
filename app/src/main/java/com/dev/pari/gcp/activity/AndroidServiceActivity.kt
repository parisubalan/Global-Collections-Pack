package com.dev.pari.gcp.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.dev.pari.gcp.R
import com.dev.pari.gcp.common.Utils
import com.dev.pari.gcp.databinding.ActivityAndroidServiceBinding
import com.dev.pari.gcp.service_utils.android_service.SimpleBackgroundService
import com.dev.pari.gcp.service_utils.android_service.SimpleForegroundService
import com.dev.pari.gcp.service_utils.interfaces.AlertListener
import com.dev.pari.gcp.service_utils.permissionutils.PermissionUtils

class AndroidServiceActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityAndroidServiceBinding
    private lateinit var utils: Utils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAndroidServiceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialization()
    }

    private fun initialization() {
        utils = Utils(this)
        binding.bgServiceStartBtn.setOnClickListener(this)
        binding.bgServiceStopBtn.setOnClickListener(this)
        binding.foregroundServiceStartBtn.setOnClickListener(this)
        binding.foregroundServiceStopBtn.setOnClickListener(this)
    }

    private fun startBackgroundService() {
        val intent = Intent(this, SimpleBackgroundService::class.java)
        intent.putExtra("close", false)
        startService(intent)
    }

    private fun stopBackgroundService() {
        val intent = Intent(this, SimpleBackgroundService::class.java)
        intent.putExtra("close", true)
        startService(intent)
    }

    private fun startForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!PermissionUtils.isNotificationPermissionEnable(this))
                if (PermissionUtils.isNotificationPermissionDenied(this))
                    utils.alertOkType(
                        this,
                        "Permission Alert!",
                        "Kindly enable notification permission",
                        object : AlertListener {
                            override fun actionYes() {
                                val intent = Intent()
                                intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                                intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName);
                                startActivity(intent)
                            }

                            override fun actionNo() {

                            }

                        })
                else
                    PermissionUtils.raiseNotificationPermission(this)
            else {
                val intent = Intent(this, SimpleForegroundService::class.java)
                intent.putExtra("close", false)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    startForegroundService(intent)
                else
                    startService(intent)
            }
        } else {
            val intent = Intent(this, SimpleForegroundService::class.java)
            intent.putExtra("close", false)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                startForegroundService(intent)
            else
                startService(intent)
        }
    }

    private fun stopForegroundService() {
        val intent = Intent(this, SimpleForegroundService::class.java)
        intent.putExtra("close", true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForegroundService(intent)
        else
            startService(intent)
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.bgServiceStartBtn -> {
                startBackgroundService()
            }

            R.id.bgServiceStopBtn -> {
                stopBackgroundService()
            }

            R.id.foregroundServiceStartBtn -> {
                startForegroundService()
            }

            R.id.foregroundServiceStopBtn -> {
                stopForegroundService()
            }

        }
    }
}