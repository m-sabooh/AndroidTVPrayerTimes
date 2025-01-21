package com.example.myapplication

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

class MainActivity : FragmentActivity() {

    private lateinit var clockTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var hijriDateTextView: TextView
    private lateinit var nextPrayerTextView: TextView
    private lateinit var countdownTextView: TextView
    private lateinit var locationTextView: TextView
    private lateinit var prayerTimesContainer: LinearLayout

    private val handler = Handler(Looper.getMainLooper())
    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            updateTime()
            handler.postDelayed(this, 1000)
        }
    }

    // Mock prayer times (replace with actual data in production)
    private val prayerTimes = mapOf(
        "Fajr" to "04:30",
        "Sunrise" to "05:45",
        "Dhuhr" to "12:30",
        "Asr" to "15:45",
        "Maghrib" to "18:15",
        "Isha" to "19:45"
    )

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

        locationTextView.text = "Dubai, United Arab Emirates"
        hijriDateTextView.text = "15 Ramadan 1444" // Replace with actual Hijri date calculation

        setupPrayerTimeViews()
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
        val currentTimeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(currentTime.time)
        val nextPrayer = prayerTimes.entries.find { it.value > currentTimeStr }?.key ?: prayerTimes.keys.first()

        nextPrayerTextView.text = "Next Prayer: $nextPrayer"
        updateCountdown(currentTime, nextPrayer)
        updatePrayerTimeProgress()
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
            val progressView = prayerTimeView.findViewById<View>(R.id.progressView)

            prayerNameTextView.text = prayer
            prayerTimeTextView.text = time

            prayerTimeView.tag = progressView
            prayerTimesContainer.addView(prayerTimeView)
        }
    }

    private fun updatePrayerTimeProgress() {
        val currentTime = Calendar.getInstance()
        val currentTimeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(currentTime.time)

        prayerTimes.entries.forEachIndexed { index, (prayer, time) ->
            val prayerTimeView = prayerTimesContainer.getChildAt(index)
            val progressView = prayerTimeView.tag as View
            val progress = calculateProgress(currentTimeStr, time, prayerTimes.values.elementAtOrNull(index + 1) ?: prayerTimes.values.first())
            val layoutParams = progressView.layoutParams
            layoutParams.width = (progress * prayerTimeView.width).toInt()
            progressView.layoutParams = layoutParams
        }
    }

    private fun calculateProgress(currentTime: String, startTime: String, endTime: String): Float {
        val current = timeToMinutes(currentTime)
        val start = timeToMinutes(startTime)
        var end = timeToMinutes(endTime)

        if (end <= start) {
            end += 24 * 60 // Add 24 hours if the end time is on the next day
        }

        if (current < start) return 0f
        if (current >= end) return 1f

        return min((current - start).toFloat() / (end - start), 1f)
    }

    private fun timeToMinutes(time: String): Int {
        val (hours, minutes) = time.split(":").map { it.toInt() }
        return hours * 60 + minutes
    }
}