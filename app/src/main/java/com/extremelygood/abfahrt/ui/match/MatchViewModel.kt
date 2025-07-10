package com.extremelygood.abfahrt.ui.match

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.extremelygood.abfahrt.classes.DatabaseManager
import com.extremelygood.abfahrt.classes.MatchProfile

class MatchViewModel(
    private val databaseManager: DatabaseManager
) : ViewModel() {


    // Exponierte LiveData für das beobachtete Match-Profil
    lateinit var matchProfile: LiveData<MatchProfile?>

    /**
     * Setzt das zu beobachtende Profil anhand der userId
     */
    fun loadProfile(userId: String) {
        matchProfile = databaseManager.observeMatchProfile(userId)
    }
}
