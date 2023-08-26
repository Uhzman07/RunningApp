package com.example.runningapp.di

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.runningapp.R
import com.example.runningapp.other.Constants
import com.example.runningapp.ui.theme.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

// Note that we should install this in the service component because they generally exist when the service still exists generally and even when the app is closed

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    @ServiceScoped // This means that for the life time of our service, there is only going to be an instance of the "FusedLocationProviderClient"
    @Provides
    fun provideFusedLocationProviderClient(
        @ApplicationContext app : Context // This is always our context when using dagger hilt instead of using "this"
    ) = LocationServices.getFusedLocationProviderClient(app)


    @ServiceScoped
    @Provides
    // This is to get an intent from the main activity // This is because we are trying to set the intent of the notification and then the notification is started in the main activity
    fun provideMainActivityPendingIntent(
        @ApplicationContext app : Context
    ) = PendingIntent.getActivity(
        app,
        0,
        Intent(app, MainActivity::class.java).also {
            it.action = Constants.ACTION_SHOW_TRACKING_FRAGMENT
        },
        PendingIntent.FLAG_MUTABLE // This will update our pending intent instead of recreating it
    )

    @ServiceScoped
    @Provides
    fun provideBaseNotificationBuilder(
        @ApplicationContext app : Context,
        pendingIntent: PendingIntent // Note that this is an instance of the pending intent that we had created above
    ) =  NotificationCompat.Builder(app, Constants.NOTIFICATION_CHANNEL_ID)
        .setAutoCancel(false) // This is to allow the notification to be always active
        .setOngoing(true)
        .setSmallIcon(R.drawable.ic_directions_run_black_24dp)
        .setContentTitle("Running App")
        .setContentText("00::00::00")
        .setContentIntent(pendingIntent)
}