package com.example.caixa_dagua_kotlin

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.caixa_dagua_kotlin.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var data: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    fun buttonPressed(view: View) {
        when(view.id) {
            R.id.generic_button -> {
                lifecycleScope.launch(Dispatchers.Main) {
                    tcpComm("test")
                }
            }
            R.id.generic_button_2 -> binding.genericTextView.text = "Button 2 pressed"
        }
    }

    private suspend fun tcpComm(message: String) {
        val value = GlobalScope.async(Dispatchers.IO) {
            try {
                Socket().use { socket ->
                    val sockAdr = InetSocketAddress("192.168.15.11", 49152)
                    socket.connect(sockAdr, 3000)
                    socket.soTimeout = 3000

                    BufferedReader(InputStreamReader(socket.getInputStream())).use { bufReader ->
                        val printWriter = PrintWriter(socket.getOutputStream())

                        data = bufReader.readLine()
                        printWriter.write(message)
                        printWriter.flush()
                    }
                }
            } catch (e: IOException) {
                println("Send Exception: " + e.message)
                data = "Sem conexão"
            }
        }

        value.await()
        binding.genericTextView.text = data
    }
}

