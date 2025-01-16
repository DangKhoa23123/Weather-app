package com.example.a1113

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class CalendarActivity : AppCompatActivity() {
    private lateinit var calendarView: CalendarView
    private lateinit var noteEditText: EditText
    private lateinit var saveButton: Button
    private val userId = "user123"
    private var selectedDate: String = "" // Biến lưu trữ ngày đã chọn

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.date)

        calendarView = findViewById(R.id.calendarView)
        noteEditText = findViewById(R.id.noteEditText)
        saveButton = findViewById(R.id.saveButton)

        // Lắng nghe sự kiện thay đổi ngày trên CalendarView
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = "$year-${month + 1}-$dayOfMonth" // Cập nhật ngày đã chọn
            loadNoteForDate(selectedDate)
        }

        // Lưu ghi chú khi nhấn nút Save
        saveButton.setOnClickListener {
            saveNoteForDate(selectedDate) // Sử dụng ngày đã chọn để lưu
        }
    }

    // Hàm lấy ngày đã chọn từ CalendarView
    private fun getSelectedDate(): String {
        // Nếu selectedDate không rỗng thì dùng ngay, nếu không sẽ lấy ngày hiện tại
        return if (selectedDate.isNotEmpty()) {
            selectedDate
        } else {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = calendarView.date
            return "${calendar.get(Calendar.YEAR)}-" +
                    "${calendar.get(Calendar.MONTH) + 1}-" +
                    calendar.get(Calendar.DAY_OF_MONTH)
        }
    }

    // Tải ghi chú cho ngày đã chọn từ API
    private fun loadNoteForDate(date: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.apiService.getNote(date, userId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        // Cập nhật giao diện với nội dung ghi chú
                        noteEditText.setText(response.body()!!.content)
                    } else {
                        // Nếu không tìm thấy ghi chú, xóa nội dung và thông báo
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

    // Lưu ghi chú cho ngày đã chọn
    private fun saveNoteForDate(date: String) {
        val note = Note(
            date = date,
            content = noteEditText.text.toString(),
            userId = userId
        )

        lifecycleScope.launch(Dispatchers.IO) {
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
