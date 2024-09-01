package com.example.scarabeo
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog

class OptionsActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_options)

        sharedPreferences = getSharedPreferences("game_stats", Context.MODE_PRIVATE)

        val resetStatsButton: Button = findViewById(R.id.resetStatsButton)
        val changeThemeButton: Button = findViewById(R.id.changeThemeButton)

        resetStatsButton.setOnClickListener {
            resetStatistics()
        }

        changeThemeButton.setOnClickListener {
            changeTheme()
        }
    }

    private fun resetStatistics() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Reset Statistiche")
        builder.setMessage("Sei sicuro di voler resettare tutte le statistiche?")
        builder.setPositiveButton("Sì") { _, _ ->
            val editor = sharedPreferences.edit()
            editor.putInt("gamesPlayed", 0)
            editor.putInt("gamesWon", 0)
            editor.putInt("gamesLost", 0)
            editor.putString("longestWord", "")
            editor.putString("mostUsedWord", "")
            editor.putInt("mostUsedWordCount", 0)
            editor.apply()
        }
        builder.setNegativeButton("No", null)
        builder.show()
    }

    private fun changeTheme() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Cambia Tema")
        builder.setMessage("Questa funzionalità non è ancora implementata.")
        builder.setPositiveButton("OK", null)
        builder.show()
    }
}
