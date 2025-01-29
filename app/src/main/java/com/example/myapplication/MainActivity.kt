package com.example.myapplication

import PrayerTimesApi
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.util.Log


class MainActivity : FragmentActivity() {

    private lateinit var clockTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var hijriDateTextView: TextView
    private lateinit var nextPrayerTextView: TextView
    private lateinit var countdownTextView: TextView
    private lateinit var locationTextView: TextView
    private lateinit var prayerTimesContainer: LinearLayout
//    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val handler = Handler(Looper.getMainLooper())
    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            updateTime()
            handler.postDelayed(this, 1000)
        }
    }

    // Mock prayer times (replace with actual data in production)
//    private val prayerTimes = mapOf(
//        "Fajr" to "04:30",
//        "Sunrise" to "05:45",
//        "Dhuhr" to "12:30",
//        "Asr" to "15:45",
//        "Maghrib" to "18:15",
//        "Isha" to "19:45"
//    )

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

//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fetchPrayerTimes("Mezitli", "Turkey")

//        setupPrayerTimeViews()
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

//        updateNextPrayer(currentTime)
    }

//    private fun updateNextPrayer(currentTime: Calendar) {
//        val currentTimeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(currentTime.time)
//        val nextPrayer = prayerTimes.entries.find { it.value > currentTimeStr }?.key ?: prayerTimes.keys.first()
//
//        nextPrayerTextView.text = "Next Prayer: $nextPrayer"
//        updateCountdown(currentTime, nextPrayer)
//        highlightNextPrayer(nextPrayer)
//    }

//    private fun updateCountdown(currentTime: Calendar, nextPrayer: String) {
//        val nextPrayerTime = Calendar.getInstance()
//        val (hours, minutes) = prayerTimes[nextPrayer]!!.split(":").map { it.toInt() }
//        nextPrayerTime.set(Calendar.HOUR_OF_DAY, hours)
//        nextPrayerTime.set(Calendar.MINUTE, minutes)
//        nextPrayerTime.set(Calendar.SECOND, 0)
//
//        if (nextPrayerTime.before(currentTime)) {
//            nextPrayerTime.add(Calendar.DAY_OF_MONTH, 1)
//        }
//
//        val diff = nextPrayerTime.timeInMillis - currentTime.timeInMillis
//        val hourss = diff / (60 * 60 * 1000)
//        val minutess = (diff / (60 * 1000)) % 60
//        val seconds = (diff / 1000) % 60
//
//        countdownTextView.text = String.format("%02d:%02d:%02d", hourss, minutess, seconds)
//    }

//    private fun setupPrayerTimeViews() {
//        val inflater = LayoutInflater.from(this)
//        prayerTimes.forEach { (prayer, time) ->
//            val prayerTimeView = inflater.inflate(R.layout.hidden_prayer_time_item, prayerTimesContainer, false)
//            val prayerNameTextView = prayerTimeView.findViewById<TextView>(R.id.prayerNameTextView)
//            val prayerTimeTextView = prayerTimeView.findViewById<TextView>(R.id.prayerTimeTextView)
//            val cardView = prayerTimeView.findViewById<CardView>(R.id.prayerTimeCardView)
//
//            prayerNameTextView.text = prayer
//            prayerTimeTextView.text = time
//
//            prayerTimeView.tag = cardView
//            prayerTimesContainer.addView(prayerTimeView)
//        }
//    }

//    private fun highlightNextPrayer(nextPrayer: String) {
//        prayerTimes.keys.forEachIndexed { index, prayer ->
//            val prayerTimeView = prayerTimesContainer.getChildAt(index)
//            val cardView = prayerTimeView.tag as CardView
//
//            if (prayer == nextPrayer) {
//                cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.next_prayer_highlight))
//            } else {
//                cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.prayer_time_background))
//            }
//        }
//    }

//    private fun getLocationAndFetchPrayerTimes() {
//        if (ActivityCompat.checkSelfPermission(
//                this, ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                this, ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            // Request location permissions if not granted
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(ACCESS_FINE_LOCATION),
//                1001
//            )
//            return
//        }
//
//        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
////            if (location != null) {
////                val city = "Mezitli" // You can use reverse geocoding to get city name from lat/long
////                val country = "Turkey"
////                fetchPrayerTimes(city, country)
////            } else {
////                Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show()
////            }
//            val city = "Mezitli" // You can use reverse geocoding to get city name from lat/long
//            val country = "Turkey"
//            fetchPrayerTimes(city, country)
//        }
//    }

    private fun fetchPrayerTimes(city: String, country: String) {
        val api = RetrofitClient.getInstance().create(PrayerTimesApi::class.java)
        val call = api.getPrayerTimes(city, country)

        call.enqueue(object : Callback<PrayerTimesResponse> {
            override fun onResponse(
                call: Call<PrayerTimesResponse>,
                response: Response<PrayerTimesResponse>
            ) {
                if (response.isSuccessful) {
                    val timings = response.body()?.data?.timings
                    if (timings != null) {
                        // Update your UI with the timings (Fajr, Dhuhr, Asr, etc.)
                        // Example:
                        findViewById<TextView>(R.id.fajrTime).text = timings.Fajr
                        findViewById<TextView>(R.id.sunriseTime).text = timings.Sunrise
                        findViewById<TextView>(R.id.dhuhrTime).text = timings.Dhuhr
                        findViewById<TextView>(R.id.asrTime).text = timings.Asr
                        findViewById<TextView>(R.id.maghribTime).text = timings.Maghrib
                        findViewById<TextView>(R.id.ishaTime).text = timings.Isha
                        // Similarly for other prayers
                    }
                }
            }

            override fun onFailure(call: Call<PrayerTimesResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Failed to fetch prayer times", Toast.LENGTH_SHORT).show()
            }
        })
    }

//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            getLocationAndFetchPrayerTimes()
//        } else {
//            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
//        }
//    }

}