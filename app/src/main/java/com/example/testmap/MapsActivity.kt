package com.example.testmap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.testmap.databinding.ActivityMapsBinding
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import android.location.Location
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.Marker
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationUpdatesStarted = false
    private companion object {
        const val MIN_DISTANCE_METERS = 50.0  // 50 meters minimum
        const val MAX_DISTANCE_METERS = 200.0 // 200 meters maximum
        const val METERS_PER_DEGREE = 111320.0 // meters in one degree

        const val MISSION_COMPLETE_DISTANCE = 30.0 // 30 meters
    }
    data class Mission(
        val id: String,
        val lat: Double, // latitude, szerokosc
        val lng: Double, // longitude, dlugosc
        var completed: Boolean = false,
        var circle: Circle? = null
    )

    val missions = mutableListOf<Mission>()
    private var missionGenerated = false
    //private val missionMarkers = mutableListOf<String, Marker>()

    private fun openStreetView(mission: Mission) {
        val intent = Intent(this, StreetViewActivity::class.java).apply {
            putExtra("MISSION_LAT", mission.lat)
            putExtra("MISSION_LNG", mission.lng)
        }
        startActivity(intent)
    }
    private fun checkMissionCompletion(userLat: Double, userLng: Double) {
        missions.forEach { mission ->
            if (!mission.completed) {
                val distance = calculateDistance(userLat, userLng, mission.lat, mission.lng)
                if (distance <= MISSION_COMPLETE_DISTANCE) {
                    completeMission(mission.id)
                    Toast.makeText(this, "Mission ${mission.id} completed! Distance: ${"%.1f".format(distance)}m", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun calculateDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lng1, lat2, lng2, results)
        return results[0].toDouble()
    }

    private fun showMissionZones(missions: List<Mission>) {
        missions.forEach { mission ->
            val circle = mMap.addCircle(
                CircleOptions()
                    .center(LatLng(mission.lat, mission.lng))
                    .radius(MISSION_COMPLETE_DISTANCE)
                    .strokeColor(Color.parseColor("#FF0000"))
                    .fillColor(Color.parseColor("#22FF0000"))
                    .strokeWidth(2f)
            )
            mission.circle = circle
        }
    }
    private fun completeMission(missionId: String) {
        val mission = missions.find { it.id == missionId }
        mission?.let {
            it.completed = true
            it.circle?.apply {
                strokeColor = Color.parseColor("#00FF00")
                fillColor = Color.parseColor("#2200FF00")
            }
            Toast.makeText(this, "Mission ${mission.id} completed!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateMissionMarker(mission: Mission) {
        Toast.makeText(this, "Mission ${mission.id} completed!", Toast.LENGTH_SHORT).show()
    }
    private fun generateMissionsAroundUser(userLat: Double, userLng: Double) {
        if (missionGenerated) return



        repeat(3) {
            index -> val mission = generateRandomMissions(userLat, userLng, index + 1)
            missions.add(mission)
        }
        showMissionsOnMap(missions)
        showMissionZones(missions)
        missionGenerated = true

        startLocationUpdates()
    }

    private fun generateRandomMissions(userLat: Double, userLng: Double, missionId: Int): Mission {
        val distance = MIN_DISTANCE_METERS + Math.random() * (MAX_DISTANCE_METERS - MIN_DISTANCE_METERS)

        val angle = Math.random() * 2 * Math.PI

        val latOffset = (distance * Math.cos(angle)) / METERS_PER_DEGREE
        val lngOffset = (distance * Math.sin(angle)) / (METERS_PER_DEGREE * Math.cos(Math.toRadians(userLat)))

        val missionLat = userLat + latOffset
        val missionLng = userLng + lngOffset

        return Mission(missionId.toString(), missionLat, missionLng)
    }

    private fun showMissionsOnMap(missions: List<Mission>) {
        missions.forEach { mission ->
            val marker = mMap.addMarker(
                MarkerOptions()
                    .position(LatLng(mission.lat, mission.lng)).title("Mission ${mission.id}")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            )?.apply {
                tag = mission.id
            }
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


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mMap.setOnMarkerClickListener { marker ->
            val missionId = marker.tag as? String
            if (missionId != null) {
                Log.d("Street", "Mission id not null")
                val mission = missions.find { it.id == missionId }
                mission?.let {
                    openStreetView(it)
                }
                true
            } else {
                Log.d("Street", "Mission id is null")
                Toast.makeText(this, "Clicked: ${marker.title}", Toast.LENGTH_SHORT).show()
                true
            }
        }

        enableMyLocation()
    }

    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            return
        }


        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val userLat = location.latitude
                    val userLng = location.longitude

                    checkMissionCompletion(userLat, userLng)

                    generateMissionsAroundUser(userLat, userLng)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(userLat, userLng), 16f))
                } else {
                    if (!missionGenerated) {
                        generateMissionsAroundUser(50.0647, 19.9450)
                    }
                }
            }
            .addOnFailureListener { e ->
                if (!missionGenerated) {
                    generateMissionsAroundUser(50.0647, 19.9450)
                }
            }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (locationUpdatesStarted) return

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 5000 // 5 секунд
        ).build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult.lastLocation?.let { location ->
                        val userLat = location.latitude
                        val userLng = location.longitude
                        Log.d("LOCATION_UPDATE", "New location: $userLat, $userLng")

                        checkMissionCompletion(userLat, userLng)
                    }
                }
            },
            Looper.getMainLooper()
        )
        locationUpdatesStarted = true
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
            getLastKnownLocation()
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


