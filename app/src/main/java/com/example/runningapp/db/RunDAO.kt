package com.example.runningapp.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao // Note that things like this are called annotation
interface RunDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE) // This means that when we are trying to insert a new run that already exists then that new run will replace it
    suspend fun insertRun(run: Run)

    @Delete
    suspend fun deleteRun(run: Run)

    // Then we will use data base for the query
    @Query("SELECT * FROM running_table ORDER BY timestamp DESC") // This query means that we are getting from the "running_table" table in our data base and then we are doing it in the order of our time stamp and then in descending order.
    // This above is just like an SQL code that is used to get the data from the Entity that we had created in our "Run.kt" data class
    fun getALlRunsSortedByDate() :LiveData<List<Run>>// Note that we will not be using a suspend function here because if we are trying to get a live data from data base then we do not to put in a coroutine scope
    // Note that this above means that it will return a live data from the data base of type Run

    // To sort by timeInMillis
    @Query("SELECT * FROM running_table ORDER BY timeInMillis DESC")
    fun getALlRunsSortedByTimeInMillis() :LiveData<List<Run>>

    // To sort by caloriesBurned
    @Query("SELECT * FROM running_table ORDER BY caloriesBurned DESC")
    fun getALlRunsSortedByCaloriesBurned() :LiveData<List<Run>>

    // To sort by Average speed
    @Query("SELECT * FROM running_table ORDER BY avgSpeedInKMH DESC")
    fun getALlRunsSortedByAvgSpeed() :LiveData<List<Run>>

    // To sort by distance
    @Query("SELECT * FROM running_table ORDER BY distanceInMeters DESC")
    fun getALlRunsSortedByDistance() :LiveData<List<Run>>

    // To get the addition of all the sum of our time in milliseconds directly from our database

    @Query("SELECT SUM(timeInMillis) FROM running_table") // This is used to get the sum of all the "timeInMillis" from the database entity "running_table"
    fun getTotalTimeInMillis(): LiveData<Long> // This is expected to return a long value

    // To get the sum of the total calories burned
    @Query("SELECT SUM(caloriesBurned) FROM running_table")
    fun getTotalCaloriesBurned(): LiveData<Int>

    // To get the sum of the total distance
    @Query("SELECT SUM(distanceInMeters) FROM running_table")
    fun getTotalDistance(): LiveData<Int>

    // To get the sum of the average speed
    @Query("SELECT SUM(avgSpeedInKMH) FROM running_table")
    fun getTotalAvgSpeed(): LiveData<Float>







}