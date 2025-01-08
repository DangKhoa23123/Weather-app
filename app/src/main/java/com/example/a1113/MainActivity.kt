package com.example.a1113

import android.os.Bundle
import android.view.View
import android.widget.GridLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private val CITY: String = "dhaka,bd"
    private val API: String = "6042bb13c018872eaf05048bf14864dd"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fetchWeatherData()
    }

    private fun fetchWeatherData() {
        // Hiển thị loader và ẩn nội dung chính
        findViewById<ProgressBar>(R.id.loader).visibility = View.VISIBLE
        findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.GONE
        findViewById<TextView>(R.id.errorText).visibility = View.GONE

        CoroutineScope(Dispatchers.IO).launch {
            val response = try {
                URL("https://api.openweathermap.org/data/2.5/weather?q=$CITY&units=metric&appid=$API")
                    .readText(Charsets.UTF_8)
            } catch (e: Exception) {
                null
            }

            withContext(Dispatchers.Main) {
                if (response != null) {
                    try {
                        updateUI(JSONObject(response))
                    } catch (e: Exception) {
                        showError()
                    }
                } else {
                    showError()
                }
            }
        }
    }

    private fun updateUI(jsonObj: JSONObject) {
        val main = jsonObj.getJSONObject("main")
        val sys = jsonObj.getJSONObject("sys")
        val wind = jsonObj.getJSONObject("wind")
        val weather = jsonObj.getJSONArray("weather").getJSONObject(0)

        val updatedAt: Long = jsonObj.getLong("dt")
        val updatedAtText = SimpleDateFormat("dd MMM yyyy - hh:mm a", Locale.ENGLISH).format(Date(updatedAt * 1000))

        val temp = main.getString("temp") + "°C"
        val tempMin = "Nhiệt độ tối thiểu: " + main.getString("temp_min") + "°C"
        val tempMax = "Nhiệt độ tối đa: " + main.getString("temp_max") + "°C"
        val pressure = main.getString("pressure")
        val humidity = main.getString("humidity")

        val sunrise: Long = sys.getLong("sunrise")
        val sunset: Long = sys.getLong("sunset")
        val windSpeed = wind.getString("speed")
        val weatherDescription = weather.getString("description")

        val address = jsonObj.getString("name") + ", " + sys.getString("country")

        // Cập nhật giao diện
        findViewById<TextView>(R.id.address).text = address
        findViewById<TextView>(R.id.updated_at).text = updatedAtText
        findViewById<TextView>(R.id.status).text = weatherDescription.replaceFirstChar { it.uppercase() }
        findViewById<TextView>(R.id.temp).text = temp
        findViewById<TextView>(R.id.temp_min).text = tempMin
        findViewById<TextView>(R.id.temp_max).text = tempMax
        findViewById<TextView>(R.id.sunrise).text = "Sunrise\n" + SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunrise * 1000))
        findViewById<TextView>(R.id.sunset).text = "Sunset\n" + SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunset * 1000))
        findViewById<TextView>(R.id.wind).text = "Gió\n" + windSpeed + " km/h"
        findViewById<TextView>(R.id.pressure).text = "Áp suất\n" + pressure + " hPa"
        findViewById<TextView>(R.id.humidity).text = "Độ ẩm\n" + humidity + "%"

        // Hiển thị nội dung chính và ẩn loader
        findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
        findViewById<GridLayout>(R.id.mainContainer).visibility = View.VISIBLE
    }

    private fun showError() {
        findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
        findViewById<TextView>(R.id.errorText).visibility = View.VISIBLE
    }
}
