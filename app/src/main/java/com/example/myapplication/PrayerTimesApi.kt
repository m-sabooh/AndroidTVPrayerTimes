import com.example.myapplication.PrayerTimesResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface PrayerTimesApi {
    @GET("timingsByCity")
    fun getPrayerTimes(
        @Query("city") city: String,
        @Query("country") country: String,
        @Query("method") method: Int = 13 // This is the calculation method
    ): Call<PrayerTimesResponse>
}