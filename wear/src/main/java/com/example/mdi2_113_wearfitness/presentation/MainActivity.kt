/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.example.mdi2_113_wearfitness.presentation


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.example.mdi2_113_wearfitness.presentation.theme.MDI2113WearFitnessTheme
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.wearable.Wearable

class MainActivity : ComponentActivity() {
    private var heartRate by mutableIntStateOf(78)
    private var stepsGoal by mutableIntStateOf(10000)
    private lateinit var wearDataListener:  WearDataListener
    private lateinit var heartRateSensorManager: HeartRateSensorManager
    private val heartRatePermissionLauncher = registerForActivityResult(ActivityResultContracts
        .RequestPermission()) { isGranted ->
        if (isGranted){
            heartRateSensorManager.startListening()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel(this)
        heartRateSensorManager = HeartRateSensorManager(context = this, onHeartRateChange = {
                newHeartRate -> heartRate = newHeartRate
        })

        if(heartRateSensorManager.hasHeartRateSensor && !heartRateSensorManager.hasPermission()) {
            heartRatePermissionLauncher.launch(
                heartRateSensorManager.requiredPermission
            )
        }

        wearDataListener = WearDataListener(onStepsGoalChange = { newGoal ->
            runOnUiThread { stepsGoal = newGoal }
        })
        setContent {
            MDI2113WearFitnessTheme {
                WearFitnessApp(
                    heartRateSensorValue = heartRate,
                    hasHeartRateSensor = heartRateSensorManager.hasHeartRateSensor,
                    stepsGoalFromPhone = stepsGoal
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::heartRateSensorManager.isInitialized){
            heartRateSensorManager.startListening()
        }
        if (::wearDataListener.isInitialized){
            Wearable.getDataClient(this).addListener(wearDataListener)
        }
    }


    override fun onStop() {
        super.onStop()
        if (::heartRateSensorManager.isInitialized){
            heartRateSensorManager.stopListening()
        }

        if (::wearDataListener.isInitialized){
            Wearable.getDataClient(this).removeListener(wearDataListener)
        }
    }
}