package com.example.runningapp.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_DEFAULT
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationRequest
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.runningapp.R
import com.example.runningapp.other.Constants.ACTION_PAUSE_SERVICE
import com.example.runningapp.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.example.runningapp.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runningapp.other.Constants.ACTION_STOP_SERVICE
import com.example.runningapp.other.Constants.FASTEST_LOCATION_INTERVAL
import com.example.runningapp.other.Constants.LOCATION_UPDATE_INTERVAL
import com.example.runningapp.other.Constants.NOTIFICATION_CHANNEL_ID
import com.example.runningapp.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.runningapp.other.Constants.NOTIFICATION_ID
import com.example.runningapp.other.Constants.TIMER_UPDATE_INTERVAL
import com.example.runningapp.other.TrackingUtility
import com.example.runningapp.ui.theme.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.model.LatLng
import timber.log.Timber
import com.google.android.gms.location.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

// Note that we always have to add the foreground services and foreground services location to the manifest
typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

@Suppress("DEPRECATION")
@AndroidEntryPoint

class TrackingService : LifecycleService() { // This is inheriting from the lifecycle service because we are trying to get some live data from this service
    // We will communicate from the service to the activity or the fragment using intent

    private var isFirstRun = true
    var serviceKilled = false

    @Inject // This is used to inject the "FusedLocationProviderClient" from the Service Module using dagger hilt
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private var timeRunInSeconds = MutableLiveData<Long>()

    @Inject
    lateinit var baseNotificationBuilder : NotificationCompat.Builder // Then we can now remove our notification builder

    lateinit var curNotificationBuilder: NotificationCompat.Builder // This will be our current notification builder


    companion object{
        val timeRunInMillis = MutableLiveData<Long>()
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<Polylines>() // This right here is used to store lists of lists of map coordinates and then "LatLng" here is the data type of coordinates (Longitudinal points)

        /*
        // For the above,
        The first MutableList is a collection of longitudinal points that is a single line during a simple run()
         */
    }

