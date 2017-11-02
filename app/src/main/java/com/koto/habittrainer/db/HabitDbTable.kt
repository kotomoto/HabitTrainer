package com.koto.habittrainer.db

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.koto.habittrainer.Habit
import java.io.ByteArrayOutputStream

class HabitDbTable(context: Context) {

    private val TAG = HabitDbTable::class.java.simpleName

    private val dbHelper = HabitTrainerDb(context)

    fun store(habit: Habit): Long {
        val db = dbHelper.writableDatabase

        val values = ContentValues()
        values.put(HabitEntry.TITLE_COL, habit.title)
        values.put(HabitEntry.DESC_COL, habit.description)
        values.put(HabitEntry.IMAGE_COL, toByteArray(habit.image))

        db.beginTransaction()
        val id = try {
            val returnValue = db.insert(HabitEntry.TABLE_NAME, null, values)
            db.setTransactionSuccessful()
            returnValue
        } finally {
            db.endTransaction()
        }

        db.close()

        Log.d(TAG, "Stored new habit to the DB $habit")

        return id
    }

    private fun toByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream)
        return stream.toByteArray()
    }
}
