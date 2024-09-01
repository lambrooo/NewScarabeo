package com.example.scarabeo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object GameData {
    private var _gameModel: MutableLiveData<GameModel> = MutableLiveData()
    var gameModel: LiveData<GameModel> = _gameModel
    var myID = ""
    private var currentGameId: String? = null

    fun saveGameModel(model: GameModel) {
        _gameModel.postValue(model)
        currentGameId = model.gameId
        if (model.gameId != "-1") {
            Firebase.firestore.collection("scarabeo_games")
                .document(model.gameId)
                .set(model)
                .addOnFailureListener { e ->
                    println("Error saving game model: ${e.message}")
                }
        }
    }

    fun fetchGameModel() {
        currentGameId?.let { gameId ->
            Firebase.firestore.collection("scarabeo_games")
                .document(gameId)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        println("Error fetching game model: ${error.message}")
                        return@addSnapshotListener
                    }
                    val model = value?.toObject(GameModel::class.java)
                    model?.let { _gameModel.postValue(it) }
                }
        } ?: println("No current game ID set")
    }

    fun setCurrentGameId(gameId: String) {
        currentGameId = gameId
    }
}