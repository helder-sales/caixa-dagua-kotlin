package com.example.caixa_dagua_kotlin

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket

class MainActivityViewModel : ViewModel() {
    private lateinit var data: String
    private var _data = MutableLiveData<String>()

    suspend fun tcpComm(message: String?) {
        val job = GlobalScope.launch(Dispatchers.IO) {
            try {
                Socket().use { socket ->
                    val sockAdr = InetSocketAddress("192.168.15.11", 49152)

                    socket.connect(sockAdr, 3000)
                    socket.soTimeout = 3000

                    BufferedReader(InputStreamReader(socket.getInputStream())).use { bufReader ->
                        val printWriter = PrintWriter(socket.getOutputStream())

                        data = bufReader.readLine()
                        printWriter.write(message ?: "0")
                        printWriter.flush()
                    }
                }
            } catch (e: IOException) {
                println("Send Exception: " + e.message)
                data = "No connection"
            }
        }

        job.join()
        _data.value = data
    }

    fun tcpData(): MutableLiveData<String> {
        return _data
    }
}