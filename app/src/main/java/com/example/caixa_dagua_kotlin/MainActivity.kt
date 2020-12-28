package com.example.caixa_dagua_kotlin

import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)

        viewModel.tcpData().observe(this, {
            binding.progressBar.visibility = View.GONE
            binding.statusImageView.visibility = View.VISIBLE

            if (it != "No connection") {
                val rawData = it.toInt()
                val waterLevel: Double = (rawData - 1444) / 704.3
                val waterPercentageLeft = 1.448 * waterLevel.pow(5.0) - 7.451 *
                        waterLevel.pow(4.0) + 13.47 * waterLevel.pow(3.0) - 16.09 *
                        waterLevel.pow(2.0) + 29.96 * waterLevel + 64.13

                when {
                    rawData >= 4095 -> {
                        binding.statusImageView.setImageResource(R.drawable.ic_ok)
                        binding.statusImageView.setColorFilter(getColor(R.color.color_normal))
                        binding.warningTextView.text = "Caixa d'água cheia"
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

                        binding.warningTextView.text = "Caixa d'água esvaziando"
                        binding.levelTextView.text = "Nível d'água: $waterPercentageLeft%"
                    }
                }
            } else {
                binding.statusImageView.setImageResource(R.drawable.ic_tomb_grave)
                binding.statusImageView.setColorFilter(getColor(R.color.color_white))
                binding.warningTextView.text = "Conexão morreu :("
                binding.levelTextView.text = "Tente novamente"
            }
        })

        lifecycleScope.launch(Dispatchers.Main) {
            viewModel.tcpComm(null)
        }
    }

    fun buttonPressed(view: View) {
        binding.progressBar.visibility = View.VISIBLE
        binding.statusImageView.visibility = View.GONE
        binding.warningTextView.text = ""
        binding.levelTextView.text = ""

        when (view.id) {
            R.id.check_level_button -> lifecycleScope.launch(Dispatchers.Main) {
                viewModel.tcpComm(null)
            }
            R.id.turn_alarm_off_button -> lifecycleScope.launch(Dispatchers.Main) {
                viewModel.tcpComm("off")
            }
        }
    }
}

