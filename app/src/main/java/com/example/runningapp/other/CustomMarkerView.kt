package com.example.runningapp.other

import android.content.Context
import android.icu.util.Calendar
import android.widget.TextView
import com.example.runningapp.R
import com.example.runningapp.db.Run
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import java.text.SimpleDateFormat
import java.util.Locale


// Note that the marker view is the one that quickly updates the info of a particular point when the using is hovering on it or something
class CustomMarkerView(
    val runs : List<Run>,
    c: Context,
    layoutId: Int
) : MarkerView(c,layoutId){
    // To get the points for positioning
    override fun getOffset(): MPPointF {
        //return super.getOffset()
        return MPPointF(-width/2f, -height.toFloat()) // Note that these values of width and height were got from the the android documentation
    }



    // This is a function that we can easily generate ourself
    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        super.refreshContent(e, highlight)
        if(e == null){
            return
        }
        val curRunId = e.x.toInt() // this is the id of that run
        val run = runs[curRunId]


        val calendar = Calendar.getInstance().apply {
            timeInMillis = run.timestamp
        }
        val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault()) // This is used to set the format in which we want to display the date

        val tvDate = findViewById<TextView>(R.id.tvDate)
        tvDate.text = dateFormat.format(calendar.time) // Then to format the date from our calendar

        val avgSpeed = "${run.avgSpeedInKMH}km/h"
        val tvAvgSpeed =findViewById<TextView>(R.id.tvAvgSpeed)
        tvAvgSpeed.text = avgSpeed

        val distanceInKm = "${run.distanceInMeters / 1000f}km"
        val tvDistance = findViewById<TextView>(R.id.tvDistance)
        tvDistance.text = distanceInKm

        val tvDuration = findViewById<TextView>(R.id.tvDuration)
        tvDuration.text = TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)

        val caloriesBurned = "${run.caloriesBurned}kcal"
        val tvCaloriesBurned = findViewById<TextView>(R.id.tvCaloriesBurned)
        tvCaloriesBurned.text = caloriesBurned
    }
}