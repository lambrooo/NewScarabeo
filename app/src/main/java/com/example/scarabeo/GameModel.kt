package com.example.scarabeo

import com.google.firebase.firestore.Exclude

data class GameModel(
    var gameId: String = "-1",
    var players: MutableMap<String, String> = mutableMapOf(), // Map of userId to username
    var currentPlayerIndex: Int = 0,
    var lastWord: String = "",
    var lastLetter: String = "",
    var playerScores: MutableMap<String, Int> = mutableMapOf(),
    var usedWords: MutableList<String> = mutableListOf(),
    var gameStatus: GameStatus = GameStatus.CREATED,
    var minWordLength: Int = 3,
    var requiredLetters: List<Char> = listOf(),
    var turnCount: Int = 0,
    var maxTurns: Int = 30
) {
    var difficulty: GameDifficulty = GameDifficulty.EASY
        set(value) {
            field = value
            when (value) {
                GameDifficulty.EASY -> {
                    minWordLength = 3
                    requiredLetters = listOf()
                }
                GameDifficulty.MEDIUM -> {
                    minWordLength = 5
                    requiredLetters = listOf(('A'..'Z').random())
                }
                GameDifficulty.HARD -> {
                    minWordLength = 7
                    requiredLetters = listOf(('A'..'Z').random(), ('A'..'Z').random())
                }
            }
        }

    @Exclude
    fun getCurrentPlayer(): String = players.keys.elementAt(currentPlayerIndex)

    fun nextTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size
        turnCount++
    }

    fun isGameOver(): Boolean {
        return turnCount >= maxTurns || playerScores.any { it.value >= 100 }
    }

    fun isCurrentPlayerTurn(playerId: String): Boolean {
        return getCurrentPlayer() == playerId
    }

    fun isReadyToStart(): Boolean {
        return players.size == 2 && gameStatus == GameStatus.JOINED
    }

    fun startGame() {
        if (isReadyToStart()) {
            gameStatus = GameStatus.INPROGRESS
            lastLetter = ('A'..'Z').random().toString()
        }
    }

    fun abandonGame(playerId: String) {
        gameStatus = GameStatus.FINISHED
        val winner = players.keys.find { it != playerId }
        winner?.let { playerScores[it] = 100 }
    }

    fun addPlayer(userId: String, username: String) {
        players[userId] = username
        playerScores[userId] = 0
    }

    fun getPlayerName(userId: String): String {
        return players[userId] ?: "Unknown"
    }
}

enum class GameStatus {
    CREATED,
    JOINED,
    INPROGRESS,
    FINISHED
}

enum class GameDifficulty {
    EASY, MEDIUM, HARD
}