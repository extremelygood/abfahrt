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


    /**
     * Origin function to start getting the data from peer
     * The way this is supposed to work is that _this_ is simply trying to "invoke" data from
     * the peer. We are listening for any peer requests to be fulfilled, peer is listening to
     * fulfill our requests.
     */
    fun start() {
        end()

        mainJob = myCoroutineScope.launch {



            val encountersDeferred = async { database.getAllMatches(MAX_ENCOUNTERS_TO_GET) }
            val profileDeferred = async { database.loadMyProfile() }

            val encountersList = encountersDeferred.await()
            val ownProfile = profileDeferred.await()

        }
    }


    fun end() {
        mainJob?.cancel()
    }


    /**
     * Method to handle when peer requests more information about a specific user id
     */
    private fun handleRequestEncounter(packet: RequestEncountersPacket) {
        myCoroutineScope.launch {
            val myProfileDeferred = async { database.loadMyProfile() }
            val encountersListDeferred = async { database.getAllMatches(MAX_ENCOUNTERS_TO_GET) }

            val myProfile = myProfileDeferred.await()
            val encountersList = encountersListDeferred.await()



        }

    }

    /**
     * Method to transmit to peer a list of userIds this client holds
     */
    private fun handleRequestEncountersList() {
        myCoroutineScope.launch {
            val myProfileDeferred = async { database.loadMyProfile() }
            val encountersListDeferred = async { database.getAllMatches(MAX_ENCOUNTERS_TO_GET) }

            val myProfile = myProfileDeferred.await()
            val encountersList = encountersListDeferred.await()


            val listOfIds = mutableListOf<String>()

            if (myProfile != null) {
                listOfIds.add(myProfile.id)
            }
            encountersList.forEach { encounter ->
                listOfIds.add(encounter.userId)
            }

            connection.sendPacket(EncountersListPacket(listOfIds), listOf())
        }
    }

    private fun onPacketReceive(combinedPacket: ParsedCombinedPacket) {
        when (combinedPacket.metaPacket) {
            is RequestEncountersListPacket -> {
                handleRequestEncountersList()
            }
            is RequestEncountersPacket -> {
                handleRequestEncounter(combinedPacket.metaPacket)
            }
        }
    }




}