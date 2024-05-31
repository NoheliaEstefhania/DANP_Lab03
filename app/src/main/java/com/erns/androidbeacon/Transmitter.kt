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
        Log.d(TAG, "estoy funcionando") // Registro de inicio de la transmisión

        val ID = "6ef0e30d73084458b62ef706c692ca77" // Identificador único

        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        var adapter = bluetoothManager.adapter

        if (adapter == null || !adapter.isEnabled) {
            Log.e(TAG, "Bluetooth is disabled") // Verificación de si Bluetooth está habilitado
            return
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADVERTISE)
            != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "BLUETOOTH_ADVERTISE permission denied!") // Verificación de permisos
            return
        }

        adapter.name="LE"

        // Verificación de soporte para 2M PHY
        if (!adapter.isLe2MPhySupported) {
            Log.e(TAG, "2M PHY not supported!")
            return
        }

        // Verificación de soporte para publicidad extendida
        if (!adapter.isLeExtendedAdvertisingSupported) {
            Log.e(TAG, "LE Extended Advertising not supported!")
            return
        }

        val advertiser = adapter.bluetoothLeAdvertiser

        // Verificación de si el objeto de publicidad es nulo
        if (advertiser == null) {
            Log.e(TAG, "bluetoothLeAdvertiser is null")
            return
        }

        val dataBuilder = BuildAdvertiseData(ID) // Construcción de datos de publicidad
        val settingsBuilder = buildAdvertiseSettings() // Configuración de la publicidad

        if (advertiser != null) {
            advertiser.stopAdvertising(callbackClose)
            advertiser.startAdvertising(settingsBuilder.build(), dataBuilder.build(), callback) // Iniciar la publicidad
        } else {
            Log.d(TAG, "advertiser is null")
        }
    }

    // Método para construir la configuración de la publicidad
    private fun buildAdvertiseSettings(): AdvertiseSettings.Builder{
        val settingsBuilder = AdvertiseSettings.Builder()
        settingsBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER) // Modo de publicidad de bajo consumo
        settingsBuilder.setConnectable(false) // No conectable
        settingsBuilder.setTimeout(0) // Publicidad continua
        settingsBuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
        return settingsBuilder
    }

    // Callback para la publicidad
    private val callback = object : AdvertiseCallback() {

        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            Log.d(TAG, "Advertising successfully started") // Registro de inicio exitoso de la publicidad
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)

            Log.d(TAG, "Advertising failed, errorCode: $errorCode") // Registro de fallo de la publicidad
            handleAdvertisingError(errorCode) // Manejo de errores de la publicidad
        }
    }

    // Método para manejar los errores de la publicidad
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

    // Callback para el cierre de la publicidad
    private val callbackClose = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            Log.d(TAG, "Advertising successfully close") // Registro de cierre exitoso de la publicidad
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)

            Log.d(TAG, "Advertising failed, errorCode: $errorCode") // Registro de fallo de la publicidad
        }
    }

    // Método para construir los datos de la publicidad
    private fun BuildAdvertiseData(uuid: String) : AdvertiseData.Builder{
        val dataBuilder = AdvertiseData.Builder()
        dataBuilder.setIncludeDeviceName(true) // Incluye el nombre del dispositivo en los datos de publicidad
        dataBuilder.setIncludeTxPowerLevel(false) //Esta línea indica que no se incluye el nivel de
        // potencia de transmisión en el mensaje

        val manufacturerData = ByteBuffer.allocate(23)

        val uuid: ByteArray = BleTools.getIdAsByte(uuid)

        manufacturerData.put(0, 0x02.toByte()) // Identificador de Beacon
        manufacturerData.put(1, 0x15.toByte()) // Identificador de Beacon
        for (i in 2..17) {
            manufacturerData.put(i, uuid[i - 2]) // Añadir el UUID
        }
        manufacturerData.put(18, 0x00.toByte()) // Primer byte de Major
        manufacturerData.put(19, 0x05.toByte()) // Segundo byte de Major
        manufacturerData.put(20, 0x00.toByte()) // Primer byte de Minor
        manufacturerData.put(21, 0x58.toByte()) // Segundo byte de Minor
        manufacturerData.put(22, 0x76.toByte()) // txPower
        dataBuilder.addManufacturerData(76, manufacturerData.array())
        return dataBuilder
    }
}
