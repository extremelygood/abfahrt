package com.extremelygood.abfahrt

import android.app.Application
import android.widget.Toast
import com.extremelygood.abfahrt.classes.DatabaseManager
import com.extremelygood.abfahrt.network.EncounterHandler
import com.extremelygood.abfahrt.network.NearbyConnectionManager

const val SERVICE_ID = "com.abfahrt"

class AbfahrtApplication: Application() {
    lateinit var databaseManager: DatabaseManager
    lateinit var connectionManager: NearbyConnectionManager

    override fun onCreate() {
        super.onCreate()

        databaseManager = DatabaseManager.getInstance(applicationContext)
        connectionManager = NearbyConnectionManager(applicationContext, SERVICE_ID)

        startConnectionManager()

    }


    private fun startConnectionManager() {
        connectionManager.startDiscovery()
        connectionManager.startAdvertising()

        connectionManager.setOnConnectionEstablished { newConnection ->
            Toast.makeText(this, "Connection established", Toast.LENGTH_SHORT).show()
            EncounterHandler(newConnection, databaseManager).start()
        }


    }


}