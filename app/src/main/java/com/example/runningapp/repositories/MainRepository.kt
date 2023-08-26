package com.example.runningapp.repositories

import com.example.runningapp.db.Run
import com.example.runningapp.db.RunDAO
import javax.inject.Inject

// This is used to get the functions of the database through injection using dagger hilt
// The job of this is to collect all of our data from our data sources
class MainRepository @Inject constructor(
    val runDao : RunDAO
    ){
    suspend fun insertRun(run: Run) =runDao.insertRun(run)

    suspend fun deleteRun(run: Run) = runDao.deleteRun(run)

    fun getAllRunsSortedByDate() = runDao.getALlRunsSortedByDate() // Note that since this is asynchronous then we don't have to put it in a suspend function because it is operated by live data

    fun getAllRunsSortedByDistance() = runDao.getALlRunsSortedByDistance()

    fun getAllRunsSortedByTimeInMillis() = runDao.getALlRunsSortedByTimeInMillis()

    fun getAllRunsSortedByAvgSpeed() = runDao.getALlRunsSortedByAvgSpeed()

    fun getAllRunsSortedByCaloriesBurned() = runDao.getALlRunsSortedByCaloriesBurned()

    fun getTotalAvgSpeed() = runDao.getTotalAvgSpeed()

    fun getTotalDistance() = runDao.getTotalDistance()

    fun getTotalCaloriesBurned() = runDao.getTotalCaloriesBurned()

    fun getTotalTimeInMillis() = runDao.getTotalTimeInMillis()





}