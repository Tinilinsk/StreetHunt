package com.example.testmap

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    private lateinit var mapView: MapView

    private var currentButtonState = 0
    private val sharedPrefs by lazy {
        getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    }

    companion object {
        private const val PREFS_NAME = "player_data"
        private const val KEY_NICKNAME = "nickname"
        private const val KEY_EXPERIENCE = "experience"
        private const val DEFAULT_NICKNAME = "Player"
        private const val LEVEL_UP_THRESHOLD = 100
    }

    private val mapsActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d("MainActivity", "Result received: code=${result.resultCode}")

        if (result.resultCode == RESULT_OK) {
            val data = result.data
            if (data != null) {
                val missionCompleted = data.getBooleanExtra("mission_completed", false)
                val totalXpEarned = data.getIntExtra("total_xp_earned", 0)
                val missionsCount = data.getIntExtra("completed_missions_count", 0)

                Log.d("MainActivity", "Mission completed: $missionCompleted, XP: $totalXpEarned, Count: $missionsCount")

                if (missionCompleted && totalXpEarned > 0) {
                    addExperience(totalXpEarned)
                    showMissionCompleteMessage(missionsCount, totalXpEarned)
                } else {
                    Log.d("MainActivity", "No XP to add: missionCompleted=$missionCompleted, totalXpEarned=$totalXpEarned")
                }
            } else {
                Log.d("MainActivity", "Data is null")
                Toast.makeText(this, "No data received from maps", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.d("MainActivity", "Result not OK: ${result.resultCode}")
            Toast.makeText(this, "Maps activity cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)

        miniMap()

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()

        // Load player data when activity starts
        loadPlayerData()

        updateLocationText()

        findViewById<LinearLayout>(R.id.nav_map).setOnClickListener {
            Log.d("MainActivity", "Launching MapsActivity with result launcher")
            val intent = Intent(this, MapsActivity::class.java)
            mapsActivityResultLauncher.launch(intent)
        }

        findViewById<LinearLayout>(R.id.nav_profile).setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        currentButtonState = sharedPrefs.getInt("button_state", 0)
        updateButtonAppearance()

        findViewById<TextView>(R.id.radius).setOnClickListener {
            currentButtonState = (currentButtonState + 1) % 3
            updateButtonAppearance()

            sharedPrefs.edit().putInt("button_state", currentButtonState).apply()
        }

        // show level on start app
        val currentExp = sharedPreferences.getInt(KEY_EXPERIENCE, 0)
        val level = (currentExp / LEVEL_UP_THRESHOLD) + 1
        findViewById<TextView>(R.id.level_num).text = "$level"

        val progressBar = findViewById<ProgressBar>(R.id.experienceProgressBar)
        val currentLevelExp = currentExp % LEVEL_UP_THRESHOLD

        progressBar.max = LEVEL_UP_THRESHOLD
        progressBar.progress = currentLevelExp
    }

    private fun updateButtonAppearance() {
        val button = findViewById<TextView>(R.id.radius)
        when (currentButtonState) {
            0 -> {
                button.text = "1km"
            }
            1 -> {
                button.text = "2km"
            }
            2 -> {
                button.text = "3km"
            }
        }
    }

    private fun miniMap() {
        mapView.getMapAsync { googleMap ->
            googleMap.uiSettings.isZoomControlsEnabled = true

            val position = LatLng(52.2297, 21.0122) // Warszawa
            googleMap.addMarker(MarkerOptions().position(position).title("Warszawa"))
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 16f))
        }
    }
    private fun showMissionCompleteMessage(missionsCount: Int, xpEarned: Int) {
        val message = if (missionsCount > 1) {
            "Completed $missionsCount missions! +$xpEarned XP"
        } else {
            "Mission completed! +$xpEarned XP"
        }

        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

        AlertDialog.Builder(this)
            .setTitle("🎉 Success!")
            .setMessage(message)
            .setPositiveButton("Awesome!", null)
            .show()
    }

    private fun loadPlayerData() {
        val nickname = sharedPreferences.getString(KEY_NICKNAME, DEFAULT_NICKNAME) ?: DEFAULT_NICKNAME
        val experience = sharedPreferences.getInt(KEY_EXPERIENCE, 0)

        // Update UI with loaded data
        findViewById<TextView>(R.id.nicknameTextView).text = nickname

        Log.d("PlayerData", "Loaded - Nickname: $nickname, XP: $experience")
    }

    private fun addExperience(amount: Int) {
        val currentExp = sharedPreferences.getInt(KEY_EXPERIENCE, 0)
        val newExp = currentExp + amount

        editor.putInt(KEY_EXPERIENCE, newExp)
        editor.apply()

        updateExperienceProgress(newExp)

        Log.d("PlayerData", "Added $amount XP. Total: $newExp")
    }

    private fun updateExperienceProgress(experience: Int) {
        val progressBar = findViewById<ProgressBar>(R.id.experienceProgressBar)

        val level = (experience / LEVEL_UP_THRESHOLD) + 1
        val currentLevelExp = experience % LEVEL_UP_THRESHOLD

        progressBar.max = LEVEL_UP_THRESHOLD
        progressBar.progress = currentLevelExp

        findViewById<TextView>(R.id.level_num).text = "$level"

        if (currentLevelExp == 0 && experience > 0) {
            Toast.makeText(this, "🎉 Level Up! You reached level $level!", Toast.LENGTH_LONG).show()
        }
    }

    fun updateNickname(newNickname: String) {
        editor.putString(KEY_NICKNAME, newNickname)
        editor.apply()

        findViewById<TextView>(R.id.nicknameTextView).text = newNickname
        Log.d("PlayerData", "Nickname updated to: $newNickname")
    }

    private fun updateLocationText() {
        val locationText = findViewById<TextView>(R.id.adress)

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    getAddressFromLocation(location.latitude, location.longitude) { address ->
                        locationText.text = address ?: "Unknown location"
                    }
                } else {
                    locationText.text = "Location unavailable"
                }
            }
        } catch (e: SecurityException) {
            locationText.text = "Location permission needed"
        }
    }

    private fun getAddressFromLocation(lat: Double, lng: Double, callback: (String?) -> Unit) {
        val geocoder = Geocoder(this, Locale.getDefault())

        try {
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (addresses?.isNotEmpty() == true) {
                val address = addresses[0]
                val locationName = if (address.locality != null) {
                    if (address.subLocality != null) {
                        "${address.locality}, ${address.subLocality}"
                    } else {
                        address.locality
                    }
                } else {
                    "${address.latitude}, ${address.longitude}"
                }
                callback(locationName)
            } else {
                callback(null)
            }
        } catch (e: Exception) {
            callback(null)
        }
    }

}