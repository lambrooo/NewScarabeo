package com.example.scarabeo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class DifficultySelectionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_difficulty_selection)

        val easyButton: Button = findViewById(R.id.easyButton)
        val mediumButton: Button = findViewById(R.id.mediumButton)
        val hardButton: Button = findViewById(R.id.hardButton)

        easyButton.setOnClickListener {
            startSinglePlayerGame("Facile")
        }

        mediumButton.setOnClickListener {
            startSinglePlayerGame("Medio")
        }

        hardButton.setOnClickListener {
            startSinglePlayerGame("Difficile")
        }
    }

    private fun startSinglePlayerGame(difficulty: String) {
        val intent = Intent(this, SinglePlayerActivity::class.java)
        intent.putExtra("DIFFICULTY", difficulty)
        startActivity(intent)
    }
}