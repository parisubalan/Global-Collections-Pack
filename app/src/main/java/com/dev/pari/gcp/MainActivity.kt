package com.dev.pari.gcp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dev.pari.gcp.databinding.ActivityMainBinding
import com.dev.pari.gcp.location.LocationUtils

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var lat: Double = 0.0
    private var long: Double = 0.0
    private var address : String = ""
    private lateinit var locationUtils : LocationUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialization()
    }

    private fun initialization() {
        locationUtils = LocationUtils(this,this)
        locationUtils.locationInitialize()
        binding.getLatLongBtn.setOnClickListener {
            locationUtils.getLatAndLong()
            locationUtils.locationState.observe(this) {
                if (it != null) {
                    lat = it.latitude
                    long = it.longitude
                    binding.tvLatLong.text =
                        "Latitude : " + it.latitude + "  Longitude : " + it.longitude
                    println("-->>> Latitude : ${it.latitude}  Longitude : ${it.longitude}")
                }
            }
        }
        binding.getAddressBtn.setOnClickListener {
            if (lat != 0.0 && long != 0.0) {
                locationUtils.getAddressFromLatLong(lat, long).observe(this) {
                    address = it
                    binding.tvAddress.text = "Address : $it"
                }
            }
        }
        binding.getLatLangFromAddressBtn.setOnClickListener {
            if (address.isNotEmpty()) {
                locationUtils.getLatLongFromAddress(address).observe(this) {
                    binding.tvNewLatLang.text = "Lat Lang From Address :  Lat : ${it.latitude}   Longitude : ${it.longitude}"
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationUtils.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        locationUtils.onActivityResult(requestCode, resultCode, data)
    }

    override fun onStop() {
        super.onStop()
        locationUtils.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        locationUtils.onDestroy()
    }
}