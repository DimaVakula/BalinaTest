package com.datasegment.balinatest.mainModule.database.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PhotoDao {
    @Query("SELECT * FROM photo")
    fun getAll(): List<Photo>

    @Query("SELECT * FROM photo WHERE id IN (:id)")
    fun loadAllById(id: Int): Photo

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(vararg photo: Photo)

    @Query("DELETE FROM photo WHERE id = :id")
    fun delete(id: Int)
}
