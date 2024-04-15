package com.datasegment.balinatest.mainModule.commentController.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Comment(
    @PrimaryKey var id: Int,
    @ColumnInfo(name = "text") var text: String?,
    @ColumnInfo(name = "date") var date: Int,
)