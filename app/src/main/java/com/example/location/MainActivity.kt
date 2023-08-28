package com.example.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.location.databinding.ActivityMainBinding
import com.google.android.gms.common.GooglePlayServicesUtil.isGooglePlayServicesAvailable
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {



    private val viewModel:MainViewModel by viewModels()
    lateinit var binding:ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val locationRequest = LocationRequest.Builder(1000)
        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
        .build()

    private lateinit var locationCallback: LocationCallback
    private var requestingLocationUpdates = false

    private val locationPermissionRequest = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    // Precise location access granted.
                    Toast.makeText(this, "ACCESS_FINE_LOCATION granted", Toast.LENGTH_LONG).show()
                }

                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    // Only approximate location access granted.
                    Toast.makeText(this, "ACCESS_COARSE_LOCATION granted", Toast.LENGTH_LONG).show()
                }

                else -> {
                    // No location access granted.
                    Toast.makeText(this, "No location access granted.", Toast.LENGTH_LONG).show()
                }
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        initListeners()
        observeGetLocation()

    }

    private fun initListeners() {


        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations){
                    val tv = TextView(this@MainActivity)
                    tv. text = "${location.longitude}, ${location.latitude}"
                    binding.llLocations.addView(tv)
                    println("location updates-----> $location")
                    // Update UI with location data
                    // ...
                }
            }
        }

        binding.btnStartGetLocation.setOnClickListener {
        //    viewModel.runLocation(true)
            requestingLocationUpdates = true
            startLocationUpdates()
        }
        binding.btnStopGetLocation.setOnClickListener {
            requestingLocationUpdates = false
          //  viewModel.runLocation(false)
        }


        binding.btnCheck.setOnClickListener {
            println("google play service----> ${isGooglePlayServicesAvailable(this)}")
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ) {
                locationPermissionRequest.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            }
            else{
                checkLocationIsOpenOrShowDialog(fusedLocationClient)
            }

        }
    }

    @SuppressLint("MissingPermission")
    private fun observeGetLocation() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED){
                viewModel.getLocation.collect{
                    while (it){
                        delay(1000)
                        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener {
                            println("current location ---> $it")
                        }
                    }

                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (requestingLocationUpdates) startLocationUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        println("onDestroy")
        stopLocationUpdates()
    }


    override fun onStop() {
        super.onStop()
        if (requestingLocationUpdates) startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        //stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    @SuppressLint("MissingPermission")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == RESULT_OK){
            println("location settings is enabled")
            fusedLocationClient.lastLocation.addOnSuccessListener {
                println("last location ---> $it")
            }

            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).addOnCompleteListener {
                println("current location ---> ${it.result}")
            }
        }
    }

    private fun checkLocationIsOpenOrShowDialog(fusedLocationClient: FusedLocationProviderClient) {

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {



        }.addOnFailureListener {exception->
            if (exception is ResolvableApiException){
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(this@MainActivity, 1)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }



    }
}