package com.example.caixa_dagua_kotlin

import android.content.Context
import android.content.pm.ActivityInfo
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED

        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        viewModel.tcpData().observe(this, {
            binding.progressBar.visibility = View.GONE
            binding.statusImageView.visibility = View.VISIBLE

            if (it != "No connection") {
                val rawData = it.toInt()
                val waterLevel = (rawData - 1444) / 704.3
                val waterPercentageLeft = 1.448 * waterLevel.pow(5.0) - 7.451 *
                        waterLevel.pow(4.0) + 13.47 * waterLevel.pow(3.0) - 16.09 *
                        waterLevel.pow(2.0) + 29.96 * waterLevel + 64.13

                when {
                    rawData >= 4095 -> {
                        binding.statusImageView.setImageResource(R.drawable.ic_ok)
                        binding.statusImageView.setColorFilter(getColor(R.color.color_normal))
                        binding.warningTextView.text = getString(R.string.water_level_full)
                        binding.levelTextView.text = ""
                    }
                    rawData < 4095 -> {
                        binding.statusImageView.setImageResource(R.drawable.ic_aviso)

                        when {
                            waterPercentageLeft >= 70 -> binding.statusImageView.setColorFilter(
                                getColor(R.color.color_warning))
                            waterPercentageLeft >= 30 -> binding.statusImageView.setColorFilter(
                                getColor(R.color.color_caution))
                            else -> binding.statusImageView.setColorFilter(getColor(
                                R.color.color_critical))
                        }

                        binding.warningTextView.text = getString(R.string.water_level_dropping)
                        binding.levelTextView.text = getString(R.string.current_water_level,
                            waterPercentageLeft)
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
            R.id.turn_alarm_off_button -> if(!isBusy) lifecycleScope.launch(Dispatchers.Main) {
                isBusy = true
                viewModel.tcpComm("off")
            }
        }
    }

    private fun Context.vibrate(milliseconds: Long = 500) {
        val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (vibrator.hasVibrator())
            vibrator.vibrate(VibrationEffect.createOneShot(milliseconds,
                VibrationEffect.DEFAULT_AMPLITUDE))
    }
}

