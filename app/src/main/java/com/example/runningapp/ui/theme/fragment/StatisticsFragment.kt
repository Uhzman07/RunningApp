package com.example.runningapp.ui.theme.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.runningapp.R
import com.example.runningapp.other.CustomMarkerView
import com.example.runningapp.other.TrackingUtility
import com.example.runningapp.ui.theme.ViewModel.MainViewModel
import com.example.runningapp.ui.theme.ViewModel.StatisticsViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.round

// Note that whenever we are trying to inject something into our android component, we need need to add "@AndroidEntryPoint"
@AndroidEntryPoint
class StatisticsFragment : Fragment(R.layout.fragment_statistics){


    private val viewModel : StatisticsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeToObservers()
        setUpBarChart()
    }

    private fun setUpBarChart(){
        val barChart = view?.findViewById<BarChart>(R.id.barChart)
        // For the x axis
        barChart?.xAxis?.apply{
            // To set the position
            position = XAxis.XAxisPosition.BOTTOM
            setDrawLabels(false)
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false) // We just disabled this

        }

        // For the Y axis at the left hand side
        barChart?.axisLeft?.apply {
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }

        // For the Y axis at the right
        barChart?.axisRight?.apply {
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)

        }

        barChart?.apply {
            description.text = "Avg Speed Over Time"
            legend.isEnabled = false
        }


    }

    private fun subscribeToObservers(){
        viewModel.totalTimeRun.observe(viewLifecycleOwner, Observer {
            it?.let {
                val totalTimeRun = TrackingUtility.getFormattedStopWatchTime(it)
                val tvTotalTime = view?.findViewById<TextView>(R.id.tvTotalTime)
                tvTotalTime?.text = totalTimeRun
            }
        })

        viewModel.totalDistance.observe(viewLifecycleOwner, Observer {
            it?.let {
                val km = it/1000f
                val totalDistance = round(km * 10f) /10f // This is a safe way of getting the required decimal point
                val totalDistanceString = "${totalDistance}km"
                val tvTotalDistance = view?.findViewById<TextView>(R.id.tvTotalDistance)
                tvTotalDistance?.text = totalDistanceString

            }
        })

        viewModel.totalAvgSpeed.observe(viewLifecycleOwner, Observer {
            it?.let{
                val avgSpeed = round(it *10f) /10f
                val avgSpeedString = "${avgSpeed}km/h"
                val tvAvgSpeed = view?.findViewById<TextView>(R.id.tvAverageSpeed)
                tvAvgSpeed?.text = avgSpeedString
            }
        })

        viewModel.totalCaloriesBurned.observe(viewLifecycleOwner, Observer {
            it?.let {
                val totalCalories = "${it}kcal"
                val tvTotalCalories = view?.findViewById<TextView>(R.id.tvTotalCalories)
                tvTotalCalories?.text = totalCalories

            }
        })

        // then to add entries to our bar chart]
        viewModel.runsSortedByDate.observe(viewLifecycleOwner, Observer {
            it?.let {  // Note that the it here is a list of the runs sorted by date
                // This is the range that goes from 0 to the run list -1 (indices)
                // Note that map is used to go through a list
                // Note that when we use map, it returns counting from 0 to the size -1
                val allAvgSpeed = it.indices.map { i -> BarEntry(i.toFloat(), it[i].avgSpeedInKMH) }
                // This above means that we are checking through the runs and then we want the x axis to be the index and then the y axis to be the avgSpeedInKMH of that run at that particular index
                val bardataSet = BarDataSet(allAvgSpeed,"Avg Speed Over Time").apply {
                    // Then to modify how they look like
                    valueTextColor = Color.WHITE
                    color = ContextCompat.getColor(requireContext(),R.color.colorAccent)
                }
                // Then to set our data
                val barChart = view?.findViewById<BarChart>(R.id.barChart)
                barChart?.data = BarData(bardataSet)
                // then to set the marker view
                barChart?.marker = CustomMarkerView(it.reversed(),requireContext(),R.layout.marker_view) // Note that "it.reversed()" here will reverse the list and then make the last one to have the index of 0
                barChart?.invalidate()  // This is to update the bar chart without changing



            }
        })
    }
}