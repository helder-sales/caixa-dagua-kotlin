package com.example.caixa_dagua_kotlin

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket

class MainActivity : AppCompatActivity() {
    private lateinit var genericTextView: TextView
    private lateinit var data: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        genericTextView = findViewById(R.id.generic_textView)
    }

    fun buttonPressed(view: View) {
        val job = GlobalScope.launch(Dispatchers.Main) {
            messageTransceiver("olar")
        }
    }

    suspend fun messageTransceiver(message: String?) {
        val value = GlobalScope.async {
            try {
                Socket().use { socket ->
                    val sockAdr = InetSocketAddress("192.168.15.11", 49152)
                    socket.connect(sockAdr, 3000)
                    socket.soTimeout = 3000
                    BufferedReader(InputStreamReader(socket.getInputStream())).use { `in` ->
                        PrintWriter(socket.getOutputStream()).use { printWriter ->
                            printWriter.write(message!!)
                            printWriter.flush()
                            data = `in`.readLine()
                        }
                    }
                }
            } catch (e: IOException) {
                println("Send Exception: " + e.message)
            }
        }

        value.await()
        genericTextView.setText(data)
    }
}

