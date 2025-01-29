package com.example.myapplication

import PrayerTimesApi
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale


class MainActivity : FragmentActivity() {

    private lateinit var clockTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var hijriDateTextView: TextView
    private lateinit var nextPrayerTextView: TextView
    private lateinit var countdownTextView: TextView
    private lateinit var locationTextView: TextView
    private lateinit var prayerTimesContainer: LinearLayout

    private val prayerTimes = mutableMapOf<String, String>()

    private val handler = Handler(Looper.getMainLooper())
    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            updateTime()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        clockTextView = findViewById(R.id.clockTextView)
        dateTextView = findViewById(R.id.dateTextView)
        hijriDateTextView = findViewById(R.id.hijriDateTextView)
        nextPrayerTextView = findViewById(R.id.nextPrayerTextView)
        countdownTextView = findViewById(R.id.countdownTextView)
        locationTextView = findViewById(R.id.locationTextView)
        prayerTimesContainer = findViewById(R.id.prayerTimesContainer)

        locationTextView.text = "Mezitli/Mersin, Türkiye"


        // Get the current date in Hijri (Islamic) calendar format
        val hijrahDate = HijrahDate.now()
        // Create a formatter for the Hijri date
        val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("en"))
        // Format the Hijri date using the formatter
        val formattedHijriDate = hijrahDate.format(formatter)
        // Display the Hijri date in the TextView
        hijriDateTextView.text = formattedHijriDate

        fetchPrayerTimes("Mezitli", "Turkey")
        //setupPrayerTimeViews()

    }

    override fun onResume() {
        super.onResume()
        handler.post(updateTimeRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateTimeRunnable)
    }

    private fun updateTime() {
        val currentTime = Calendar.getInstance()
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        clockTextView.text = sdf.format(currentTime.time)

        val dateSdf = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
        dateTextView.text = dateSdf.format(currentTime.time)

        updateNextPrayer(currentTime)
    }

    private fun updateNextPrayer(currentTime: Calendar) {
        if (prayerTimes.isEmpty()) return // Skip if prayer times haven't been fetched yet

        val currentTimeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(currentTime.time)
        val nextPrayer = prayerTimes.entries.find { it.value > currentTimeStr }?.key ?: prayerTimes.keys.first()

        nextPrayerTextView.text = "Next Prayer: $nextPrayer"
        updateCountdown(currentTime, nextPrayer)
        highlightNextPrayer(nextPrayer)
    }

    private fun updateCountdown(currentTime: Calendar, nextPrayer: String) {
        val nextPrayerTime = Calendar.getInstance()
        val (hours, minutes) = prayerTimes[nextPrayer]!!.split(":").map { it.toInt() }
        nextPrayerTime.set(Calendar.HOUR_OF_DAY, hours)
        nextPrayerTime.set(Calendar.MINUTE, minutes)
        nextPrayerTime.set(Calendar.SECOND, 0)

        if (nextPrayerTime.before(currentTime)) {
            nextPrayerTime.add(Calendar.DAY_OF_MONTH, 1)
        }

        val diff = nextPrayerTime.timeInMillis - currentTime.timeInMillis
        val hourss = diff / (60 * 60 * 1000)
        val minutess = (diff / (60 * 1000)) % 60
        val seconds = (diff / 1000) % 60

        countdownTextView.text = String.format("%02d:%02d:%02d", hourss, minutess, seconds)
    }

    private fun setupPrayerTimeViews() {
        val inflater = LayoutInflater.from(this)
        prayerTimes.forEach { (prayer, time) ->
            val prayerTimeView = inflater.inflate(R.layout.prayer_time_item, prayerTimesContainer, false)
            val prayerNameTextView = prayerTimeView.findViewById<TextView>(R.id.prayerNameTextView)
            val prayerTimeTextView = prayerTimeView.findViewById<TextView>(R.id.prayerTimeTextView)
            val cardView = prayerTimeView.findViewById<CardView>(R.id.prayerTimeCardView)

            prayerNameTextView.text = prayer
            prayerTimeTextView.text = time

            prayerTimeView.tag = cardView
            prayerTimesContainer.addView(prayerTimeView)
        }
    }

    private fun highlightNextPrayer(nextPrayer: String) {
        prayerTimes.keys.forEachIndexed { index, prayer ->
            val prayerTimeView = prayerTimesContainer.getChildAt(index)
            val cardView = prayerTimeView.tag as CardView

            if (prayer == nextPrayer) {
                cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.next_prayer_highlight))
            } else {
                cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.prayer_time_background))
            }
        }
    }

    private fun fetchPrayerTimes(city: String, country: String) {
        val api = RetrofitClient.getInstance().create(PrayerTimesApi::class.java)
        val call = api.getPrayerTimes(city, country)

        call.enqueue(object : Callback<PrayerTimesResponse> {
            override fun onResponse(
                call: Call<PrayerTimesResponse>,
                response: Response<PrayerTimesResponse>
            ) {
                if (response.isSuccessful) {
                    val fetchedPrayerTimes = response.body()?.data?.timings
                    fetchedPrayerTimes?.let {
                        prayerTimes["Fajr"] = it.Fajr
                        prayerTimes["Sunrise"] = it.Sunrise
                        prayerTimes["Dhuhr"] = it.Dhuhr
                        prayerTimes["Asr"] = it.Asr
                        prayerTimes["Maghrib"] = it.Maghrib
                        prayerTimes["Isha"] = it.Isha

                    }
                    setupPrayerTimeViews()
                }
            }

            override fun onFailure(call: Call<PrayerTimesResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Failed to fetch prayer times", Toast.LENGTH_SHORT).show()
            }
        })
    }

}