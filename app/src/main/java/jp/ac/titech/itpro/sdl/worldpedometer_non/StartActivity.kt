package jp.ac.titech.itpro.sdl.worldpedometer_non

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import java.util.*

class StartActivity : AppCompatActivity() {
    private val REQUEST_SENSOR_PERMISSION: Int = 300
    private var difficulty_choice: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        val difficulty: RadioGroup = findViewById(R.id.difficulty)
        difficulty.setOnCheckedChangeListener { radioGroup, checkedId: Int ->
            when(checkedId) {
                R.id.Button_easy -> this.setDifficulty(1)
                R.id.Button_normal -> this.setDifficulty(2)
                R.id.Button_hard -> this.setDifficulty(3)
                R.id.Button_debug -> this.setDifficulty(4)
            }
        }
        val startButton: Button = findViewById(R.id.StartButton)
        startButton.setOnClickListener{
            if(difficulty_choice == 0) {
                Toast.makeText(this, "難易度を選択してください", Toast.LENGTH_SHORT).show()
            } else {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACTIVITY_RECOGNITION), REQUEST_SENSOR_PERMISSION);
                } else {
                    this.switchActivity()
                }
            }
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == REQUEST_SENSOR_PERMISSION) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                this.switchActivity()
            } else {
                Toast.makeText(this, "センサー機能を許可してください", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setDifficulty(d: Int){
        difficulty_choice = d
    }

    private fun switchActivity(){
        val intent = Intent(this, MainActivity::class.java).apply {
            var dailytarget = 100
            when(difficulty_choice) {
                1 -> dailytarget = 6000
                2 -> dailytarget = 8000
                3 -> dailytarget = 10000
                4 -> dailytarget = 30
            }
            val sharedPref = getSharedPreferences(DATAFILE, MODE_PRIVATE) ?: return
            with(sharedPref.edit()) {
                putInt(PEDOMETER_DAILYTARGET, dailytarget)
                commit()
            }
        }
        startActivity(intent)
    }
}