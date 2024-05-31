package com.erns.androidbeacon

import android.Manifest
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.ParcelUuid
import android.util.Log
import androidx.core.app.ActivityCompat
import com.erns.androidbeacon.tools.BleTools
import java.nio.ByteBuffer

class Transmitter(private val context: Context) {
    private val TAG = "Transmitter"

    fun startAdvertiser() {
        Log.d(TAG, "estoy funcionando")

        val ID = "6ef0e30d73084458b62ef706c692ca77"

        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        var adapter = bluetoothManager.adapter

        if (adapter == null || !adapter.isEnabled) {
            Log.e(TAG, "Bluetooth is disabled")
            return
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADVERTISE)
            != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "BLUETOOTH_ADVERTISE permission denied!")
            return
        }

        //Log.e(TAG, " adapter.leMaximumAdvertisingDataLength "+ adapter.leMaximumAdvertisingDataLength)

        adapter.name="LE"

        if (!adapter.isLe2MPhySupported) {
            Log.e(TAG, "2M PHY not supported!")
            return
        }

        if (!adapter.isLeExtendedAdvertisingSupported) {
            Log.e(TAG, "LE Extended Advertising not supported!")
            return
        }

        val advertiser = adapter.bluetoothLeAdvertiser

        if (advertiser == null) {
            Log.e(TAG, "bluetoothLeAdvertiser is null")
            return
        }

        val dataBuilder = BuildAdvertiseData(ID)
        val settingsBuilder = buildAdvertiseSettings()

        if (advertiser != null) {
            advertiser.stopAdvertising(callbackClose)
            advertiser.startAdvertising(settingsBuilder.build(), dataBuilder.build(), callback)
        } else {
            Log.d(TAG, "advertiser is null")
        }
    }

    private fun buildAdvertiseSettings(): AdvertiseSettings.Builder{
        val settingsBuilder = AdvertiseSettings.Builder()
        settingsBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER) // saves on battery power
        settingsBuilder.setConnectable(false) // set to true to connect to other ble devices
        settingsBuilder.setTimeout(0) //set to 0 to continously advertise
        settingsBuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
        return settingsBuilder
    }


    private val callback = object : AdvertiseCallback() {

        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            Log.d(TAG, "Advertising successfully started")
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)

            Log.d(TAG, "Advertising failed, errorCode: $errorCode")
            handleAdvertisingError(errorCode)
            //                sendFailureIntent(errorCode);
            //                stopSelf();
        }
    }

    private fun handleAdvertisingError(errorCode:Int){
        when (errorCode) {
            AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED -> Log.d(TAG, "ADVERTISE_FAILED_ALREADY_STARTED")
            AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE -> Log.d(TAG, "ADVERTISE_FAILED_DATA_TOO_LARGE")
            AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED -> Log.d(TAG, "ADVERTISE_FAILED_FEATURE_UNSUPPORTED")
            AdvertiseCallback.ADVERTISE_FAILED_INTERNAL_ERROR -> Log.d(TAG, "ADVERTISE_FAILED_INTERNAL_ERROR")
            AdvertiseCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS -> Log.d(TAG, "ADVERTISE_FAILED_TOO_MANY_ADVERTISERS")
            else -> Log.d(TAG, "Unhandled error: $errorCode")
        }
    }

    private val callbackClose = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            Log.d(TAG, "Advertising successfully close")
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)

            Log.d(TAG, "Advertising failed, errorCode: $errorCode")
        }
    }

    private fun BuildAdvertiseData(uuid: String) : AdvertiseData.Builder{
        val dataBuilder = AdvertiseData.Builder()
        dataBuilder.setIncludeDeviceName(true) // To save on packet space you may not include some data
        dataBuilder.setIncludeTxPowerLevel(false)

        val manufacturerData = ByteBuffer.allocate(23)

        val uuid: ByteArray = BleTools.getIdAsByte(uuid)

        manufacturerData.put(0, 0x02.toByte()) // Beacon Identifier
        manufacturerData.put(1, 0x15.toByte()) // Beacon Identifier
        for (i in 2..17) {
            manufacturerData.put(i, uuid[i - 2]) // adding the UUID
        }
        manufacturerData.put(18, 0x00.toByte()) // first byte of Major
        manufacturerData.put(19, 0x05.toByte()) // second byte of Major
        manufacturerData.put(20, 0x00.toByte()) // first minor
        manufacturerData.put(21, 0x58.toByte()) // second minor
        manufacturerData.put(22, 0x76.toByte()) // txPower
        dataBuilder.addManufacturerData(76, manufacturerData.array())
        return dataBuilder
    }

}