package com.example.scarabeo



import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GameViewModel : ViewModel() {
    private val _currentLetter = MutableLiveData<String>()
    val currentLetter: LiveData<String> get() = _currentLetter

    fun updateLetter(newLetter: String) {
        _currentLetter.value = newLetter
    }
}