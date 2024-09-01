package com.example.scarabeo

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class DictionaryManager(private val context: Context) {
    private val _dictionaryLoaded = MutableLiveData<Boolean>()
    val dictionaryLoaded: LiveData<Boolean> = _dictionaryLoaded

    private var dictionary: Set<String> = setOf()

    suspend fun loadDictionary() {
        withContext(Dispatchers.IO) {
            dictionary = loadDictionaryFromAssets("parole_italiane.txt")
            _dictionaryLoaded.postValue(true)
        }
    }

    fun isValidWord(word: String): Boolean {
        return dictionary.contains(word.toLowerCase())
    }

    fun getDictionary(): Set<String> = dictionary

    fun getRandomWord(startingLetter: Char? = null, minLength: Int = 0): String? {
        val filteredWords = dictionary.filter { word ->
            (startingLetter == null || word.startsWith(startingLetter, ignoreCase = true)) &&
                    word.length >= minLength
        }
        return filteredWords.randomOrNull()
    }

    private fun loadDictionaryFromAssets(fileName: String): Set<String> {
        val words = mutableSetOf<String>()
        try {
            context.assets.open(fileName).use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.forEachLine { line ->
                        words.add(line.trim().toLowerCase())
                    }
                }
            }
        } catch (e: Exception) {
            println("Error loading dictionary: ${e.message}")
            _dictionaryLoaded.postValue(false)
        }
        return words
    }
}