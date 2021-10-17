package com.example.caixa_dagua_kotlin

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.caixa_dagua_kotlin.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.pow

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainActivityViewModel
    private var isBusy = false

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        viewModel.tcpData().observe(this, {
            binding.progressBar.visibility = View.GONE
            binding.statusImageView.visibility = View.VISIBLE

            if (it != "No connection") {
                val rawData = it.toDoubleOrNull()
                val waterLevel = calculateWaterLevel(rawData)

                when {
                    waterLevel > 98 -> {
                        binding.statusImageView.setImageResource(R.drawable.ic_ok)
                        binding.statusImageView.setColorFilter(getColor(R.color.color_normal))
                        binding.warningTextView.text = getString(R.string.water_level_full)
                        binding.levelTextView.text = ""
                    }
                    waterLevel < 98 -> {
                        binding.statusImageView.setImageResource(R.drawable.ic_aviso)

                        when {
                            waterLevel >= 70 -> binding.statusImageView.setColorFilter(
                                getColor(R.color.color_warning))
                            waterLevel >= 30 -> binding.statusImageView.setColorFilter(
                                getColor(R.color.color_caution))
                            else -> binding.statusImageView.setColorFilter(getColor(
                                R.color.color_critical))
                        }

                        binding.warningTextView.text = getString(R.string.water_level_dropping)
                        binding.levelTextView.text = getString(R.string.current_water_level,
                            waterLevel)
                    }
                }
            } else {
                binding.statusImageView.setImageResource(R.drawable.ic_tomb_grave)
                binding.statusImageView.setColorFilter(getColor(R.color.color_white))
                binding.warningTextView.text = getString(R.string.connection_failed)
                binding.levelTextView.text = getString(R.string.try_again)
                vibrate(250)
            }

            isBusy = false
        })

        lifecycleScope.launch(Dispatchers.Main) {
            isBusy = true
            viewModel.tcpComm(null)
        }
    }

    fun buttonPressed(view: View) {
        binding.progressBar.visibility = View.VISIBLE
        binding.statusImageView.visibility = View.GONE
        binding.warningTextView.text = ""
        binding.levelTextView.text = ""

        when (view.id) {
            R.id.check_level_button -> if (!isBusy) lifecycleScope.launch(Dispatchers.Main) {
                isBusy = true
                viewModel.tcpComm(null)
            }
            R.id.turn_alarm_off_button -> if (!isBusy) lifecycleScope.launch(Dispatchers.Main) {
                isBusy = true
                viewModel.tcpComm("off")
            }
        }
    }

    private fun Context.vibrate(milliseconds: Long = 500) {
        val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (vibrator.hasVibrator())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                vibrator.vibrate(VibrationEffect.createOneShot(milliseconds,
                    VibrationEffect.DEFAULT_AMPLITUDE))
            else
                @Suppress("DEPRECATION")
                vibrator.vibrate(milliseconds)
    }

    private fun calculateWaterLevel(receivedData: Double?): Double {
        if (receivedData == null) return 0.0

        return 1.0 * receivedData.pow(1)
    }
}

