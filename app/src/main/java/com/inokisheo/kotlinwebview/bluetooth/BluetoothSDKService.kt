package com.inokisheo.kotlinwebview.bluetooth

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class BluetoothSDKService: Service() {

    // Service Binder
    private val binder = LocalBinder()

    // Bluetooth stuff
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var pairedDevices: MutableSet<BluetoothDevice>
    private var connectedDevice: BluetoothDevice? = null
    private val MY_UUID = "..."
    private val RESULT_INTENT = 15

    // Bluetooth connections
    private var connectThread: ConnectThread? = null
    private var connectedThread: ConnectedThread? = null
    private var mAcceptThread: AcceptThread? = null

    // Invoked only first time
    override fun onCreate() {
        super.onCreate()
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    }

    // Invoked every service star
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    /**
     * Class used for the client Binder.
     */
    inner class LocalBinder : Binder() {
        /*
        Function that can be called from Activity or Fragment
        */
        public fun startDiscovery(context: Context) {
            val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            registerReceiver(discoveryBroadcastReceiver, filter)
            bluetoothAdapter.startDiscovery()
            pushBroadcastMessage(BluetoothUtils.ACTION_DISCOVERY_STARTED, null, null)
        }


        /**
         * stop discovery
         */
        public fun stopDiscovery() {
            bluetoothAdapter.cancelDiscovery()
            pushBroadcastMessage(BluetoothUtils.ACTION_DISCOVERY_STOPPED, null, null)
        }

    }


    /**
     * Broadcast Receiver for catching ACTION_FOUND aka new device discovered
     */
    private val discoveryBroadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            /*
              Our broadcast receiver for manage Bluetooth actions
            */
        }
    }

    private inner class AcceptThread : Thread() {
        // Body
    }

    private inner class ConnectThread(device: BluetoothDevice) : Thread() {
        // Body
    }

    @Synchronized
    private fun startConnectedThread(
        bluetoothSocket: BluetoothSocket?,
    ) {
        connectedThread = ConnectedThread(bluetoothSocket!!)
        connectedThread!!.start()
    }

    private inner class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {
        private val mmInStream: InputStream = mmSocket.inputStream
        private val mmOutStream: OutputStream = mmSocket.outputStream
        private val mmBuffer: ByteArray = ByteArray(1024) // mmBuffer store for the stream

        override fun run() {
            var numBytes: Int // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                // Read from the InputStream.
                numBytes = try {
                    mmInStream.read(mmBuffer)
                } catch (e: IOException) {
                    pushBroadcastMessage(
                        BluetoothUtils.ACTION_CONNECTION_ERROR,
                        null,
                        "Input stream was disconnected"
                    )
                    break
                }

                val message = String(mmBuffer, 0, numBytes)

                // Send to broadcast the message
                pushBroadcastMessage(
                    BluetoothUtils.ACTION_MESSAGE_RECEIVED,
                    mmSocket.remoteDevice,
                    message
                )
            }
        }

        // Call this from the main activity to send data to the remote device.
        fun write(bytes: ByteArray) {
            try {
                mmOutStream.write(bytes)

                // Send to broadcast the message
                pushBroadcastMessage(
                    BluetoothUtils.ACTION_MESSAGE_SENT,
                    mmSocket.remoteDevice,
                    null
                )
            } catch (e: IOException) {
                pushBroadcastMessage(
                    BluetoothUtils.ACTION_CONNECTION_ERROR,
                    null,
                    "Error occurred when sending data"
                )
                return
            }
        }

        // Call this method from the main activity to shut down the connection.
        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
                pushBroadcastMessage(
                    BluetoothUtils.ACTION_CONNECTION_ERROR,
                    null,
                    "Could not close the connect socket"
                )
            }
        }
        // Body
    }


    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(discoveryBroadcastReceiver)
        } catch (e: Exception) {
            // already unregistered
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    private fun pushBroadcastMessage(action: String, device: BluetoothDevice?, message: String?) {
        val intent = Intent(action)
        if (device != null) {
            intent.putExtra(BluetoothUtils.EXTRA_DEVICE, device)
        }
        if (message != null) {
            intent.putExtra(BluetoothUtils.EXTRA_MESSAGE, message)
        }
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }

}