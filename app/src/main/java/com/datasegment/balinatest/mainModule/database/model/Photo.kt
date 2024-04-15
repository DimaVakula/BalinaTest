package com.datasegment.balinatest.mainModule.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Photo(
    @PrimaryKey var id: Int,
    @ColumnInfo(name = "url") var url: String?,
    @ColumnInfo(name = "date") var date: Int,
    @ColumnInfo(name = "lat") var lat: Double,
    @ColumnInfo(name = "lng") var lng: Double
)