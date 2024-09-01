package com.example.scarabeo

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class LeaderboardPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> DailyLeaderboardFragment()
            1 -> AllTimeLeaderboardFragment()
            else -> throw IllegalArgumentException("Invalid position $position")
        }
    }
}