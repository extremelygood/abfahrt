package com.extremelygood.abfahrt

import android.app.Application
import android.util.Log
import android.widget.Toast
import com.extremelygood.abfahrt.classes.DatabaseManager
import com.extremelygood.abfahrt.network.EncounterHandler
import com.extremelygood.abfahrt.network.NearbyConnectionManager

const val SERVICE_ID = "com.abfahrt"

class AbfahrtApplication: Application() {
    private lateinit var databaseManager: DatabaseManager
    private lateinit var connectionManager: NearbyConnectionManager

    companion object {
        lateinit var appModule: AppModule
    }

    override fun onCreate() {
        super.onCreate()

        Log.d("AbfahrtApplication", "Starting application")

        appModule = AppModuleImpl(applicationContext)



        databaseManager = DatabaseManager.getInstance(applicationContext)
        connectionManager = NearbyConnectionManager(applicationContext, SERVICE_ID)

    }


     fun tryStartConnectionManager() {
        connectionManager.startDiscovery()
        connectionManager.startAdvertising()

        connectionManager.setOnConnectionEstablished { newConnection ->
            Log.d("AbfahrtApplication", "Connection established callback")

            Toast.makeText(applicationContext, "Connection established", Toast.LENGTH_SHORT).show()
            EncounterHandler(newConnection, databaseManager).start()
        }

    }





}