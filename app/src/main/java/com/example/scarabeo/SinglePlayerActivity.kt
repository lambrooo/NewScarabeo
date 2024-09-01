package com.example.scarabeo

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class SinglePlayerActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var initialLetterTextView: TextView
    private lateinit var wordEditText: EditText
    private lateinit var submitWordButton: Button
    private lateinit var endGameButton: Button
    private lateinit var viewUsedWordsButton: Button
    private lateinit var scoreTextView: TextView
    private lateinit var timerTextView: TextView
    private lateinit var aiWordTextView: TextView
    private lateinit var aiThinkingProgressBar: ProgressBar
    private lateinit var turnTextView: TextView
    private lateinit var conditionsTextView: TextView

    private lateinit var dictionaryManager: DictionaryManager
    private lateinit var aiPlayer: AIPlayer
    private var playerScore = 0
    private var aiScore = 0
    private lateinit var countDownTimer: CountDownTimer
    private var isFirstTurn = true
    private var difficulty: String = "Facile"
    private var minWordLength = 3
    private var requiredLetters = listOf<Char>()
    private val usedWords = mutableSetOf<String>()

    private var turnCount = 0
    private val maxTurns = 30
    private var baseLoseProbability = 0.05

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_player)

        initializeViews()
        setupListeners()
        loadDifficultySettings()
        initializeDictionaryManager()
    }

    private fun initializeViews() {
        sharedPreferences = getSharedPreferences("game_stats", Context.MODE_PRIVATE)
        initialLetterTextView = findViewById(R.id.initialLetterTextView)
        wordEditText = findViewById(R.id.wordEditText)
        submitWordButton = findViewById(R.id.submitWordButton)
        endGameButton = findViewById(R.id.endGameButton)
        viewUsedWordsButton = findViewById(R.id.viewUsedWordsButton)
        scoreTextView = findViewById(R.id.scoreTextView)
        timerTextView = findViewById(R.id.timerTextView)
        aiWordTextView = findViewById(R.id.aiWordTextView)
        aiThinkingProgressBar = findViewById(R.id.aiThinkingProgressBar)
        turnTextView = findViewById(R.id.turnTextView)
        conditionsTextView = findViewById(R.id.conditionsTextView)
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

        endGameButton.setOnClickListener { endGame("Giocatore ha terminato il gioco") }
        viewUsedWordsButton.setOnClickListener { showUsedWords() }
    }

    private fun loadDifficultySettings() {
        difficulty = intent.getStringExtra("DIFFICULTY") ?: "Facile"
        when (difficulty) {
            "Facile" -> {
                baseLoseProbability = 0.05
                minWordLength = 3
                requiredLetters = listOf()
            }
            "Medio" -> {
                baseLoseProbability = 0.03
                minWordLength = 5
                requiredLetters = generateRandomLetters(2)
            }
            "Difficile" -> {
                baseLoseProbability = 0.01
                minWordLength = 7
                requiredLetters = generateRandomLetters(3)
            }
        }
        updateConditionsTextView()
    }

    private fun initializeDictionaryManager() {
        dictionaryManager = DictionaryManager(this)
        lifecycleScope.launch {
            dictionaryManager.loadDictionary()
            withContext(Dispatchers.Main) {
                aiPlayer = AIPlayer(dictionaryManager)
                resetGame()
                startPlayerTurn()
            }
        }
    }

    private fun resetGame() {
        playerScore = 0
        aiScore = 0
        turnCount = 0
        isFirstTurn = true
        usedWords.clear()
        updateScore()
        loadDifficultySettings()
    }

    private fun startPlayerTurn() {
        if (shouldEndGame()) {
            endGame("Limite di turni o punteggio raggiunto")
            return
        }

        turnTextView.text = "Turno del Giocatore"
        turnTextView.setTextColor(Color.BLACK)

        if (isFirstTurn) {
            val randomLetter = ('A'..'Z').random()
            initialLetterTextView.text = randomLetter.toString()
            isFirstTurn = false
        }

        startTimer()
    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timerTextView.text = "Tempo rimasto: ${millisUntilFinished / 1000} secondi"
                if (millisUntilFinished <= 10000) {
                    timerTextView.setTextColor(Color.RED)
                } else {
                    timerTextView.setTextColor(Color.BLACK)
                }
            }

            override fun onFinish() {
                endGame("Tempo scaduto")
            }
        }.start()
    }

    private fun submitWord(word: String) {
        playerScore += word.length
        updateScore()
        initialLetterTextView.text = word.last().toString().toUpperCase()
        wordEditText.text.clear()
        usedWords.add(word.toLowerCase())
        countDownTimer.cancel()

        if (shouldEndGame()) {
            endGame("Punteggio massimo raggiunto")
        } else {
            computerTurn()
        }
    }

    private fun computerTurn() {
        turnCount++
        if (shouldEndGame()) {
            endGame("Limite di turni o punteggio raggiunto")
            return
        }

        turnTextView.text = "Turno dell'IA"
        turnTextView.setTextColor(Color.RED)
        aiThinkingProgressBar.visibility = ProgressBar.VISIBLE

        lifecycleScope.launch(Dispatchers.Default) {
            val aiLoses = checkIfAILoses()
            if (aiLoses) {
                withContext(Dispatchers.Main) {
                    endGame("L'IA non riesce a trovare una parola valida")
                }
                return@launch
            }

            val startingLetter = initialLetterTextView.text.toString().first()
            val word = aiPlayer.generateWord(startingLetter, minWordLength, requiredLetters)

            withContext(Dispatchers.Main) {
                aiThinkingProgressBar.visibility = ProgressBar.GONE
                if (word != null && !usedWords.contains(word.toLowerCase())) {
                    aiScore += word.length
                    updateScore()
                    aiWordTextView.text = "Parola IA: $word"
                    initialLetterTextView.text = word.last().toString().toUpperCase()
                    usedWords.add(word.toLowerCase())
                    startPlayerTurn()
                } else {
                    endGame("L'IA non riesce a trovare una parola valida")
                }
            }
        }
    }

    private fun checkIfAILoses(): Boolean {
        val currentLoseProbability = baseLoseProbability * (1 + turnCount / 10.0)
        return Random.nextDouble() < currentLoseProbability
    }

    private fun shouldEndGame(): Boolean {
        return turnCount >= maxTurns || playerScore >= 100 || aiScore >= 100
    }

    private fun updateScore() {
        scoreTextView.text = "Punteggio: Giocatore $playerScore - IA $aiScore"
    }

    private fun endGame(reason: String) {
        if (::countDownTimer.isInitialized) {
            countDownTimer.cancel()
        }
        val winner = when {
            playerScore > aiScore -> "Giocatore"
            aiScore > playerScore -> "IA"
            else -> "Pareggio"
        }
        val message = "Il gioco è finito!\nMotivo: $reason\nVincitore: $winner\nPunteggio finale: Giocatore $playerScore - IA $aiScore"
        AlertDialog.Builder(this)
            .setTitle("Fine del Gioco")
            .setMessage(message)
            .setPositiveButton("Nuova Partita") { _, _ ->
                updateStatistics(winner)
                resetGame()
                startPlayerTurn()
            }
            .setNegativeButton("Esci") { _, _ ->
                updateStatistics(winner)
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun showUsedWords() {
        AlertDialog.Builder(this)
            .setTitle("Parole Usate")
            .setMessage(usedWords.joinToString("\n"))
            .setPositiveButton("OK", null)
            .show()
    }

    private fun updateConditionsTextView() {
        val conditions = "Lunghezza minima: $minWordLength\nLettere richieste: ${requiredLetters.joinToString(", ")}"
        conditionsTextView.text = conditions
    }

    private fun generateRandomLetters(count: Int): List<Char> {
        val letters = listOf('A', 'E', 'I', 'O', 'U')
        return List(count) { letters.random() }
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
            "Giocatore" -> editor.putInt("gamesWon", gamesWon + 1)
            "IA" -> editor.putInt("gamesLost", gamesLost + 1)
        }

        val userWords = usedWords.filterIndexed { index, _ -> index % 2 == 0 }
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

        editor.putInt("gamesPlayed", gamesPlayed)
        editor.putString("longestWord", longestWord)
        editor.putString("mostUsedWord", mostUsedWord)
        editor.putInt("mostUsedWordCount", mostUsedWordCount)
        editor.apply()
    }

    private fun isValidWord(word: String): Boolean {
        return word.length >= minWordLength &&
                word.startsWith(initialLetterTextView.text.toString(), ignoreCase = true) &&
                requiredLetters.all { word.contains(it, ignoreCase = true) } &&
                dictionaryManager.isValidWord(word) &&
                !usedWords.contains(word.toLowerCase())
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::countDownTimer.isInitialized) {
            countDownTimer.cancel()
        }
    }
}