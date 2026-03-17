package com.example.sleep_tracker

import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import android.widget.Button
import android.widget.Chronometer
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    private lateinit var chronometer: Chronometer
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private lateinit var btnReset: Button
    private lateinit var btnSettings: Button
    private lateinit var tvSleepStage: TextView
    private lateinit var tvSummary: TextView
    private lateinit var tvGoalStatus: TextView

    private var isTracking = false
    private var isGoalSet = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        chronometer = findViewById(R.id.chronometer)
        btnStart = findViewById(R.id.btnStart)
        btnStop = findViewById(R.id.btnStop)
        btnReset = findViewById(R.id.btnReset)
        btnSettings = findViewById(R.id.btnSettings)
        tvSleepStage = findViewById(R.id.tvSleepStage)
        tvSummary = findViewById(R.id.tvSummary)
        tvGoalStatus = findViewById(R.id.tvGoalStatus)

        updateSummary()

        btnStart.setOnClickListener {
            startTracking()
        }

        btnStop.setOnClickListener {
            stopTracking()
        }

        btnReset.setOnClickListener {
            resetTracking()
        }

        btnSettings.setOnClickListener {
            isGoalSet = true
            Toast.makeText(this, getString(R.string.test_goal_set), Toast.LENGTH_SHORT).show()
        }

        chronometer.onChronometerTickListener = Chronometer.OnChronometerTickListener {
            val elapsedMillis = SystemClock.elapsedRealtime() - it.base
            updateSleepStage(elapsedMillis)
        }
    }

    private fun startTracking() {
        if (!isTracking) {
            chronometer.base = SystemClock.elapsedRealtime()
            chronometer.start()
            isTracking = true
            btnStart.isEnabled = false
            btnStop.isEnabled = true
            tvSleepStage.text = getString(R.string.status_light_sleep)
        }
    }

    private fun stopTracking() {
        if (isTracking) {
            chronometer.stop()
            val durationMillis = SystemClock.elapsedRealtime() - chronometer.base
            isTracking = false
            btnStart.isEnabled = true
            btnStop.isEnabled = false
            saveSleepRecord(durationMillis)
            updateSummary()
            checkGoal(durationMillis)
        }
    }

    private fun resetTracking() {
        chronometer.stop()
        chronometer.base = SystemClock.elapsedRealtime()
        isTracking = false
        btnStart.isEnabled = true
        btnStop.isEnabled = false
        tvSleepStage.text = getString(R.string.status_not_tracking)
        tvGoalStatus.text = ""
    }

    private fun updateSleepStage(elapsedMillis: Long) {
        if (elapsedMillis >= 5000) {
            tvSleepStage.text = getString(R.string.status_deep_sleep)
        } else {
            tvSleepStage.text = getString(R.string.status_light_sleep)
        }
    }

    private fun saveSleepRecord(durationMillis: Long) {
        val sharedPref = getSharedPreferences("SleepPrefs", Context.MODE_PRIVATE)
        sharedPref.edit {
            val totalSleep = sharedPref.getLong("total_sleep", 0) + durationMillis
            val count = sharedPref.getInt("sleep_count", 0) + 1
            putLong("total_sleep", totalSleep)
            putInt("sleep_count", count)
        }
    }

    private fun updateSummary() {
        val sharedPref = getSharedPreferences("SleepPrefs", Context.MODE_PRIVATE)
        val totalSleep = sharedPref.getLong("total_sleep", 0)
        val count = sharedPref.getInt("sleep_count", 0)

        if (count > 0) {
            val avgMillis = totalSleep / count
            val seconds = (avgMillis / 1000).toInt()
            tvSummary.text = getString(R.string.avg_sleep_format, seconds)
        }
    }

    private fun checkGoal(durationMillis: Long) {
        if (isGoalSet) {
            val goalMillis = 10 * 1000L
            val diff = durationMillis - goalMillis
            val diffSec = abs(diff) / 1000
            
            if (diff >= 0) {
                tvGoalStatus.text = getString(R.string.goal_met_format, diffSec)
            } else {
                tvGoalStatus.text = getString(R.string.goal_missed_format, diffSec)
            }
        }
    }
}