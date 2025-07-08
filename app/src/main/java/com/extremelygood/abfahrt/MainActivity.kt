package com.extremelygood.abfahrt

import android.Manifest
import android.os.Bundle
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.extremelygood.abfahrt.utils.RuntimePermissionsChecker
import com.extremelygood.abfahrt.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_profile, R.id.navigation_match
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


        val resultCallback: (Map<String, Boolean>, Boolean) -> Unit = { resultMap, fullSuccess ->
            Log.d("RuntimePermissions", "Got a result")
            resultMap.forEach { (permissionName, isAllowed) ->
                Log.d("RuntimePermissions", "Permission $permissionName is allowed? $isAllowed")
            }

            (application as AbfahrtApplication).tryStartConnectionManager()

        }

        val permissionsChecker: RuntimePermissionsChecker = RuntimePermissionsChecker(this, this, arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.NEARBY_WIFI_DEVICES,
            ),
            resultCallback
        )
        permissionsChecker.checkPermissions()
    }
}