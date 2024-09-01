package com.example.scarabeo

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MultiplayerGameActivity : AppCompatActivity() {

    private lateinit var gameIdTextView: TextView
    private lateinit var currentPlayerTextView: TextView
    private lateinit var initialLetterTextView: TextView
    private lateinit var scoreTextView: TextView
    private lateinit var timerTextView: TextView
    private lateinit var lastWordTextView: TextView
    private lateinit var conditionsTextView: TextView
    private lateinit var wordEditText: EditText
    private lateinit var submitWordButton: MaterialButton
    private lateinit var viewUsedWordsButton: MaterialButton
    private lateinit var abandonGameButton: MaterialButton

    private var gameModel: GameModel? = null
    private lateinit var dictionaryManager: DictionaryManager
    private lateinit var countDownTimer: CountDownTimer
    private var waitingDialog: AlertDialog? = null
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multiplayer_game)

        sharedPreferences = getSharedPreferences("game_stats", Context.MODE_PRIVATE)

        initializeViews()
        setupListeners()
        initializeDictionaryManager()

        val gameId = GameData.gameModel.value?.gameId ?: "-1"
        if (gameId == "-1") {
            showErrorAndFinish("Invalid Game ID")
            return
        }

        GameData.setCurrentGameId(gameId)
        GameData.fetchGameModel()
        GameData.gameModel.observe(this, Observer {
            gameModel = it
            checkGameStart()
            updateUI()
        })
    }

    private fun initializeViews() {
        gameIdTextView = findViewById(R.id.gameIdTextView)
        currentPlayerTextView = findViewById(R.id.currentPlayerTextView)
        initialLetterTextView = findViewById(R.id.initialLetterTextView)
        scoreTextView = findViewById(R.id.scoreTextView)
        timerTextView = findViewById(R.id.timerTextView)
        lastWordTextView = findViewById(R.id.lastWordTextView)
        conditionsTextView = findViewById(R.id.conditionsTextView)
        wordEditText = findViewById(R.id.wordEditText)
        submitWordButton = findViewById(R.id.submitWordButton)
        viewUsedWordsButton = findViewById(R.id.viewUsedWordsButton)
        abandonGameButton = findViewById(R.id.abandonGameButton)
    }

    private fun setupListeners() {
        submitWordButton.setOnClickListener {
            val word = wordEditText.text.toString()
            if (isValidWord(word)) {
                submitWord(word)
            } else {
                wordEditText.error = "Parola non valida"
            }
        }

        viewUsedWordsButton.setOnClickListener {
            showUsedWords()
        }

        abandonGameButton.setOnClickListener {
            showAbandonGameConfirmation()
        }
    }

    private fun initializeDictionaryManager() {
        dictionaryManager = DictionaryManager(this)
        CoroutineScope(Dispatchers.Main).launch {
            dictionaryManager.loadDictionary()
        }
    }

    private fun updateUI() {
        gameModel?.apply {
            gameIdTextView.text = "Game ID: $gameId"
            currentPlayerTextView.text = "Current Player: ${getCurrentPlayer()}"
            initialLetterTextView.text = lastLetter
            scoreTextView.text = "Scores: ${playerScores.map { "${it.key}: ${it.value}" }.joinToString(", ")}"
            lastWordTextView.text = "Last Word: $lastWord"
            conditionsTextView.text = "Min Length: $minWordLength, Required Letters: ${requiredLetters.joinToString()}"

            when (gameStatus) {
                GameStatus.CREATED -> showWaitingForPlayersMessage()
                GameStatus.JOINED -> {
                    if (players.size < 2) {
                        showWaitingForPlayersMessage()
                    } else {
                        dismissWaitingMessage()
                        if (isReadyToStart()) {
                            startGame()
                            GameData.saveGameModel(this)
                        }
                    }
                }
                GameStatus.INPROGRESS -> {
                    dismissWaitingMessage()
                    if (isCurrentPlayerTurn(GameData.myID)) {
                        enableWordSubmission()
                        startTimer()
                    } else {
                        disableWordSubmission()
                        stopTimer()
                    }
                }
                GameStatus.FINISHED -> {
                    dismissWaitingMessage()
                    showGameOverDialog()
                }
            }
        }
    }

    private fun checkGameStart() {
        gameModel?.apply {
            if (isReadyToStart() && gameStatus == GameStatus.JOINED) {
                startGame()
                GameData.saveGameModel(this)
            }
        }
    }

    private fun isValidWord(word: String): Boolean {
        if (!dictionaryManager.dictionaryLoaded.value!!) {
            showError("Dictionary not loaded yet")
            return false
        }

        return gameModel?.let { model ->
            word.length >= model.minWordLength &&
                    word.startsWith(model.lastLetter, ignoreCase = true) &&
                    model.requiredLetters.all { word.contains(it, ignoreCase = true) } &&
                    dictionaryManager.isValidWord(word) &&
                    !model.usedWords.contains(word.toLowerCase())
        } ?: false
    }

    private fun submitWord(word: String) {
        gameModel?.apply {
            if (isCurrentPlayerTurn(GameData.myID)) {
                lastWord = word
                lastLetter = word.last().toString().toUpperCase()
                usedWords.add(word.toLowerCase())
                val score = calculateScore(word)
                playerScores[GameData.myID] = (playerScores[GameData.myID] ?: 0) + score
                nextTurn()
                if (isGameOver()) {
                    gameStatus = GameStatus.FINISHED
                }
                GameData.saveGameModel(this)
                wordEditText.text.clear()
                stopTimer()
            } else {
                showError("Not your turn")
            }
        }
    }

    private fun calculateScore(word: String): Int {
        return word.length 
    }

    private fun startTimer() {
        stopTimer()
        countDownTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timerTextView.text = "Time left: ${millisUntilFinished / 1000} seconds"
            }

            override fun onFinish() {
                gameModel?.apply {
                    if (isCurrentPlayerTurn(GameData.myID)) {
                        nextTurn()
                        lastLetter = ('A'..'Z').random().toString()
                        GameData.saveGameModel(this)
                    }
                }
            }
        }.start()
    }

    private fun stopTimer() {
        if (::countDownTimer.isInitialized) {
            countDownTimer.cancel()
        }
    }

    private fun showGameOverDialog() {
        val winner = gameModel?.playerScores?.maxByOrNull { it.value }?.key ?: "No one"
        AlertDialog.Builder(this)
            .setTitle("Game Over")
            .setMessage("Winner: $winner")
            .setPositiveButton("OK") { _, _ ->
                updateStatistics(winner)
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun showUsedWords() {
        gameModel?.let {
            AlertDialog.Builder(this)
                .setTitle("Used Words")
                .setMessage(it.usedWords.joinToString("\n"))
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun enableWordSubmission() {
        wordEditText.isEnabled = true
        submitWordButton.isEnabled = true
    }

    private fun disableWordSubmission() {
        wordEditText.isEnabled = false
        submitWordButton.isEnabled = false
    }

    private fun showError(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showErrorAndFinish(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    private fun showWaitingForPlayersMessage() {
        if (waitingDialog == null) {
            waitingDialog = AlertDialog.Builder(this)
                .setTitle("Waiting for Players")
                .setMessage("Waiting for another player to join...")
                .setCancelable(false)
                .create()
        }
        waitingDialog?.show()
    }

    private fun dismissWaitingMessage() {
        waitingDialog?.dismiss()
    }

    private fun showAbandonGameConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Abandon Game")
            .setMessage("Are you sure you want to abandon the game? This will result in a loss.")
            .setPositiveButton("Yes") { _, _ ->
                abandonGame()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun abandonGame() {
        gameModel?.apply {
            abandonGame(GameData.myID)
            GameData.saveGameModel(this)
            updateStatistics(GameData.myID)
            finish()
        }
    }

    private fun startGame() {
        gameModel?.apply {
            gameStatus = GameStatus.INPROGRESS
            lastLetter = ('A'..'Z').random().toString()
            GameData.saveGameModel(this)
        }
    }

    private fun updateStatistics(winner: String) {
        val editor = sharedPreferences.edit()
        val gamesPlayed = sharedPreferences.getInt("gamesPlayed", 0) + 1
        val gamesWon = sharedPreferences.getInt("gamesWon", 0)
        val gamesLost = sharedPreferences.getInt("gamesLost", 0)
        var longestWord = sharedPreferences.getString("longestWord", "") ?: ""
        var mostUsedWord = sharedPreferences.getString("mostUsedWord", "") ?: ""
        var mostUsedWordCount = sharedPreferences.getInt("mostUsedWordCount", 0)

        when (winner) {
            GameData.myID -> editor.putInt("gamesWon", gamesWon + 1)
            else -> editor.putInt("gamesLost", gamesLost + 1)
        }

        gameModel?.let { model ->
            val userWords = model.usedWords.filterIndexed { index, _ -> model.players.keys.elementAt(index % model.players.size) == GameData.myID }
            for (word in userWords) {
                if (word.length > longestWord.length) {
                    longestWord = word
                }
                val wordCount = userWords.count { it == word }
                if (wordCount > mostUsedWordCount) {
                    mostUsedWord = word
                    mostUsedWordCount = wordCount
                }
            }
        }

        editor.putInt("gamesPlayed", gamesPlayed)
        editor.putString("longestWord", longestWord)
        editor.putString("mostUsedWord", mostUsedWord)
        editor.putInt("mostUsedWordCount", mostUsedWordCount)
        editor.apply()

        updateLeaderboards(winner)
    }

    private fun updateLeaderboards(winner: String) {
        gameModel?.let { model ->
            val score = model.playerScores[GameData.myID] ?: 0
            ScoreUtility.saveScore(score)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
    }
}