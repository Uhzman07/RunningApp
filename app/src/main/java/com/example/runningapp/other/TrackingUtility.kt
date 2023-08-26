package com.example.runningapp.other

import android.content.Context
import android.location.Location
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.runningapp.services.Polyline
//import com.example.runningapp.Manifest
import pub.devrel.easypermissions.EasyPermissions
import java.util.concurrent.TimeUnit

// This is to check if the user has accepted the permission
object TrackingUtility {

    fun hasLocationPermissions(context: Context) =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) { // This is because Android Q amd below only needs access to the fine and coarse location only
            // Note that "EasyPermissions" is used to check if it has permissions
            EasyPermissions.hasPermissions(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION, // Note that we should always make use of the android manifest
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )

        } else { // This is to check if it is android Q and above
            EasyPermissions.hasPermissions(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION, // Note that we should always make use of the android manifest
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION,// Note that android Q and above will require to check this one

            )

        }

    fun calculatePolylineLength(polyline: Polyline) : Float{
        var distance = 0f
        for(i in 0..polyline.size-2){
            val pos1 = polyline[i]
            val pos2 = polyline[i+1]

            val result = FloatArray(1) // We are creating a float array variable of size 1

            Location.distanceBetween(
                pos1.latitude,
                pos1.longitude,
                pos2.latitude,
                pos2.longitude,
                result // This is where we want to store it

            )
            distance += result[0]
        }
        return distance

    }

    fun getFormattedStopWatchTime(ms:Long, includeMillis:Boolean = false) : String{
        var milliseconds = ms
        // To derive the hours
        val hours = TimeUnit.MILLISECONDS.toHours(milliseconds) // This will convert our milliseconds to hours

        // To derive the minutes from the hours
        milliseconds -= TimeUnit.HOURS.toMillis(hours) // This will give us the remaining minutes after taking out the hours
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)

        // To then get the seconds
        milliseconds -= TimeUnit.MINUTES.toMillis(minutes)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds)

        if(!includeMillis){
            return "${if(hours<10) "0" else ""}$hours:" +
                    "${if(minutes < 10) "0" else ""}$minutes:" +
                    "${if(seconds<10) "0" else ""}$seconds"
        }
        milliseconds -= TimeUnit.SECONDS.toMillis(seconds)
        milliseconds/=10
        return "${if(hours<10) "0" else ""}$hours:" +
                "${if(minutes < 10) "0" else ""}$minutes:" +
                "${if(seconds<10) "0" else ""}$seconds:" +
                "${if(milliseconds <10) "0" else ""}$milliseconds"



    }




}