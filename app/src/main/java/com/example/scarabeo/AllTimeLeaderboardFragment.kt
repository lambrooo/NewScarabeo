package com.example.scarabeo

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AllTimeLeaderboardFragment : Fragment() {
    private lateinit var listView: ListView
    private lateinit var noDataTextView: TextView
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_leaderboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView = view.findViewById(R.id.leaderboardListView)
        noDataTextView = view.findViewById(R.id.noDataTextView)
        loadAllTimeLeaderboard()
    }

    private fun loadAllTimeLeaderboard() {
        db.collection("users")
            .orderBy("totalScore", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { documents ->
                val leaderboardData = documents.mapNotNull { doc ->
                    val data = doc.data
                    mapOf(
                        "userId" to doc.id,
                        "score" to (data["totalScore"] as? Long ?: 0).toString()
                    )
                }
                if (leaderboardData.isEmpty()) {
                    showNoDataMessage()
                } else {
                    updateListView(leaderboardData)
                }
            }
            .addOnFailureListener { e ->
                Log.e("AllTimeLeaderboardFragment", "Error loading all-time leaderboard", e)
                showErrorMessage(e.message ?: "Unknown error")
            }
    }

    private fun updateListView(data: List<Map<String, String>>) {
        val list = data.mapIndexed { index, entry ->
            mapOf(
                "rank" to (index + 1).toString(),
                "userId" to entry["userId"]!!,
                "score" to entry["score"]!!
            )
        }

        val adapter = SimpleAdapter(
            requireContext(),
            list,
            R.layout.leaderboard_item,
            arrayOf("rank", "userId", "score"),
            intArrayOf(R.id.rankTextView, R.id.userIdTextView, R.id.scoreTextView)
        )

        listView.adapter = adapter
        listView.visibility = View.VISIBLE
        noDataTextView.visibility = View.GONE
    }

    private fun showNoDataMessage() {
        listView.visibility = View.GONE
        noDataTextView.visibility = View.VISIBLE
        noDataTextView.text = "Nessun punteggio disponibile"
    }

    private fun showErrorMessage(message: String) {
        listView.visibility = View.GONE
        noDataTextView.visibility = View.VISIBLE
        noDataTextView.text = "Errore nel caricamento: $message"
    }
}