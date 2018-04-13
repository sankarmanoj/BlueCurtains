package com.sankarmanoj.bluecurtains

import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.widget.Button
import android.widget.NumberPicker
import android.widget.SeekBar
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.set_range.*
import org.jetbrains.anko.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private var blueAdapter:BluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    var socketConnected = false
    var attemptingConnection = false
    lateinit var socket :BluetoothSocket
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        my_toolbar.inflateMenu(R.menu.menu_main)
        setSupportActionBar(my_toolbar)
        my_toolbar.setOnMenuItemClickListener {

            when (it.itemId) {
                R.id.action_range -> {
                    var dialog = Dialog(this)
                    dialog.setTitle("Set Range")
                    dialog.setContentView(R.layout.set_range)
                    var rangeNumberPicker = dialog.findViewById<NumberPicker>(R.id.rangeNumberPicker)
                    rangeNumberPicker.minValue = 1
                    rangeNumberPicker.maxValue = 500
                    rangeNumberPicker.value = seekBar.max

                    dialog.findViewById<Button>(R.id.closeRangeDialogButton).setOnClickListener(
                            {
                                seekBar.max = rangeNumberPicker.value
                                dialog.dismiss()
                                getPreferences(Context.MODE_PRIVATE).edit().putInt("Range",rangeNumberPicker.value).apply()
                            }
                    )
                    dialog.show()
                }

            }
            true
        }

        openButton.setOnClickListener{
            seekBar.setProgress(0,true)
        }
        closeButton.setOnClickListener {
            seekBar.setProgress(seekBar.max,true)
        }
        statusTextView.setOnClickListener {
            this.doAsync {
                if (!blueAdapter.isEnabled()) {
                    blueAdapter.enable()
                }
                for (device in blueAdapter.bondedDevices) {
                    if ("HC-05" in device.name) {

                        socket = device.createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                        try {
                            attemptingConnection = true
                            socket?.connect()
                            attemptingConnection = false
                            socketConnected = true
                            socket?.outputStream?.write((seekBar.progress.toString() + "~").toByteArray())
                            uiThread {
                                statusTextView.setTextColor(Color.GREEN)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            statusTextView.setTextColor(Color.RED)
                            socketConnected = false
                            attemptingConnection = false
                        }
                    }
                }
            }
        }

        seekBar.max = getPreferences(Context.MODE_PRIVATE).getInt("Range",10)
        seekBar.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener
        {
            override fun onStartTrackingTouch(p0: SeekBar?) {
                return

            }

            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if(socketConnected)
                {
                    try {
                        socket.outputStream?.write((p1.toString() + "~").toByteArray())
                    }
                    catch (e : Exception)
                    {
                        e.printStackTrace()
                        socketConnected = false
                        statusTextView.setTextColor(Color.RED)
                    }
                }
                else if(attemptingConnection==false) {
                    statusTextView.setTextColor(Color.RED)
                    this.doAsync {
                        if(!blueAdapter.isEnabled()) {
                            blueAdapter.enable()
                        }
                        for (device in blueAdapter.bondedDevices)
                        {
                            if("HC-05" in device.name)
                            {

                                socket = device.createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                                try {
                                    attemptingConnection = true
                                    socket?.connect()
                                    attemptingConnection = false
                                    socketConnected = true
                                    socket?.outputStream?.write((p1.toString() + "~").toByteArray())
                                    uiThread {
                                        statusTextView.setTextColor(Color.GREEN)
                                    }
                                }
                                catch (e : Exception)
                                {
                                    e.printStackTrace()
                                    statusTextView.setTextColor(Color.RED)
                                    socketConnected = false
                                    attemptingConnection = false
                                }
                            }
                        }
                    }

                    }

                    progressTextView.setText(p1.toString())
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                return
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.menu_main,menu)
        return true
    }

}
