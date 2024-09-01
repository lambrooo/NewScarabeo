package com.example.scarabeo

class AIPlayer(private val dictionaryManager: DictionaryManager) {
    fun generateWord(startingLetter: Char, minLength: Int = 0, requiredLetters: List<Char> = listOf(), difficulty: Float = 1.0f): String? {
        val possibleWords = dictionaryManager.getDictionary().filter { word ->
            isValidWord(word, startingLetter, minLength, requiredLetters)
        }.shuffled()
        return possibleWords.randomOrNull()
    }

    fun isValidWord(word: String, startingLetter: Char, minLength: Int = 0, requiredLetters: List<Char> = listOf()): Boolean {
        return word.startsWith(startingLetter, ignoreCase = true) &&
                word.length >= minLength &&
                requiredLetters.all { letter -> word.contains(letter, ignoreCase = true) } &&
                dictionaryManager.isValidWord(word)
    }
}