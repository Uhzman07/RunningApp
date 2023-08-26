package com.example.runningapp.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import com.example.runningapp.db.RunningDatabase
import com.example.runningapp.other.Constants.KEY_FIRST_TIME_TOGGLE
import com.example.runningapp.other.Constants.KEY_NAME
import com.example.runningapp.other.Constants.KEY_WEIGHT
import com.example.runningapp.other.Constants.RUNNING_DATABASE_NAME
import com.example.runningapp.other.Constants.SHARED_PREFERENCES_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
//import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.internal.managers.ApplicationComponentManager
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// This is to provide an instance of an application and then install the database in it

@Module
@InstallIn(SingletonComponent::class) // This is referring to the entire application
//@InstallIn(ActivityComponent::class)// This means that this "AppModule" below will only be installed in the activity component
// This means that the dependency of this "AppModule" will only be available during the life time of this activity
// Note that we also have "ServiceComponent"
// Also "ApplicationComponent"
// Also "FragmentComponent"
object AppModule {

    @Singleton // This means that each class in our app that needs the running database that is created using the function below gets the same instance and not multiple instances
    @Provides // This is used to tell dagger that the result can be used to create other dependencies and can also be used to inject in our classes
    fun provideRunningDatabase( // This is used to create the data base for dagger
        @ApplicationContext app : Context // Here we are providing the context and we need to show that using dagger hilt with the annotation
    ) = Room.databaseBuilder(
        app, // This is the context
        RunningDatabase :: class.java, // This our created data base instance
        RUNNING_DATABASE_NAME

    ).build()

    // To create the Data object
    // Then to create the interface for the data base
    @Singleton
    @Provides
    fun provideRunDao(db : RunningDatabase) = db.getRunDao() // The is used to the RunDAO object

    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext app:Context) =
        app.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)
        // Mode private means that only our app is allowed to read from it

    @Singleton
    @Provides
    fun provideName(sharedPref:SharedPreferences) = sharedPref.getString(KEY_NAME,"")?: "" // The empty string means that we return an empty string if the name does not exist

    @Singleton
    @Provides
    fun provideWeight(sharedPref:SharedPreferences) = sharedPref.getFloat(KEY_WEIGHT,80f) // Note that 80f here is the default weight that we had set

    @Singleton
    @Provides
    fun provideFirstTimeToggle(sharedPref:SharedPreferences) = sharedPref.getBoolean(
        KEY_FIRST_TIME_TOGGLE,true)

















    // Note
    // We do not have to call the function as dagger will call it for us
    // Then we have to go the manifest file to make change the name of our application to ".BaseApplication"







}