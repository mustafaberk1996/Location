package com.example.location

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.example.location.databinding.ActivityLocationBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class LocationActivity : AppCompatActivity() {



    private lateinit var binding:ActivityLocationBinding


    private val locationRequest = LocationRequest.Builder(1000)
        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
        .build()

    private lateinit var locationCallback: LocationCallback
    private var requestingLocationUpdates = false


    private lateinit var fusedLocationClient:FusedLocationProviderClient


    private val requestLocationPermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
        when {
            it.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)->{
                //fine location izni alindi
            }
            it.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)->{
                //course location izni alindi
            }
            else->{
                //location izni alinamadi
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        initListeners()
    }

    private fun initListeners() {
        binding.btnStartLocation.setOnClickListener {
            requestingLocationUpdates = true
            startLocation()
        }

        binding.btnStopLocation.setOnClickListener {
            stopLocation()
        }

        locationCallback = object :LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                locationResult.locations.forEach {
                    val tv = TextView(this@LocationActivity)
                    tv.text = "${it.latitude}, ${it.longitude}"
                    tv.textSize = 24f
                    binding.llLocations.addView(tv)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (requestingLocationUpdates) startLocation()
    }

    override fun onPause() {
        super.onPause()
        stopLocation()
    }

    private fun stopLocation() {
        requestingLocationUpdates = false
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun startLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //request permission
            requestLocationPermission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
        else{
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }

    }

}