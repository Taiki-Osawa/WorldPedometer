package jp.ac.titech.itpro.sdl.worldpedometer_non

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.lang.Math.pow
import java.util.*

const val DATAFILE: String = "jp.ac.titech.itpro.sdl.worldpedometer_non.DATAFILE"
const val PEDOMETER_DAILYTARGET: String = "jp.ac.titech.itpro.sdl.worldpedometer_non.PEDOMETER_DAILYTARGET"
const val LASTSENSOR: String = "jp.ac.titech.itpro.sdl.worldpedometer_non.LASTSENSOR"
const val PEDOMETER_TODAY: String = "jp.ac.titech.itpro.sdl.worldpedometer_non.PEDOMETER_TODAY"
const val PEDOMETER_TOTAL: String = "jp.ac.titech.itpro.sdl.worldpedometer_non.PEDOMETER_TOTAL"
const val PEDOMETER_ALL: String = "jp.ac.titech.itpro.sdl.worldpedometer_non.PEDOMETER_ALL"
const val DATE: String = "jp.ac.titech.itpro.sdl.worldpedometer_non.DATE"
const val LASTDAY: String = "jp.ac.titech.itpro.sdl.worldpedometer_non.LASTDAY"
const val LASTMONTH: String = "jp.ac.titech.itpro.sdl.worldpedometer_non.LASTMONTH"
const val LASTYEAR: String = "jp.ac.titech.itpro.sdl.worldpedometer_non.LASTYEAR"
const val PLAYER_HEIGHT: String = "jp.ac.titech.itpro.sdl.worldpedometer_non.PLAYER_HEIGHT"
const val PLAYER_HEIGHT_PREV: String = "jp.ac.titech.itpro.sdl.worldpedometer_non.PLAYER_HEIGHT_PREV"

class MainActivity : AppCompatActivity(), SensorEventListener{
    private lateinit var manager: SensorManager
    private var pedometerSensor: Sensor? = null
    private var pedometerDetector: Sensor? = null

    private lateinit var progressbar_today: ProgressBar
    private lateinit var progressbar_all: ProgressBar

    val LASTSENSOR_RESET: Int = 1000000
    val LASTTIME_RESET: Int = 1000000
    val PLAYER_HEIGHT_DEFAULT: Int = 170
    val EARTH_DIAMETER: Int = 40000000
    val LENGTH_ONEWALK: Double = 0.7

    private var pedometer_today: Int = 0
    private var pedometer_total: Int = 0
    private var pedometer_all: Double = 0.0
    private var date: Int = 0
    private var pedometer_todayTarget: Int = 100
    private var player_height: Int = PLAYER_HEIGHT_DEFAULT
    private var pedometer_lastSensor: Int = LASTSENSOR_RESET
    private var lastday: Int = LASTTIME_RESET
    private var lastmonth: Int = LASTTIME_RESET
    private var lastyear: Int = LASTTIME_RESET

