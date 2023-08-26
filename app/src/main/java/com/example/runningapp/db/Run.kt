package com.example.runningapp.db

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Timestamp

@Entity(tableName = "running_table")
// An entity is just like a table in our data base and then each column in that table/entity will represent the property of our table
// Just like for this object class "Run" will be the entity and the it will have columns for average speed and all
// A primary key is a unique identifier for each identifier in our database table
data class Run(
    var img : Bitmap? = null, // Note that room is designed to save less complex objects unlike a bitmap. We the need to use a type converter to then convert the bitmap to a format that room understands
    // So then we create a converter class
    var timestamp: Long = 0L,// Note that we are not storing this as a date because it is easier to sort out a long value instead (time stamp describes when our run was)
    var avgSpeedInKMH : Float = 0f,
    var distanceInMeters : Int =0,
    var timeInMillis : Long = 0L, // This describes how long our run was
    var caloriesBurned : Int =0

) {
    @PrimaryKey(autoGenerate = true) // This means that room will auto generate this for us
    var id : Int ?= null
}