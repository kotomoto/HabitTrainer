package com.koto.habittrainer.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.koto.habittrainer.Habit
import com.koto.habittrainer.db.HabitEntry.DESC_COL
import com.koto.habittrainer.db.HabitEntry.IMAGE_COL
import com.koto.habittrainer.db.HabitEntry.TABLE_NAME
import com.koto.habittrainer.db.HabitEntry.TITLE_COL
import com.koto.habittrainer.db.HabitEntry._ID
import java.io.ByteArrayOutputStream

class HabitDbTable(context: Context) {

    private val TAG = HabitDbTable::class.java.simpleName

    private val dbHelper = HabitTrainerDb(context)

    fun store(habit: Habit): Long {
        val db = dbHelper.writableDatabase

        val values = ContentValues()
        with(values) {
            put(TITLE_COL, habit.title)
            put(DESC_COL, habit.description)
            put(IMAGE_COL, toByteArray(habit.image))
        }

        val id = db.transaction { insert(HabitEntry.TABLE_NAME, null, values) }

        Log.d(TAG, "Stored new habit to the DB $habit")

        return id
    }

    fun readAllHabits(): List<Habit> {
        val columns = arrayOf(_ID, TITLE_COL, DESC_COL, IMAGE_COL)
        val db = dbHelper.readableDatabase
        val order = "$_ID ASC"
        val cursor = db.doQuery(TABLE_NAME, columns, orderBy = order)

        return parseHabitsFrom(cursor)
    }

    private fun parseHabitsFrom(cursor: Cursor): MutableList<Habit> {
        val habits = mutableListOf<Habit>()

        with(cursor) {
            while (moveToNext()) {
                val title = getString(TITLE_COL)
                val description = getString(DESC_COL)
                val bitmap = getBitmap(IMAGE_COL)
                habits.add(Habit(title, description, bitmap))
            }

            close()
        }

        return habits
    }
}

private fun toByteArray(bitmap: Bitmap): ByteArray {
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream)
    return stream.toByteArray()
}

private inline fun <T> SQLiteDatabase.transaction(function: SQLiteDatabase.() -> T): T {
    beginTransaction()
    val result = try {
        val returnValue = function()
        setTransactionSuccessful()

        returnValue
    } finally {
        endTransaction()
    }
    close()

    return result
}

private fun SQLiteDatabase.doQuery(table: String, columns: Array<String>, selection: String? = null, selectionArgs: Array<String>? = null,
                                   groupBy: String? = null, having: String? = null, orderBy: String? = null): Cursor {
    return query(table, columns, selection, selectionArgs, groupBy, having, orderBy)
}

private fun Cursor.getString(columnName: String) = getString(getColumnIndex(columnName))

private fun Cursor.getBitmap(columnName: String): Bitmap {
    val byteArray = getBlob(getColumnIndex(columnName))
    return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
}