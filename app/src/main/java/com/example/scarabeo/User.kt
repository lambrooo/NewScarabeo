package com.example.scarabeo

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String = "",
    val username: String = "",
    val score: Int = 0,
    val level: Int = 1
)