    private fun postInitialValues(){
        // Note that we have to initialize all here
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf()) // This is used to post the initials
        timeRunInSeconds.postValue(0L)
        timeRunInMillis.postValue(0L)
    }

    // This is for the location provider client
    override fun onCreate() {
        super.onCreate()

        curNotificationBuilder = baseNotificationBuilder  // We are assigning the current notification builder to the base notification builder initially

        postInitialValues()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        isTracking.observe(this, Observer { //  This is used to take a record of the changes that are occuring in the to isTracking
            updateLocationTracking(it)
            updateNotificationTrackingState(it)
        })
    }

    private fun killService(){
        serviceKilled = true
        isFirstRun = true
        pauseService()
        postInitialValues() // This is to reset all the values
        stopForeground(true)  // This will cancel all the notifications and all
        stopSelf() // This will basically kill the whole service
    }

    // This gets called whenever we send an intent to our service
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Then to check the type of intent that had been sent to the service
        intent?.let {
            when(it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if(isFirstRun){
                        //Timber.d("New service")
                        startForegroundService()
                        isFirstRun = false
                    }else{
                        Timber.d("Started or resumed service")
                        //startForegroundService()
                        startTimer() // Note that this will also start immediately since "isFirstRun" is contradicted immediately

                    }

                }
                ACTION_PAUSE_SERVICE -> {
                    pauseService() // This is called when the user sends that command to the service
                    Timber.d("Paused service")
                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("Stopped service")
                    killService()
                }


            }
        }
        return super.onStartCommand(intent, flags, startId)
    }
    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L // This is the total time spent
    private var timeStarted = 0L
    private var lastSecondTimeStamp = 0L

    private fun startTimer(){
        addEmptyPolyline()
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true
        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!){
                // time difference between now and timeStarted
                lapTime = System.currentTimeMillis() - timeStarted

                // post new lap time
                timeRunInMillis.postValue(timeRun + lapTime)

                if(timeRunInMillis.value!! >= lastSecondTimeStamp +1000L){ // Note that the "lastSecondTimeStamp" is used to check if a second has passed by and then it will then save it in the actual timer in seconds
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1) // Then we increase our time in seconds by one
                    lastSecondTimeStamp += 1000L // Then update the "lastSecondTimeStamp" again


                }
                delay(TIMER_UPDATE_INTERVAL) // This is the interval at which we want to update our timer


            }
            timeRun += lapTime // This is when we are not tracking any more (just in case we want to continue again)
        }
    }


    private fun pauseService(){
        isTracking.postValue(false)
        isTimerEnabled = false
    }


    private fun updateNotificationTrackingState(isTracking: Boolean){
        val notificationActionText = if(isTracking) "Pause" else "Resume"
        val pendingIntent = if(isTracking){
            val pauseIntent = Intent(this,TrackingService::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this,1,pauseIntent, FLAG_MUTABLE)

        } else {
            val resumeIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_START_OR_RESUME_SERVICE
            }
            PendingIntent.getService(this, 2, resumeIntent, FLAG_MUTABLE)
        }
        // Then we need to create our notification manager
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Then to modify our notification builder
        // Then we need to remove all the actions from our notification builder
        // This will then make our "curNotification" to be like an empty form of our notification builder
        curNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(curNotificationBuilder,ArrayList<NotificationCompat.Action>()) // This is used to set the set "curNotificationBuilder" to have an empty action
        }

        if(!serviceKilled){
            // Then we now set the new actions for our "curNotificationBuilder"
            curNotificationBuilder = baseNotificationBuilder
                .addAction(R.drawable.ic_pause_black_24dp,notificationActionText,pendingIntent)
            notificationManager.notify(NOTIFICATION_ID,curNotificationBuilder.build())

        }





    }


    //@RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("MissingPermission", "NewApi")
    private fun updateLocationTracking(isTracking : Boolean){
        if(isTracking){
            if(TrackingUtility.hasLocationPermissions(this)){
                val request = LocationRequest().apply {
                    interval = LOCATION_UPDATE_INTERVAL // This is the time interval that we want to be requesting the location
                    fastestInterval = FASTEST_LOCATION_INTERVAL // This is the fastest time it is allowed to request location
                    priority = PRIORITY_HIGH_ACCURACY // This is to say that we want high accuracy


                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }

        }else{
            fusedLocationProviderClient.removeLocationUpdates(locationCallback) // This is when it is not tracking
        }
    }
    val locationCallback = object :LocationCallback(){
        // We press shift + o to get this function below
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            if(isTracking.value!!){ // This is used to simply perform a nullable check on isTracking even if we know that it cannot be null
                result?.locations?.let { locations -> // Note that "locations" here is used to represent the locations that we derive from our results
                    for(location in locations){
                        addPathPoint(location) // Note that we are then adding every new location to our last polyline
                        Timber.d("NEW LOCATION : ${location.latitude}, ${location.longitude}")
                    }
                }

            }
        }
    }
    private fun addPathPoint(location: Location?){
        location?.let{
            val pos = LatLng(location.latitude,location.longitude) // Note that we can easily get the latitude and longitude through our location data type
            pathPoints.value?.apply {
                last().add(pos) // Note that "last()" here is like the last point in the "pathPoints", that is we are trying to add the last position to the value
                pathPoints.postValue(this)
            }
        }
    }

    // To then add to the our MutableLiveData List
    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf()) // This is a form of initialization for the polylines
        pathPoints.postValue(this) // Note that this here is used to add the polylines just as we did in the previous initialization
    } ?: pathPoints.postValue(mutableListOf(mutableListOf())) // This is used to check if it is empty and then we want to add the initial polylines to the list of that live data

    private fun startForegroundService(){
        startTimer()



        isTracking.postValue(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager // This is used to create the notification locally here

        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
            createNotificationChannel(notificationManager)
        }
        // Note that we have created the notification builder using dagger hilt and then injected it
        startForeground(NOTIFICATION_ID,baseNotificationBuilder.build()) // Note that the id must be set to 1 in order to avoid crashing
        timeRunInSeconds.observe(this, Observer {
            if(!serviceKilled){
                val notification = curNotificationBuilder
                    .setContentText(TrackingUtility.getFormattedStopWatchTime(it * 1000L)) // We are multiplying by 1000L because we want to change from second to milli second and then we do not need the other boolean value because we do not need the milli second time
                notificationManager.notify(NOTIFICATION_ID,notification.build())

            }

        }
        )






    }
    // Note that we also removed the pendingIntent because we had already stated that in the "ServiceModule" then it is injected




    @RequiresApi(Build.VERSION_CODES.O) // note that this is generated automatically
    private fun createNotificationChannel(notificationManager: NotificationManager){
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW // Note that we had set the importance as low because we do not want it to make a sound everytime
        )
        notificationManager.createNotificationChannel(channel)

    }




    // Special note
    /*
    We have the foreground service and the background service
    - The fore ground service usually comes with a notification, it cannot be killed by the android studio in case of memory shortage or something like that
    - The background service can be killed by the android system so it is not really advisable to make use of it
    - Initially, our service is a background service so what we need to do is to launch it as a fore ground service
     */
}