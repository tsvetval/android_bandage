package com.inokisheo.kotlinwebview.ui.devices

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.inokisheo.kotlinwebview.R
import java.util.UUID

/**
 * Фрагмент по тображению настроек подключения к устройству
 */
class SettingsFragment : Fragment() {
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private var bluetoothGatt: BluetoothGatt? = null

    private val deviceList: MutableList<BluetoothDevice> = mutableListOf()
    //private val deviceNames: MutableList<String> = mutableListOf()
    private lateinit var recyclerView: RecyclerView
    private lateinit var debugLog: TextView;

    fun Context.bluetoothAdapter(): BluetoothAdapter? =
        (this.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
//        connectedDeviceTextView = view.findViewById(R.id.connectedDeviceTextView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = DeviceAdapter(deviceList) { device ->
            connectToDevice(device)
        }

//        listView = view.findViewById(R.id.listView)
        debugLog = view.findViewById(R.id.debug_log)

        val bta = requireContext().bluetoothAdapter()
        if (bta == null ||  !bta.isEnabled) {
            Toast.makeText(context, "Bluetooth не поддерживается", Toast.LENGTH_SHORT).show()
            return view
        } else {
            bluetoothAdapter = bta
            bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
            checkPermissionsAndStartScan()
        }
        return view
    }

    private fun checkPermissionsAndStartScan() {
        if
                (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
                1
            )
        } else {
            scanDevices()
        }
    }


    private var scanning = false
    private val handler = Handler()

    // Stops scanning after 10 seconds.
    private val SCAN_PERIOD: Long = 10000

    @SuppressLint("MissingPermission")
    private fun scanDevices() {
//        repeat(20){
//            debugLog.append("test\n\r")
//        }
        handler.postDelayed({
            bluetoothAdapter.bluetoothLeScanner?.stopScan(scanCallback)
        }, SCAN_PERIOD)

        deviceList.clear()
        recyclerView.adapter?.notifyDataSetChanged()

        bluetoothAdapter.bluetoothLeScanner?.startScan(scanCallback)
/*
        debugLog.append("start scanDevices\n\r")
        if (!scanning) { // Stops scanning after a pre-defined scan period.
            handler.postDelayed({
                debugLog.append("postDelayed \n\r")
                scanning = false
                try {
                    bluetoothLeScanner.stopScan(scanCallback)
                } catch (e: Throwable) {
                    debugLog.append("!postDelayed\n\r" + e.message)
                }
            }, SCAN_PERIOD)
            scanning = true
            debugLog.append("!scanning\n\r")
            try {
                bluetoothLeScanner.startScan(scanCallback)
            } catch (e: Throwable) {
                debugLog.append("!scanning\n\r" + e.message)
            }
        } else {
            debugLog.append("!stopScan\n\r")
            scanning = false
            try {
                bluetoothLeScanner.stopScan(scanCallback)
            } catch (e: Throwable) {
                debugLog.append("!stopScan\n\r" + e.message)
            }

        }
*/
    }

    @SuppressLint("MissingPermission")
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            debugLog.append("onScanResult\n\r")
                super.onScanResult(callbackType, result)
                val device = result.device

                if (!deviceList.contains(device)) {
                    deviceList.add(device)
                    //deviceNames.add(device.name + "\n" + device.address)
                    //updateListView()
                    recyclerView.adapter?.notifyItemInserted(deviceList.size - 1)
                }
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            debugLog.append("onBatchScanResults\n\r")
            super.onBatchScanResults(results)
            for (result in results) {
                val device = result.device

                if (!deviceList.contains(device)) {
                    deviceList.add(device)
                    recyclerView.adapter?.notifyItemInserted(deviceList.size - 1)
                    //deviceNames.add(device.name + "\n" + device.address)
                }
            }
            //updateListView()
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

