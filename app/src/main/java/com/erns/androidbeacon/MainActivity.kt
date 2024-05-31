package com.erns.androidbeacon

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

// Clase principal de la aplicación
class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Configura la IU de la actividad
        setContentView(R.layout.activity_main)

        // Configura los botones y la IU
        val btnStartTransmitter: Button = findViewById(R.id.btnStartTransmitter)
        btnStartTransmitter.setOnClickListener {
            // Cuando se presiona el botón, se inicia la transmisión
            val transmitter = Transmitter(applicationContext)
            transmitter.startAdvertiser()
        }

        // Solicita permisos necesarios
        val permissions = arrayOf(
            android.Manifest.permission.BLUETOOTH_ADVERTISE,
            android.Manifest.permission.BLUETOOTH_CONNECT,
            android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
        )

        // Registra el lanzador de resultados para solicitar permisos
        activityResultLauncher.launch(permissions)
    }

    // Callback para manejar los resultados de la solicitud de permisos
    val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.entries.forEach {
            val permissionName = it.key
            val isGranted = it.value
            if (isGranted) {
                Log.d(TAG, "Permission $permissionName is granted")
            } else {
                Log.d(TAG, "Permission $permissionName is denied")
            }
        }
    }
}