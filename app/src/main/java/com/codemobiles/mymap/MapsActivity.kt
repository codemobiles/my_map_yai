package com.codemobiles.mymap

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.location.places.AutocompleteFilter
import com.google.android.gms.location.places.ui.PlaceAutocomplete
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.custom_info.view.*
import java.text.DecimalFormat


class MapsActivity : AppCompatActivity() {

    private val PLACE_AUTOCOMPLETE_REQUEST_CODE: Int = 1150

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val result = PlaceAutocomplete.getPlace(this, data)

                val markerOptions = MarkerOptions()
                markerOptions.position(result.latLng)
                markerOptions.title(result.name.toString())
                markerOptions.snippet("${result.address} \n ${result.phoneNumber}")
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_01))

                mMap.addMarker(markerOptions)

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(result.latLng, 18F))

            } else {
                val status = PlaceAutocomplete.getStatus(this, data)
                showToast(status.toString())
            }
        }
    }

    private fun initMap() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mMapFragment) as SupportMapFragment
        mapFragment.getMapAsync { googleMap ->
            mMap = googleMap

            setupMap()
            checkRuntimePermission()
            bindEventWidget()
        }
    }

    private fun bindEventWidget() {
        mMapSearchBtn.setOnClickListener {
            try {
                val typeFilter = AutocompleteFilter
                    .Builder()
                    .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ESTABLISHMENT)
                    .setCountry("TH")
                    .build()

                val intent = PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                    .setFilter(typeFilter)
                    .build(this@MapsActivity)

                startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE)
            } catch (e: Exception) {
                Log.e("CM_Google_Search", "Exception: ${e.message}")
            }
        }

        mMapClearBtn.setOnClickListener {
            mMap.clear()
        }
    }

    private fun checkRuntimePermission() {
        Dexter.withActivity(this)
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {
                    Toast.makeText(applicationContext, "permission granted", Toast.LENGTH_LONG)
                        .show()
                    //tracking()
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

                                val latLng = LatLng(
                                    mCurrentLocation!!.latitude,
                                    mCurrentLocation!!.longitude
                                )
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

        //--------------------------
        mMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoContents(marker: Marker?): View? {
                return null
            }

            override fun getInfoWindow(marker: Marker): View {
                val view = layoutInflater.inflate(R.layout.custom_info, null)

                if (marker.title != null && marker.title != "") {
                    view.info_window_title.text = marker.title
                    view.info_window_title.visibility = View.VISIBLE // VISIBLE, INVISIBLE, GONE
                } else {
                    view.info_window_title.visibility = View.GONE
                }

                val formatter = DecimalFormat("#,###.00")
                val lat = "${formatter.format(marker.position.latitude)}°"
                val lng = "${formatter.format(marker.position.longitude)}°"

                view.info_window_position.text = "$lat, $lng"

                return view
            }
        })

        mMap.setOnInfoWindowClickListener {
            marker ->
            val startString = "${mCurrentLocation!!.latitude},${mCurrentLocation!!.longitude}"
            val destString = "${marker.position.latitude},${marker.position.longitude}"

            val url = "http://maps.google.com/maps?saddr=$startString&daddr=$destString"

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }
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