/*    private fun updateListView() {
        debugLog.append("updateListView\n\r")
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, deviceNames)
        listView.adapter = adapter
    }*/

    @SuppressLint("MissingPermission")
    override fun onPause() {
        debugLog.append("onPause\n\r")
        super.onPause()
        bluetoothLeScanner.stopScan(scanCallback)
    }

    @SuppressLint("MissingPermission")
    private fun connectToDevice(device: BluetoothDevice) {
        debugLog.append("Try to connect ${device.name}\n\r")
        bluetoothGatt = device.connectGatt(requireContext(), false,
            object : BluetoothGattCallback() {
                override fun onConnectionStateChange(
                    gatt: BluetoothGatt,
                    status: Int,
                    newState: Int
                ) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        debugLog.append("BluetoothProfile.STATE_CONNECTED device name : ${device.name} gatt: $gatt\n\r")
                        Toast.makeText(context, "Подключено к ${device.name}", Toast.LENGTH_SHORT)
                            .show()
                        gatt.discoverServices()
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        debugLog.append("BluetoothProfile.STATE_DISCONNECTED\n\r")
                        Toast.makeText(context, "Отключено от ${device.name}", Toast.LENGTH_SHORT)
                            .show()
                        gatt.close()
                    }
                }
                override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                    debugLog.append("onServicesDiscovered BlueTooth Le ready\n\r")
                    super.onServicesDiscovered(gatt, status)
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        //enableNotifications(gatt)
                        sendCommandToDevice(gatt)
                    }
                }

                override fun onCharacteristicChanged(
                    gatt: BluetoothGatt,
                    characteristic: BluetoothGattCharacteristic,
                    value: ByteArray
                ) {
                    debugLog.append("Response ${value.toHex()}:\n\r")
                    super.onCharacteristicChanged(gatt, characteristic, value)
                }

                override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
                    super.onCharacteristicWrite(gatt, characteristic, status)
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        debugLog.append("Write command Success:\n\r")
                    } else {
                        debugLog.append("Write command Failed:\n\r")
                    }
                }

            })
    }
    fun ByteArray.toHex(): String = joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }
    fun String.decodeHex(): ByteArray {
        check(length % 2 == 0) { "Must have an even length" }

        return chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }
/*
    @SuppressLint("MissingPermission")
    private fun enableNotifications(gatt: BluetoothGatt?) {
        val service = gatt?.getService(SERVICE_UUID)
        val characteristic = service?.getCharacteristic(CHARACTERISTIC_UUID)

        characteristic?.let { char ->
            gatt.setCharacteristicNotification(char, true)

            val descriptor = char.getDescriptor(DESCRIPTOR_UUID)
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            gatt.writeDescriptor(descriptor)
        }
    }
*/


    @SuppressLint("MissingPermission")
    private fun sendCommandToDevice(gatt: BluetoothGatt?) {
        val services = gatt?.services
        debugLog.append("readServices:\n\r")
        services?.forEach {
            debugLog.append("${it.uuid}\n\r")
        }
        val service = gatt?.getService(services?.first()?.uuid)
        debugLog.append("readCharacteristics:\n\r")
        service?.characteristics?.forEach {
            debugLog.append("${it.uuid}\n\r")
        }
        val characteristic = service?.getCharacteristic(service.characteristics?.first()?.uuid)

        characteristic?.let { char ->
            // Устанавливаем значение характеристики (команда 0x58)
            val ReadTemperatureCmd = "58".decodeHex()
            char.value = ReadTemperatureCmd
            gatt.writeCharacteristic(char)

            // Включаем уведомления для характеристики
            gatt.setCharacteristicNotification(char, true)

            // Устанавливаем дескриптор для уведомлений
            val descriptor = char.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            gatt.writeDescriptor(descriptor)
        }
    }


    inner class DeviceAdapter(
        private val devices: List<BluetoothDevice>,
        private val onConnectClick: (BluetoothDevice) -> Unit
    ) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

        inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val deviceName: TextView = itemView.findViewById(R.id.deviceName)
            val connectButton: Button = itemView.findViewById(R.id.connectButton)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_device, parent, false)
            return DeviceViewHolder(view)
        }

        @SuppressLint("MissingPermission")
        override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
            val device = devices[position]
            holder.deviceName.text = device.name ?: device.address
            holder.connectButton.setOnClickListener {
                onConnectClick(device)
            }
        }

        override fun getItemCount(): Int {
            return devices.size
        }
    }


}