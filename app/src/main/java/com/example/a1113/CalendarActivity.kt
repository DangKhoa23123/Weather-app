package com.example.a1113

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class CalendarActivity : AppCompatActivity() {
    private lateinit var calendarView: CalendarView
    private lateinit var noteEditText: EditText
    private lateinit var saveButton: Button
    private val userId = "user123"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.date)

        calendarView = findViewById(R.id.calendarView)
        noteEditText = findViewById(R.id.noteEditText)
        saveButton = findViewById(R.id.saveButton)

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val date = "$year-${month + 1}-$dayOfMonth"
            loadNoteForDate(date)
        }

        saveButton.setOnClickListener {
            saveNoteForDate(getSelectedDate())
        }
    }

    private fun getSelectedDate(): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = calendarView.date
        return "${calendar.get(Calendar.YEAR)}-" +
                "${calendar.get(Calendar.MONTH) + 1}-" +
                calendar.get(Calendar.DAY_OF_MONTH)
    }

    private fun loadNoteForDate(date: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.getNote(date, userId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        noteEditText.setText(response.body()!!.content)
                    } else {
                        noteEditText.setText("")
                        Toast.makeText(
                            this@CalendarActivity,
                            "No note found",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("CalendarActivity", "Error loading note", e)
                    Toast.makeText(
                        this@CalendarActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun saveNoteForDate(date: String) {
        val note = Note(
            date = date,
            content = noteEditText.text.toString(),
            userId = userId
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.saveNote(note)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@CalendarActivity,
                        if (response.isSuccessful) "Note saved" else "Error saving note",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d("API Response", "Code: ${response.code()}, Body: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("CalendarActivity", "Error saving note", e)
                    Toast.makeText(
                        this@CalendarActivity,
                        "Connection error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}