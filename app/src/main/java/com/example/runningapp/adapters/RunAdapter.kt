package com.example.runningapp.adapters

import android.icu.util.Calendar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.runningapp.R
import com.example.runningapp.db.Run
import com.example.runningapp.other.TrackingUtility
import java.text.SimpleDateFormat
import java.util.Locale

class RunAdapter : RecyclerView.Adapter<RunAdapter.RunViewHolder>() {

    inner class RunViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    // ListDiff
    // A list Diff is a tool that takes two list and calculates the difference between the two lists and then returns it
    // The list differ is very essential in the recycler view

    val diffCallback = object : DiffUtil.ItemCallback<Run>(){ // Note that we need to press Ctrl + I to get all these functions that are related to this DiffUtil
        override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean {
            // This is to check if the items are the same
            return  oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean {
            // This is to check if they have the same components like the same bitmap and things like that
            return oldItem.hashCode() == newItem.hashCode() // The same hashcode means that the components inside it must be the same like the average speed, bitmap image and things like that

        }
    }

    // To call the diff
    val differ = AsyncListDiffer(this,diffCallback) // Note that the "this" here is referring to the recycler view

    fun submitList(list : List<Run>) = differ.submitList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder {
        return RunViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_run,
                parent,
                false
            )
        )




    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {

        val run = differ.currentList[position] // This is used to set the position of the run

        holder.itemView.apply {
            Glide.with(this).load(run.img).into(holder.itemView.findViewById(R.id.ivRunImage))

            val calendar = Calendar.getInstance().apply {
                timeInMillis = run.timestamp
            }
            val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault()) // This is used to set the format in which we want to display the date

            val tvDate = holder.itemView.findViewById<TextView>(R.id.tvDate)
            tvDate.text = dateFormat.format(calendar.time) // Then to format the date from our calendar

            val avgSpeed = "${run.avgSpeedInKMH}km/h"
            val tvAvgSpeed = holder.itemView.findViewById<TextView>(R.id.tvAvgSpeed)
            tvAvgSpeed.text = avgSpeed

            val distanceInKm = "${run.distanceInMeters / 1000f}km"
            val tvDistance = holder.itemView.findViewById<TextView>(R.id.tvDistance)
            tvDistance.text = distanceInKm

            val tvTime = holder.itemView.findViewById<TextView>(R.id.tvTime)
            tvTime.text = TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)

            val caloriesBurned = "${run.caloriesBurned}kcal"
            val tvCalories = holder.itemView.findViewById<TextView>(R.id.tvCalories)
            tvCalories.text = caloriesBurned




        }
    }
}