    private var resetFlag: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        manager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        pedometerSensor = manager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        pedometerDetector = manager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        progressbar_today = findViewById(R.id.ProgressBar_Today)
        progressbar_all = findViewById(R.id.ProgressBar_All)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item?.itemId == R.id.action_reset) {
            resetFlag = true
            val switchIntent = Intent(this, StartActivity::class.java)
            startActivity(switchIntent)
        } else if (item?.itemId == R.id.action_debug) {
            switchResults()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        val sharedPref = getSharedPreferences(DATAFILE, MODE_PRIVATE)
        with(sharedPref){
            pedometer_lastSensor = getInt(LASTSENSOR, LASTSENSOR_RESET)
            pedometer_today = getInt(PEDOMETER_TODAY, 0)
            pedometer_total = getInt(PEDOMETER_TOTAL, 0)
            pedometer_all = getInt(PEDOMETER_ALL, 0).toDouble()
            setPedometerTarget(getInt(PEDOMETER_DAILYTARGET, 100))
            player_height = getInt(PLAYER_HEIGHT, PLAYER_HEIGHT_DEFAULT)
            date = getInt(DATE, 0)
            lastday = getInt(LASTDAY, LASTTIME_RESET)
            lastmonth = getInt(LASTMONTH, LASTTIME_RESET)
            lastyear = getInt(LASTYEAR, LASTTIME_RESET)
        }
        if(pedometer_todayTarget == 100) {
            resetFlag = true
            val switchIntent = Intent(this, StartActivity::class.java)
            startActivity(switchIntent)
        }
        val date_text: TextView = findViewById(R.id.Date)
        date_text.setText("Day " + (date + 1).toString())

        val player_height_field: TextView = findViewById(R.id.player_height_field)
        val player_height_double: Double = player_height.toDouble() / 100
        val player_onewalk: Double = LENGTH_ONEWALK * player_height / PLAYER_HEIGHT_DEFAULT
        player_height_field.setText("%.1f".format(player_height_double) + "m/%.1fm".format(player_onewalk))

        manager.registerListener(this, pedometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
        manager.registerListener(this, pedometerDetector, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        manager.unregisterListener(this)
        if(resetFlag) {
            val sharedPref = getSharedPreferences(DATAFILE, MODE_PRIVATE) ?: return
            with(sharedPref.edit()) {
                putInt(PEDOMETER_TODAY, 0)
                putInt(PEDOMETER_TOTAL, 0)
                putInt(PEDOMETER_ALL, 0)
                putInt(PEDOMETER_DAILYTARGET, 100)
                putInt(LASTSENSOR, LASTSENSOR_RESET)
                putInt(PLAYER_HEIGHT, PLAYER_HEIGHT_DEFAULT)
                putInt(DATE, 0)
                putInt(LASTDAY, LASTTIME_RESET)
                putInt(LASTMONTH, LASTTIME_RESET)
                putInt(LASTYEAR, LASTTIME_RESET)
                apply()
            }
        } else {
            val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"))
            val sharedPref = getSharedPreferences(DATAFILE, MODE_PRIVATE) ?: return
            with(sharedPref.edit()) {
                putInt(PEDOMETER_TODAY, pedometer_today)
                putInt(PEDOMETER_TOTAL, pedometer_total)
                putInt(PEDOMETER_ALL, pedometer_all.toInt())
                putInt(PEDOMETER_DAILYTARGET, pedometer_todayTarget)
                putInt(LASTSENSOR, pedometer_lastSensor)
                putInt(PLAYER_HEIGHT, player_height)
                putInt(DATE, date)
                putInt(LASTDAY, cal.get(Calendar.DAY_OF_MONTH))
                putInt(LASTMONTH, cal.get(Calendar.MONTH))
                putInt(LASTYEAR, cal.get(Calendar.YEAR))
                apply()
            }
        }
    }
    override fun onSensorChanged(event: SensorEvent) {
        val sensor: Sensor = event.sensor
        val values: FloatArray = event.values
        //TYPE_STEP_COUNTER
        if (sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            // sensor からの値を取得するなどの処理を行う
            Log.d("COUNTER", "COUNTER = " + values[0].toInt())
            if(pedometer_lastSensor == LASTSENSOR_RESET) {
                this.setPedometer(0)
            } else if(values[0].toInt() >= pedometer_lastSensor) {
                this.setPedometer(values[0].toInt() - pedometer_lastSensor)
            } else {
                this.setPedometer(values[0].toInt())
            }
            pedometer_lastSensor = values[0].toInt()

            val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"))
            if(lastday < cal.get(Calendar.DAY_OF_MONTH) || lastmonth < cal.get(Calendar.MONTH) || lastyear < cal.get(Calendar.YEAR)){
                switchResults()
            } else {
                lastday = cal.get(Calendar.DAY_OF_MONTH)
                lastmonth = cal.get(Calendar.MONTH)
                lastyear = cal.get(Calendar.YEAR)            }
        }

        if (sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            // sensor からの値を取得するなどの処理を行う
            Log.d("DETECTOR", "DETECTOR += 1")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    private fun setPedometer(pedometer: Int = 1) {
        pedometer_today += pedometer
        pedometer_total += pedometer
        progressbar_today.setProgress(pedometer_today)
        pedometer_all += pedometer * LENGTH_ONEWALK * player_height / PLAYER_HEIGHT_DEFAULT
        progressbar_all.setProgress(pedometer_all.toInt())
        val pedometer_today_text: TextView = findViewById(R.id.pedometer_today)
        pedometer_today_text.setText(pedometer_today.toString() + "歩/" + pedometer_todayTarget.toString() + "歩")
        val pedometer_all_text: TextView = findViewById(R.id.pedometer_all)
        pedometer_all_text.setText("残り" + (EARTH_DIAMETER - pedometer_all.toInt()).toString() + "m")
    }

    private fun setPedometerTarget(pedometerTargetDaily: Int){
        pedometer_todayTarget = pedometerTargetDaily
        progressbar_today.setMax(pedometer_todayTarget)
        progressbar_all.setMax(EARTH_DIAMETER)
    }

    private fun switchResults(){
        if(date > 6){
            resetFlag = true
            val switchIntent = Intent(this, StartActivity::class.java)
            startActivity(switchIntent)
        }

        val player_height_new = calcNewHeight()
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        val fragment = DayResultFlagment()
        val bundle = Bundle()
        with(bundle){
            putInt(PEDOMETER_TODAY, pedometer_today)
            putInt(PEDOMETER_TOTAL, pedometer_total)
            putInt(PEDOMETER_ALL, pedometer_all.toInt())
            putInt(PEDOMETER_DAILYTARGET, pedometer_todayTarget)
            putInt(LASTDAY, lastday)
            putInt(LASTMONTH, lastmonth)
            putInt(LASTYEAR, lastyear)
            putInt(PLAYER_HEIGHT, player_height_new)
            putInt(PLAYER_HEIGHT_PREV, player_height)
        }
        fragment.setArguments(bundle)
        with(fragmentTransaction){
            add(R.id.results_fragmentContainerView, fragment)
            addToBackStack(null)
            commit()
        }
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"))
        pedometer_today = 0
        player_height = player_height_new
        lastday = cal.get(Calendar.DAY_OF_MONTH)
        lastmonth = cal.get(Calendar.MONTH)
        lastyear = cal.get(Calendar.YEAR)
        date += 1
        val date_text: TextView = findViewById(R.id.Date)
        date_text.setText("Day " + (date + 1).toString())
        val player_height_field: TextView = findViewById(R.id.player_height_field)
        val player_height_double: Double = player_height.toDouble() / 100
        val player_onewalk: Double = LENGTH_ONEWALK * player_height / PLAYER_HEIGHT_DEFAULT
        player_height_field.setText("%.1f".format(player_height_double) + "m/%.1fm".format(player_onewalk))
        setPedometer(0)
    }
    private fun calcNewHeight(): Int{
        if(date > 5){
            return player_height
        } else {
            val k_pow_date: Double = (EARTH_DIAMETER - pedometer_all) / (LENGTH_ONEWALK * pedometer_todayTarget)
            Log.d("k_pow", k_pow_date.toString())
            val k: Double = pow(k_pow_date, 1.0/(6 - date))
            Log.d("k", k.toString())
            return (k * PLAYER_HEIGHT_DEFAULT).toInt()
        }
    }
}