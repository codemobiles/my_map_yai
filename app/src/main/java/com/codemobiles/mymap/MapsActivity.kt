package com.codemobiles.mymap

import android.Manifest
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.activity_maps.*
import java.text.DecimalFormat


class MapsActivity : AppCompatActivity() {

    private var mCurrentLocation: Location? = null
    private var mLocationProvider: FusedLocationProviderClient? = null
    private val FASTEST_INTERVAL: Long = 1000
    private val UPDATE_INTERVAL: Long = 2000
    private lateinit var mGoogleApi: GoogleApiClient
    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        mLocationProvider = FusedLocationProviderClient(this);

        initMap()
    }

    private fun initMap() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mMapFragment) as SupportMapFragment
        mapFragment.getMapAsync { googleMap ->
            mMap = googleMap

            setupMap()
            checkRuntimePermission()
        }
    }

    private fun checkRuntimePermission() {
        Dexter.withActivity(this)
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {
                    Toast.makeText(applicationContext, "permission granted", Toast.LENGTH_LONG).show()
                    tracking()
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    finish()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).check()
    }

    private fun tracking() {
        mGoogleApi = GoogleApiClient.Builder(this)
            .addApi(LocationServices.API)
            .addConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {
                override fun onConnected(bundle: Bundle?) {

                    val request = LocationRequest()
                    request.interval = UPDATE_INTERVAL
                    request.fastestInterval = FASTEST_INTERVAL
                    request.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

                    val callback = object : LocationCallback() {
                        override fun onLocationResult(resut: LocationResult?) {
                            super.onLocationResult(resut)

                            val currentLocation = resut!!.lastLocation

                            if (currentLocation != null) {
                                mCurrentLocation = currentLocation
//
                                updateLocationTextView()

                                val latLng = LatLng(mCurrentLocation!!.latitude, mCurrentLocation!!.longitude)
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
//
//                                if (mTrackBtn.tag == R.drawable.ic_action_stop) {
//                                    animateCamera(LatLng(currentLocation.latitude, currentLocation.longitude), 15)
//                                }
                            }
                        }
                    }
//
                    mLocationProvider!!.requestLocationUpdates(request, callback, Looper.myLooper())
                }

                override fun onConnectionSuspended(i: Int) {
                    //todo
                }
            })
            .addOnConnectionFailedListener {
                //todo
            }
            .build()

        mGoogleApi.connect() // important
    }

    private fun setupMap() {
        val _latLng = LatLng(13.7467237, 100.53198)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(_latLng, 12f))

        mMap.uiSettings.isCompassEnabled = false
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true

        mMap.mapType = GoogleMap.MAP_TYPE_HYBRID

        mMap.isTrafficEnabled = true
    }

    fun updateLocationTextView() {
        if (mCurrentLocation != null) {
            val formatter = DecimalFormat("#,###.00")
            val lat = formatter.format(mCurrentLocation!!.getLatitude())
            val lng = formatter.format(mCurrentLocation!!.getLongitude())

            val currentLocStr = "Lat: $lat°, Long: $lng°"
            mCurrentLocationTextView.text = currentLocStr
        }
    }

}
