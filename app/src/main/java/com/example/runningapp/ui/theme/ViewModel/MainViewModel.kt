package com.example.runningapp.ui.theme.ViewModel
// The job of this view Model is to collect the data from our repository and provide to all the fragments
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runningapp.db.Run
import com.example.runningapp.other.SortType
import com.example.runningapp.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val mainRepository: MainRepository

) : ViewModel() {

    private val runSortedByDate = mainRepository.getAllRunsSortedByDate()
    private val runSortedByDistance = mainRepository.getAllRunsSortedByDate()
    private val runSortedByCaloriesBurned = mainRepository.getAllRunsSortedByDate()
    private val runSortedByTimeInMillis = mainRepository.getAllRunsSortedByDate()
    private val runSortedByAvgSpeed = mainRepository.getAllRunsSortedByDate()

    // Mediator live data is a type of live data that allows the user to merge several live data together

    val runs = MediatorLiveData<List<Run>>()

    var sortType = SortType.DATE // This is our default sort type

    init { // Note that the "init" block is executed once the instance has been created
        runs.addSource(runSortedByDate) { result ->
            if (sortType == SortType.DATE) {
                result?.let {
                    runs.value = it
                } // "it" here represents the result that is we are storing the results got from "runsSortedByDate"
            }
        }
        runs.addSource(runSortedByAvgSpeed) { result ->
            if (sortType == SortType.AVG_SPEED) {
                result?.let { runs.value = it }
            }

        }

        runs.addSource(runSortedByCaloriesBurned) { result ->
            if (sortType == SortType.CALORIES_BURNED) {
                result?.let { runs.value = it }
            }

        }
        runs.addSource(runSortedByDistance) { result ->
            if (sortType == SortType.DISTANCE) {
                result?.let { runs.value = it }
            }
        }
        runs.addSource(runSortedByTimeInMillis) { result ->
            if (sortType == SortType.RUNNING_TIME) {
                result?.let { runs.value = it }
            }
        }
    }

    fun sortRuns(sortType: SortType) = when (sortType){
        SortType.DATE -> runSortedByDate.value?.let{ runs.value = it}
        SortType.RUNNING_TIME -> runSortedByTimeInMillis.value?.let{ runs.value = it}
        SortType.AVG_SPEED -> runSortedByAvgSpeed.value?.let{ runs.value = it}
        SortType.DISTANCE -> runSortedByDistance.value?.let{ runs.value = it}
        SortType.CALORIES_BURNED -> runSortedByCaloriesBurned.value?.let{ runs.value = it}
    }.also {
        this.sortType = sortType // This is used to the sort type also at the end
    }






    fun insertRun(run: Run) = viewModelScope.launch {
        mainRepository.insertRun(run)
    }


    }