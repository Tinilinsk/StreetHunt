package com.example.testmap

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    companion object {
        private const val PREFS_NAME = "player_data"
        private const val KEY_NICKNAME = "nickname"
        private const val KEY_EXPERIENCE = "experience"
        private const val DEFAULT_NICKNAME = "Player"
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

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()

        // Load player data when activity starts
        loadPlayerData()

        // ИСПРАВЛЕНИЕ: Используем mapsActivityResultLauncher вместо startActivity
        findViewById<Button>(R.id.supabutton).setOnClickListener {
            Log.d("MainActivity", "Launching MapsActivity with result launcher")
            val intent = Intent(this, MapsActivity::class.java)
            mapsActivityResultLauncher.launch(intent) // ВОТ ТАК НУЖНО!
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
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
        findViewById<TextView>(R.id.experienceTextView).text = "XP: $experience"

        Log.d("PlayerData", "Loaded - Nickname: $nickname, XP: $experience")
    }

    private fun addExperience(amount: Int) {
        val currentExp = sharedPreferences.getInt(KEY_EXPERIENCE, 0)
        val newExp = currentExp + amount

        editor.putInt(KEY_EXPERIENCE, newExp)
        editor.apply()

        findViewById<TextView>(R.id.experienceTextView).text = "XP: $newExp"

        Log.d("PlayerData", "Added $amount XP. Total: $newExp")
    }

    fun updateNickname(newNickname: String) {
        editor.putString(KEY_NICKNAME, newNickname)
        editor.apply()

        findViewById<TextView>(R.id.nicknameTextView).text = newNickname
        Log.d("PlayerData", "Nickname updated to: $newNickname")
    }

}