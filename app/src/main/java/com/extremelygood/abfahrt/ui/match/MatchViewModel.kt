package com.extremelygood.abfahrt.ui.match

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.extremelygood.abfahrt.classes.DatabaseManager
import com.extremelygood.abfahrt.classes.MatchProfile
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


const val MATCHES_LIMIT = 50
const val ACCEPTABLE_DISTANCE_METER = 10_000

class MatchViewModel(
    private val databaseManager: DatabaseManager
) : ViewModel() {
    private var evalMatchesJob: Job? = null

    private val _matchProfile: MutableLiveData<MatchProfile?> = MutableLiveData()
    val matchProfile: LiveData<MatchProfile?> = _matchProfile


    private fun bestMatchFound(bestMatch: MatchProfile?) {
        // No best match found case
        if (bestMatch == null) {
            if (_matchProfile.value == null) {
                return // Return here because dont want to trigger observesr over nothing
            }
            _matchProfile.value = null
            return
        }

        // Match that is currently displayed is the same as found match
        if (_matchProfile.value != null) {
            if (bestMatch.userId == _matchProfile.value!!.userId) {
                return
            }
        }

        _matchProfile.value = bestMatch
    }

    /**
     * Master method that looks for a match to display
     */
    private fun evaluateMatches() {
        evalMatchesJob?.cancel()

        evalMatchesJob = viewModelScope.launch {
            val ownProfile = databaseManager.loadMyProfile()
            if (ownProfile == null) {
                return@launch
            }

            val matchesList = databaseManager.getAllMatches(MATCHES_LIMIT)

            var closestAcceptableMatch: MatchProfile? = null
            var closestDistance: Float? = null

            for (match in matchesList) {
                // Distance check
                val distance = match.destination.location.distanceTo(ownProfile.destination.location)
                if (distance > ACCEPTABLE_DISTANCE_METER) {
                    continue
                }

                // Compare to current closest
                if (closestAcceptableMatch != null) {
                    if (closestDistance!! > distance) {
                        closestAcceptableMatch = match
                        closestDistance = distance
                    }
                } else {
                    closestAcceptableMatch = match
                    closestDistance = distance
                }

            }
            bestMatchFound(closestAcceptableMatch)
        }
    }

    init {
        databaseManager.setOnMatchesChangedListener {
            evaluateMatches()
        }
    }
}
