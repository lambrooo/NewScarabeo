package com.example.scarabeo
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class StatisticsActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var gamesPlayedTextView: TextView
    private lateinit var gamesWonTextView: TextView
    private lateinit var gamesLostTextView: TextView
    private lateinit var winPercentageTextView: TextView
    private lateinit var longestWordTextView: TextView
    private lateinit var mostUsedWordTextView: TextView

    private val TAG = "StatisticsActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        Log.d(TAG, "onCreate called")

        sharedPreferences = getSharedPreferences("game_stats", Context.MODE_PRIVATE)

        initializeViews()
        loadStatistics()
    }

    private fun initializeViews() {
        gamesPlayedTextView = findViewById(R.id.gamesPlayedTextView)
        gamesWonTextView = findViewById(R.id.gamesWonTextView)
        gamesLostTextView = findViewById(R.id.gamesLostTextView)
        winPercentageTextView = findViewById(R.id.winPercentageTextView)
        longestWordTextView = findViewById(R.id.longestWordTextView)
        mostUsedWordTextView = findViewById(R.id.mostUsedWordTextView)
    }

    private fun loadStatistics() {
        Log.d(TAG, "Loading statistics")
        try {
            val gamesPlayed = sharedPreferences.getInt("gamesPlayed", 0)
            val gamesWon = sharedPreferences.getInt("gamesWon", 0)
            val gamesLost = sharedPreferences.getInt("gamesLost", 0)
            val winPercentage = if (gamesPlayed > 0) (gamesWon.toDouble() / gamesPlayed * 100).toInt() else 0
            val longestWord = sharedPreferences.getString("longestWord", "N/A") ?: "N/A"
            val mostUsedWord = sharedPreferences.getString("mostUsedWord", "N/A") ?: "N/A"
            val mostUsedWordCount = sharedPreferences.getInt("mostUsedWordCount", 0)

            updateUI(gamesPlayed, gamesWon, gamesLost, winPercentage, longestWord, mostUsedWord, mostUsedWordCount)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading statistics", e)
        }
    }

    private fun updateUI(gamesPlayed: Int, gamesWon: Int, gamesLost: Int, winPercentage: Int, longestWord: String, mostUsedWord: String, mostUsedWordCount: Int) {
        val fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        fadeIn.duration = 1000

        gamesPlayedTextView.text = "Partite giocate: $gamesPlayed"
        gamesWonTextView.text = "Partite vinte: $gamesWon"
        gamesLostTextView.text = "Partite perse: $gamesLost"
        winPercentageTextView.text = "Percentuale vittoria: $winPercentage%"
        longestWordTextView.text = "Parola più lunga: $longestWord"
        mostUsedWordTextView.text = "Parola più usata: $mostUsedWord ($mostUsedWordCount volte)"

        val views = listOf(gamesPlayedTextView, gamesWonTextView, gamesLostTextView, winPercentageTextView, longestWordTextView, mostUsedWordTextView)
        views.forEach { it.startAnimation(fadeIn) }

        val slideIn = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left)
        slideIn.duration = 1000
        slideIn.startOffset = 500

        views.forEach { it.startAnimation(slideIn) }
    }
}