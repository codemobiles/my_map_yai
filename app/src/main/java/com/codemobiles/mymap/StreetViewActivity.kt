package com.codemobiles.mymap

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

import com.google.android.gms.maps.SupportStreetViewPanoramaFragment
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_street_view.*

class StreetViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_street_view)

        val _streetViewFragment = street_view as SupportStreetViewPanoramaFragment
        _streetViewFragment.getStreetViewPanoramaAsync({ streetViewPanorama ->
            val _intent = intent
            val _lat = _intent.getDoubleExtra("lat", 0.0)
            val _lng = _intent.getDoubleExtra("lng", 0.0)
            streetViewPanorama.setPosition(LatLng(_lat, _lng))
        })

        // Setup back button
        val _toolbar = street_view_toolbar as Toolbar
        setSupportActionBar(_toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        _toolbar.setNavigationOnClickListener {
            finish()
        }
    }
}
