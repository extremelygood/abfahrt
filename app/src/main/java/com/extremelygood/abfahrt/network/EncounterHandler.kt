package com.extremelygood.abfahrt.network

import com.extremelygood.abfahrt.classes.MatchProfile
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
import java.util.Date

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

            connection.sendPacket(RequestEncountersListPacket(), listOf())

        }
    }


    fun end() {
        mainJob?.cancel()
    }


    private fun handleEncounterReceive(packet: EncounterPacket) {
        myCoroutineScope.launch {
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
                encounterProfile.destination
            )

            database.saveMatchProfile(newMatchProfile)
        }
    }

    private fun handleEncounterListReceive(packet: EncountersListPacket) {
        // Select which encounters this client is interested in and optionally send a reply with
        // ids
        myCoroutineScope.launch {
            val interestedIds = mutableListOf<String>()

            packet.profileIdslist.forEach { id ->
                val existingProfile = database.getMatchProfile(id)

                // Does not exist locally case, we want this definitely
                if (existingProfile == null) {
                    interestedIds.add(id)
                }
            }

            if (interestedIds.isNotEmpty()) {
                connection.sendPacket(RequestEncountersPacket(interestedIds), listOf())
            }

        }
    }

    private fun handleImageReceive(combinedPacket: ParsedCombinedPacket) {

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
            is EncountersListPacket -> {
                handleEncounterListReceive(combinedPacket.metaPacket)
            }
            is EncounterPacket -> {
                handleEncounterReceive(combinedPacket.metaPacket)
            }
        }
    }




}