package com.extremelygood.abfahrt.network

import MatchProfile
import com.extremelygood.abfahrt.classes.DatabaseManager
import com.extremelygood.abfahrt.classes.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.runInterruptible

const val MAX_ENCOUNTERS_TO_GET = 50

/**
 * Class for handling a connection made to another device
 */
class EncounterHandler(
    private val connection: NearbyConnection,
    private val database: DatabaseManager,
) {
    private var myCoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var mainJob: Job? = null


    init {
        connection.setDisconnectCallback {
            end()
        }

        connection.setPacketReceiveCallback { packet ->
            onPacketReceive(packet)
        }
    }



    fun start() {
        end()

        mainJob = myCoroutineScope.launch {

            // Step 1: Exchange profiles

            val encountersDeferred = async { database.getAllMatches(MAX_ENCOUNTERS_TO_GET) }
            val profileDeferred = async { database.loadMyProfile() }

            val encountersList = encountersDeferred.await()
            val ownProfile = profileDeferred.await()

        }
    }


    fun end() {
        mainJob?.cancel()
    }

    private fun handleRequestProfile() {
        val myProfile = UserProfile() // database.getProfile() should be here

        connection.sendPacket(ProfilePacket(myProfile), listOf())
    }

    private fun onPacketReceive(combinedPacket: ParsedCombinedPacket) {
        when (combinedPacket.metaPacket) {
            is RequestProfilePacket -> {
                handleRequestProfile()
            }
        }
    }




}