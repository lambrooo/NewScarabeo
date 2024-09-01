package com.example.scarabeo

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlin.random.Random

class MultiplayerMenuActivity : AppCompatActivity() {
    private lateinit var createGameButton: Button
    private lateinit var joinGameButton: Button
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multiplayer_menu)

        sharedPreferences = getSharedPreferences("game_prefs", MODE_PRIVATE)
        auth = FirebaseAuth.getInstance()

        initializeViews()
        setupListeners()
        checkAuthStatus()
    }

    private fun initializeViews() {
        createGameButton = findViewById(R.id.createGameButton)
        joinGameButton = findViewById(R.id.joinGameButton)
    }

    private fun setupListeners() {
        createGameButton.setOnClickListener {
            if (auth.currentUser != null) {
                createMultiplayerGame()
            } else {
                showLoginDialog()
            }
        }

        joinGameButton.setOnClickListener {
            if (auth.currentUser != null) {
                showJoinGameDialog()
            } else {
                showLoginDialog()
            }
        }
    }

    private fun checkAuthStatus() {
        if (auth.currentUser == null) {
            showLoginDialog()
        } else {
            GameData.myID = auth.currentUser?.uid ?: ""
            Toast.makeText(this, "Already authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLoginDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Login Required")
        builder.setMessage("Choose a login method:")
        builder.setPositiveButton("Email/Password") { _, _ ->
            showEmailPasswordLoginDialog()
        }
        builder.setNeutralButton("Anonymous") { _, _ ->
            signInAnonymously()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun showEmailPasswordLoginDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_email_password, null)
        val emailEditText = view.findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = view.findViewById<EditText>(R.id.passwordEditText)

        AlertDialog.Builder(this)
            .setView(view)
            .setPositiveButton("Login") { _, _ ->
                val email = emailEditText.text.toString()
                val password = passwordEditText.text.toString()
                signInWithEmailPassword(email, password)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun signInWithEmailPassword(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithEmail:success")
                    GameData.myID = auth.currentUser?.uid ?: ""
                    Toast.makeText(this, "Authentication successful.", Toast.LENGTH_SHORT).show()
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun signInAnonymously() {
        auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInAnonymously:success")
                    GameData.myID = auth.currentUser?.uid ?: ""
                    Toast.makeText(this, "Anonymous authentication successful.", Toast.LENGTH_SHORT).show()
                } else {
                    Log.w(TAG, "signInAnonymously:failure", task.exception)
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun createMultiplayerGame() {
        GameData.myID = auth.currentUser?.uid ?: return
        val gameId = Random.nextInt(1000, 9999).toString()
        val gameModel = GameModel(gameId = gameId)
        gameModel.addPlayer(GameData.myID, GameData.myID)
        GameData.saveGameModel(gameModel)
        startMultiplayerGame(gameId)
    }

    private fun showJoinGameDialog() {
        val input = EditText(this)
        AlertDialog.Builder(this)
            .setTitle("Join Game")
            .setMessage("Enter the game ID:")
            .setView(input)
            .setPositiveButton("Join") { _, _ ->
                val gameId = input.text.toString()
                if (gameId.isNotEmpty()) {
                    joinMultiplayerGame(gameId)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun joinMultiplayerGame(gameId: String) {
        GameData.myID = auth.currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("scarabeo_games").document(gameId).get()
            .addOnSuccessListener { document ->
                val gameModel = document.toObject(GameModel::class.java)
                if (gameModel != null) {
                    gameModel.addPlayer(GameData.myID, GameData.myID)
                    gameModel.gameStatus = GameStatus.JOINED
                    GameData.saveGameModel(gameModel)
                    startMultiplayerGame(gameId)
                } else {
                    showErrorDialog("Game not found")
                }
            }
            .addOnFailureListener {
                showErrorDialog("Failed to join game")
            }
    }

    private fun startMultiplayerGame(gameId: String) {
        val intent = Intent(this, MultiplayerGameActivity::class.java)
        intent.putExtra("GAME_ID", gameId)
        startActivity(intent)
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    companion object {
        private const val TAG = "MultiplayerMenuActivity"
    }
}