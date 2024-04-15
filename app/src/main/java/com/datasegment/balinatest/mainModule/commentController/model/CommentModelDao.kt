package com.datasegment.balinatest.mainModule.commentController.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CommentDao {
    @Query("SELECT * FROM comment")
    fun getAll(): List<Comment>

    @Query("SELECT * FROM comment WHERE id IN (:id)")
    fun loadAllById(id: Int): Comment

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(vararg photo: Comment)

    @Query("DELETE FROM comment WHERE id = :id")
    fun delete(id: Int)
}
