package com.example.runningapp.db

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.io.ByteArrayOutputStream

class Converters {
    // We are trying to convert the bitmap to a byte array that the data base will understand.
    // Also note that the byte array is in form of 0s and 1s that the data base understands
    @TypeConverter
    fun toBitmap (bytes: ByteArray) : Bitmap{ // To convert the byte array to bitmap
        return BitmapFactory.decodeByteArray(bytes,0, bytes.size)
    }

    @TypeConverter // This is the annotation that tells that this function is a type converter
    fun fromBitmap(bmp: Bitmap) : ByteArray{ // This is to convert the bitmap to byte array
        val outputStream = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG,100,outputStream) // Note this will be stored in the "outputStream"
        return outputStream.toByteArray()

    }
}