/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.example.mdi2_113_wearfitness.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.mdi2_113_wearfitness.presentation.notifications.FitnessNotificationManager
import com.example.mdi2_113_wearfitness.presentation.sensors.HeartRateSensorController
import com.example.mdi2_113_wearfitness.presentation.theme.MDI2113WearFitnessTheme
import com.example.mdi2_113_wearfitness.presentation.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this).get(MainViewModel::class.java)
    }

    private lateinit var heartRateSensorController: HeartRateSensorController
    private lateinit var notificationManager: FitnessNotificationManager

    private val heartRatePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                heartRateSensorController.registerHeartRateSensor()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        notificationManager = FitnessNotificationManager(this)
        notificationManager.createNotificationChannel()

        heartRateSensorController = HeartRateSensorController(
            context = this,
            onHeartRateChanged = { heartRate ->
                viewModel.updateHeartRate(heartRate)
            }
        )

        val heartRatePermission = getHeartRatePermission()
        if (
            ContextCompat.checkSelfPermission(
                this,
                heartRatePermission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            heartRatePermissionLauncher.launch(heartRatePermission)
        } else {
            heartRateSensorController.registerHeartRateSensor()
        }

        setContent {
            MDI2113WearFitnessTheme {
                WearFitnessApp(
                    viewModel = viewModel,
                    notificationManager = notificationManager,
                    hasHeartRateSensor = heartRateSensorController.hasHeartRateSensor()
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        heartRateSensorController.registerHeartRateSensor()
    }

    override fun onPause() {
        super.onPause()
        heartRateSensorController.unregisterHeartRateSensor()
    }

    private fun getHeartRatePermission(): String {
        return if (Build.VERSION.SDK_INT >= 36) {
            "android.permission.health.READ_HEART_RATE"
        } else {
            Manifest.permission.BODY_SENSORS
        }
    }
}