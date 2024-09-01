package com.example.scarabeo

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

object ScoreUtility {
    private val db = FirebaseFirestore.getInstance()

    fun saveScore(score: Int) {
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid ?: return

        saveDailyScore(userId, score)
        updateAllTimeScore(userId, score)
    }

    private fun saveDailyScore(userId: String, score: Int) {
        val dailyScoreRef = db.collection("dailyScores").document()

        val dailyScore = hashMapOf(
            "userId" to userId,
            "score" to score,
            "timestamp" to System.currentTimeMillis()
        )

        dailyScoreRef.set(dailyScore)
            .addOnSuccessListener {
                Log.d("SaveDailyScore", "Daily score saved successfully")
            }
            .addOnFailureListener { e ->
                Log.e("SaveDailyScore", "Error saving daily score", e)
            }
    }

    private fun updateAllTimeScore(userId: String, score: Int) {
        val userRef = db.collection("users").document(userId)

        db.runTransaction { transaction ->
            val userDoc = transaction.get(userRef)
            val currentTotalScore = userDoc.getLong("totalScore") ?: 0
            transaction.set(userRef, hashMapOf(
                "totalScore" to currentTotalScore + score
            ), SetOptions.merge())
        }.addOnSuccessListener {
            Log.d("UpdateAllTimeScore", "All-time score updated successfully")
        }.addOnFailureListener { e ->
            Log.e("UpdateAllTimeScore", "Error updating all-time score", e)
        }
    }
}