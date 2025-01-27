package com.inokisheo.kotlinwebview.ui.devices

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.inokisheo.kotlinwebview.R

/**
 * Фрагмент по тображению настроек подключения к устройству
 */
class SettingsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private lateinit var deviceList: MutableList<BluetoothDevice>
    private lateinit var deviceNames: MutableList<String>
    private lateinit var listView: ListView
    private lateinit var debugLog: TextView;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        listView = view.findViewById(R.id.listView)
        debugLog = view.findViewById(R.id.debug_log)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        deviceList = mutableListOf()
        deviceNames = mutableListOf()

        if (bluetoothAdapter == null) {
            Toast.makeText(context, "Bluetooth не поддерживается", Toast.LENGTH_SHORT).show()
            return view
        }

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_SCAN), 1)
        } else {
            scanDevices()
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            // Обработка нажатия для соединения с устройством
            val device = deviceList[position]
            connectToDevice(device)
        }
        debugLog.append("start\n\r")
        return view
    }

    private var scanning = false
    private val handler = Handler()

    // Stops scanning after 10 seconds.
    private val SCAN_PERIOD: Long = 10000
    @SuppressLint("MissingPermission")
    private fun scanDevices() {
        debugLog.append("start scanDevices\n\r")
        if (!scanning) { // Stops scanning after a pre-defined scan period.
            handler.postDelayed({
                debugLog.append("postDelayed \n\r")
                scanning = false
                //bluetoothLeScanner.stopScan(scanCallback)
            }, SCAN_PERIOD)
            scanning = true

            debugLog.append("!scanning\n\r")
            //bluetoothLeScanner.startScan(scanCallback)
        } else {
            debugLog.append("!stopScan\n\r")
            scanning = false
            //bluetoothLeScanner.stopScan(scanCallback)
        }
    }

    @SuppressLint("MissingPermission")
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            debugLog.append("onScanResult\n\r")
            super.onScanResult(callbackType, result)
            val device = result.device

            if (!deviceList.contains(device)) {
                deviceList.add(device)
                deviceNames.add(device.name + "\n" + device.address)
                updateListView()
            }
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            debugLog.append("onBatchScanResults\n\r")
            super.onBatchScanResults(results)
            for (result in results) {
                val device = result.device

                if (!deviceList.contains(device)) {
                    deviceList.add(device)
                    deviceNames.add(device.name + "\n" + device.address)
                }
            }
            updateListView()
        }

        override fun onScanFailed(errorCode: Int) {
            debugLog.append("onScanFailed\n\r")
            super.onScanFailed(errorCode)
            Toast.makeText(
                context,
                "Сканирование не удалось с кодом ошибки: $errorCode",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateListView() {
        debugLog.append("updateListView\n\r")
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, deviceNames)
        listView.adapter = adapter
    }

    @SuppressLint("MissingPermission")
    override fun onPause() {
        debugLog.append("updateListView\n\r")
        super.onPause()
        bluetoothLeScanner.stopScan(scanCallback)
    }

    @SuppressLint("MissingPermission")
    private fun connectToDevice(device: BluetoothDevice) {
        val gatt = device.connectGatt(requireContext(), false,
            object : BluetoothGattCallback() {
                override fun onConnectionStateChange(
                    gatt: BluetoothGatt,
                    status: Int,
                    newState: Int
                ) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        Toast.makeText(context, "Подключено к ${device.name}", Toast.LENGTH_SHORT)
                            .show()
                        gatt.discoverServices()
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        Toast.makeText(context, "Отключено от ${device.name}", Toast.LENGTH_SHORT)
                            .show()
                        gatt.close()
                    }
                }
                // Дополнительные методы для обработки сервисов и характеристик...
            })
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SettingsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SettingsFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}