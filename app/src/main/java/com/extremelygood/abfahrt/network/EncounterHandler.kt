package com.extremelygood.abfahrt.network

import android.util.Log
import com.extremelygood.abfahrt.classes.MatchProfile
import com.extremelygood.abfahrt.classes.DatabaseManager
import com.extremelygood.abfahrt.classes.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


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
            Log.d("EnounterHandler", "Start - Sending request list packet")
            connection.sendPacket(RequestEncountersListPacket(), listOf())

        }
    }


    fun end() {
        mainJob?.cancel()
    }


    private fun handleEncounterReceive(packet: EncounterPacket) {
        myCoroutineScope.launch {
            Log.d("EncounterHandler", "Receiving encounter data")

            val encounterProfile = packet.encounter.userProfile
            val existingProfile = database.getMatchProfile(encounterProfile.id)


            // Cases where you want to discard packet


            if (existingProfile != null) {
                return@launch
            }


            // All checks complete, save this profile

            val newMatchProfile = MatchProfile(
                encounterProfile.id,
                encounterProfile.firstName,
                encounterProfile.lastName,
                encounterProfile.age,
                encounterProfile.description,
                encounterProfile.isDriver,
                encounterProfile.destination,
                packet.encounter.encounterTime
            )

            database.saveMatchProfile(newMatchProfile)
        }
    }

    private fun handleEncounterListReceive(packet: EncountersListPacket) {
        // Select which encounters this client is interested in and optionally send a reply with
        // ids
        myCoroutineScope.launch {
            Log.d("EncounterHandler", "Got encounters list: " + packet.profileIdslist)
            val interestedIds = mutableListOf<String>()

            val myProfile = database.loadMyProfile()

            for (id in packet.profileIdslist) {
                val existingProfile = database.getMatchProfile(id)

                // Does not exist locally case, we want this definitely
                if (existingProfile == null && id != myProfile.id) {
                    interestedIds.add(id)
                }
            }

            if (interestedIds.isNotEmpty()) {
                Log.d("EncounterHandler", "Sending request for more information about IDs: $interestedIds")
                connection.sendPacket(RequestEncountersPacket(interestedIds), listOf())
            } else {
                Log.d("EncounterHandler", "Interested Ids is empty, not sending packet")
            }


        }
    }

    /**
     * Method to handle when peer requests more information about a specific user id
     */
    private fun handleRequestEncounter(packet: RequestEncountersPacket) {
        myCoroutineScope.launch {
            Log.d("EncounterHandler", "Peer is requesting specific encounters")

            val myProfileDeferred = async { database.loadMyProfile() }
            val encountersListDeferred = async { database.getAllMatches(MAX_ENCOUNTERS_TO_GET) }

            val myProfile = myProfileDeferred.await()
            val encountersList = encountersListDeferred.await()

            packet.profileIdsList.forEach { requestedId ->

                var intendedProfile: UserProfile? = null
                var encounterTime: Long? = null

                if (myProfile != null && myProfile.id == requestedId) {
                    // Our own profile
                    intendedProfile = myProfile
                    encounterTime = System.currentTimeMillis()
                } else {
                    // Check inside saved matches
                    for (matchProfile in encountersList) {
                        if (matchProfile.userId == requestedId) {

                            // Intended match found, wrap this into a user profile object
                            val toTransmitProfile = UserProfile(
                                id = matchProfile.userId,
                                firstName = matchProfile.firstName,
                                lastName = matchProfile.lastName,
                                age = matchProfile.age,
                                description = matchProfile.description,
                                destination = matchProfile.destination,
                                isDriver = matchProfile.isDriver
                            )

                            intendedProfile = toTransmitProfile
                            encounterTime = matchProfile.firstSeenAt

                            break
                        }
                    }


                }

                if (intendedProfile != null && encounterTime != null) {
                    val transmittedEncounter = TransmittedEncounter(intendedProfile, encounterTime)

                    connection.sendPacket(EncounterPacket(transmittedEncounter), listOf())
                }

            }
        }

    }

    /**
     * Method to transmit to peer a list of userIds this client holds
     */
    private fun handleRequestEncountersList() {
        myCoroutineScope.launch {
            Log.d("EncounterHandler", "Request List handling")
            val myProfileDeferred = async { database.loadMyProfile() }
            val encountersListDeferred = async { database.getAllMatches(MAX_ENCOUNTERS_TO_GET) }

            val myProfile = myProfileDeferred.await()
            val encountersList = encountersListDeferred.await()


            val listOfIds = mutableListOf<String>()

            if (myProfile != null) {

                // Add own profile, but only if required fields are met
                var firstNameOK = false
                if (!myProfile.firstName.contentEquals("")) {
                    firstNameOK = true
                }

                var destinationOK = false
                if (myProfile.destination.location.latitude != 0.toDouble() && myProfile.destination.location.longitude != 0.toDouble()) {
                    destinationOK = true
                }

                if (firstNameOK && destinationOK) {
                    listOfIds.add(myProfile.id)
                }
            }
            encountersList.forEach { encounter ->
                listOfIds.add(encounter.userId)
            }

            if (listOfIds.isNotEmpty()) {
                Log.d("EncounterHandler", "Sending list")
                connection.sendPacket(EncountersListPacket(listOfIds), listOf())
            } else {
                Log.d("EncounterHandler", "List is empty did not send")
            }
        }
    }

    private fun onPacketReceive(combinedPacket: ParsedCombinedPacket) {
        Log.d("EncounterHandler", "Got combined packet, going into switch case")
        when (combinedPacket.metaPacket) {
            is RequestEncountersListPacket -> {
                handleRequestEncountersList()
            }
            is RequestEncountersPacket -> {
                handleRequestEncounter(combinedPacket.metaPacket)
            }
            is EncountersListPacket -> {
                handleEncounterListReceive(combinedPacket.metaPacket)
            }
            is EncounterPacket -> {
                handleEncounterReceive(combinedPacket.metaPacket)
            }
        }
    }




}