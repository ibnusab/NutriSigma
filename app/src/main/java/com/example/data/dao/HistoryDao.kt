package com.example.data.dao

import androidx.room.*
import com.example.data.model.HistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM histories WHERE userEmail = :userEmail ORDER BY timestamp DESC")
    fun getHistoryByUser(userEmail: String): Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryEntity)

    @Delete
    suspend fun deleteHistory(history: HistoryEntity)

    @Query("DELETE FROM histories WHERE id = :id")
    suspend fun deleteHistoryById(id: Int)

    @Query("DELETE FROM histories WHERE userEmail = :userEmail")
    suspend fun clearHistoryByUser(userEmail: String)
}
