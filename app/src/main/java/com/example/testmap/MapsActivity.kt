package com.example.testmap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.testmap.databinding.ActivityMapsBinding
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    data class Mission(
        val id: String,
        val lat: Double,
        val lng: Double,
        var completed: Boolean = false
    )

    private val missions = mutableListOf<Mission>()

    //private val missionMarkers = mutableListOf<String, Marker>()

    private fun showMissionsOnMap() {
        val missions = listOf(
            Mission("1", 50.0652, 19.9452),
            Mission("2", 50.0644, 19.9454)
        )

        missions.forEach { mission -> mMap.addMarker(
            MarkerOptions().position(LatLng(mission.lat, mission.lng))
                .title("Mission ${mission.id}").icon(BitmapDescriptorFactory.defaultMarker(
                    BitmapDescriptorFactory.HUE_RED)))
         }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap.uiSettings.isZoomControlsEnabled = true

        mMap.setOnMarkerClickListener { marker ->
            Toast.makeText(this, "Clicked: ${marker.title}", Toast.LENGTH_SHORT).show()
            true
        }

        enableMyLocation()

        val krakow = LatLng(50.0647, 19.9450)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(krakow, 18f))

        showMissionsOnMap()
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}


