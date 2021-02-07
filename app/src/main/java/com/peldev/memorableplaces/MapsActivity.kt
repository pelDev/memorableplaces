package com.peldev.memorableplaces

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.text.SimpleDateFormat
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var locationListener: LocationListener
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        fusedLocationClient = LocationServices
            .getFusedLocationProviderClient(this)

        startListening()
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    @SuppressLint("ObsoleteSdkInt", "SimpleDateFormat")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap.also {
            it.setOnMapLongClickListener { latLng ->
                val geoCoder = Geocoder(applicationContext, Locale.getDefault())
                val listAddress = geoCoder.getFromLocation(latLng.latitude,
                    latLng.latitude, 1)
                var address = ""
                try {
                    if (!listAddress.isNullOrEmpty()) {
                        if (!listAddress[0].thoroughfare.isNullOrEmpty()) {
                            if (!listAddress[0].subThoroughfare.isNullOrEmpty()) {
                                address += listAddress[0].subThoroughfare + " "
                            }
                            address += listAddress[0].thoroughfare
                        }
                    }
                } catch (e: Throwable) {

                }

                if (address.isEmpty()) {
                    val sdf = SimpleDateFormat("HH:mm yyyy-MM-dd")
                    address = sdf.format(Date())
                }
                mMap.addMarker(MarkerOptions().position(latLng).title(address))
                MainActivity.places.add(address)
                MainActivity.locations.add(latLng)
                MainActivity.adapter.notifyDataSetChanged()
                Toast.makeText(applicationContext, "Location Saved", Toast.LENGTH_SHORT).show()
            }
        }
        if (intent.getLongExtra("placeNumber", -1) == 0L) {
            if (Build.VERSION.SDK_INT < 23) {
                startLocationUpdates()
            } else {
                if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    startLocationUpdates()

                    showLastKnownLocation()
                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        1
                    )
                }
            }
        }
        else {
            val location = Location(LocationManager.GPS_PROVIDER)
            location.latitude = MainActivity.locations[intent.getLongExtra("placeNumber", -1).toInt()].latitude
            location.longitude = MainActivity.locations[intent.getLongExtra("placeNumber", -1).toInt()].longitude
            mMap.clear()
            mMap.addMarker(MarkerOptions()
                .position(MainActivity.locations[intent.getLongExtra("placeNumber", -1).toInt()])
                .title(MainActivity.places[intent.getLongExtra("placeNumber", -1).toInt()]))
            centerMapOnLocation(location, MainActivity.places[intent.getLongExtra("placeNumber", -1).toInt()])
        }
    }

    override fun onPause() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        super.onPause()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates()
            showLastKnownLocation()
        }
    }

    private fun showLastKnownLocation() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null)
                        centerMapOnLocation(location, "Your Location")
                }
    }


    private fun startListening() {
        fusedLocationClient = LocationServices
            .getFusedLocationProviderClient(this)
        locationRequest = LocationRequest()
        locationRequest.interval = 1000
        locationRequest.fastestInterval = 1000
        locationRequest.smallestDisplacement = 0f
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return

                if (locationResult.locations.isNotEmpty()) {
                    val location: Location = locationResult.lastLocation
                    centerMapOnLocation(location, "Your Location")
                }
            }
        }

    }

    private fun centerMapOnLocation(location: Location, title: String) {
        val latLon = LatLng(location.latitude, location.longitude)
//        mMap.clear()
        if (title != "Your Location")
            mMap.addMarker(MarkerOptions().position(latLon).title(title))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLon, 12f))
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED)
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )
    }

}