package com.example.runningapp.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

// This is the class that will represent our database itself
// We need to annotate to show that it is our database
@Database(
    entities = [Run::class], // This is the entity "run" that is to belong in our database
    version = 1
)
// Then to get the converters that we need to use
//@TypeConverters(Converters::class)
@TypeConverters(Converters::class)
abstract class RunningDatabase : RoomDatabase() { // This is to represent our database

    // To get our Interface
    abstract fun getRunDao(): RunDAO // This is expected to return the interface "RunDAO"
}
























/*
Dagger
A dependency is a kotlin object or variable that is another object is depending on it
Note that dagger helps to create the room database instance automatically so we do not have to do it again
all we have to do is to add the annotation "@Inject"

Dagger can also be used to define the number of times or the times that we need a particular object
For example, if we want an object to only be used in the log in and things like that, it is used with scoping normally

Note that Dagger injects the dependency at compile time

Also note that we are making use of dagger hilt
Then we need to add the dependency for dagger hilt

 */