package com.example.testmap

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()


        // Load player data when activity starts
        loadPlayerData()


        findViewById<Button>(R.id.supabutton)
            .setOnClickListener {
                startActivity(Intent(this, MapsActivity::class.java))
            }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
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

    // Method to update nickname
    fun updateNickname(newNickname: String) {
        editor.putString(KEY_NICKNAME, newNickname)
        editor.apply()

        Log.d("PlayerData", "Nickname updated to: $newNickname")
    }

    // Method to get current player data
    fun getPlayerData(): Pair<String, Int> {
        val nickname = sharedPreferences.getString(KEY_NICKNAME, DEFAULT_NICKNAME) ?: DEFAULT_NICKNAME
        val experience = sharedPreferences.getInt(KEY_EXPERIENCE, 0)
        return Pair(nickname, experience)
    }

    // Method to reset player data
    fun resetPlayerData() {
        editor.clear()
        editor.apply()
        loadPlayerData() // Reload with default values
    }
}