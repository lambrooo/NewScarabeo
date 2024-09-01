package com.example.scarabeo

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: User)

    @Query("SELECT id, username, score FROM users ORDER BY score DESC")
    fun getAllUsers(): LiveData<List<UserResult>>
}