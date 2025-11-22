package com.example.testmap

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback
import com.google.android.gms.maps.StreetViewPanorama
import com.google.android.gms.maps.StreetViewPanoramaFragment
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment
import com.google.android.gms.maps.model.LatLng

class StreetViewActivity : AppCompatActivity(), OnStreetViewPanoramaReadyCallback {

    private lateinit var streetViewPanorama: StreetViewPanorama
    private var missionLat: Double = 0.0
    private var missionLng: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_street_view)

        missionLat = intent.getDoubleExtra("MISSION_LAT", 0.0)
        missionLng = intent.getDoubleExtra("MISSION_LNG", 0.0)

        val streetViewFragment = supportFragmentManager
            .findFragmentById(R.id.streetViewPanorama) as SupportStreetViewPanoramaFragment
        streetViewFragment.getStreetViewPanoramaAsync(this)

        findViewById<Button>(R.id.btnBackToMap).setOnClickListener {
            finish()
        }
    }

    override fun onStreetViewPanoramaReady(panorama: StreetViewPanorama) {
        streetViewPanorama = panorama

        val missionLocation = LatLng(missionLat, missionLng)
        streetViewPanorama.setPosition(missionLocation)

        streetViewPanorama.setOnStreetViewPanoramaChangeListener { location ->
            if (location == null) {
                Toast.makeText(this, "No Street View available here", Toast.LENGTH_SHORT).show()
            }
        }
    }
}