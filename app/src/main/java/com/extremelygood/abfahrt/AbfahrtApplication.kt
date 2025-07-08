package com.extremelygood.abfahrt

import android.app.Application
import com.extremelygood.abfahrt.network.NearbyConnectionManager

class AbfahrtApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        startConnectionManager()

    }

    private fun startConnectionManager() {
        val connectionManager = NearbyConnectionManager(this, "")
        connectionManager.startDiscovery()
        connectionManager.startAdvertising()
    }


}