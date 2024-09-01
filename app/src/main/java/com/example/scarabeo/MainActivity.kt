package com.example.scarabeo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var singlePlayerButton: Button
    private lateinit var multiplayerButton: Button
    private lateinit var optionsButton: Button
    private lateinit var statisticsButton: Button
    private lateinit var leaderboardButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        setContentView(R.layout.activity_main)

        initializeViews()
        setupListeners()
    }

    private fun initializeViews() {
        singlePlayerButton = findViewById(R.id.singlePlayerButton)
        multiplayerButton = findViewById(R.id.multiplayerButton)
        optionsButton = findViewById(R.id.optionsButton)
        statisticsButton = findViewById(R.id.statisticsButton)
        leaderboardButton = findViewById(R.id.leaderboardButton)
    }

    private fun setupListeners() {
        singlePlayerButton.setOnClickListener {
            startActivity(Intent(this, DifficultySelectionActivity::class.java))
        }

        multiplayerButton.setOnClickListener {
            startActivity(Intent(this, MultiplayerMenuActivity::class.java))
        }

        optionsButton.setOnClickListener {
            startActivity(Intent(this, OptionsActivity::class.java))
        }

        statisticsButton.setOnClickListener {
            startActivity(Intent(this, StatisticsActivity::class.java))
        }

        leaderboardButton.setOnClickListener {
            startActivity(Intent(this, LeaderboardActivity::class.java))
        }
    }
}