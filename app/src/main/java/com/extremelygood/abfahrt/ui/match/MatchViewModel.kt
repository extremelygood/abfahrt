package com.extremelygood.abfahrt.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.extremelygood.abfahrt.classes.DatabaseManager
import com.extremelygood.abfahrt.classes.MatchProfile

class MatchViewModel(application: Application) : AndroidViewModel(application) {

    private val database = DatabaseManager.getInstance(application)

    // Exponierte LiveData für das beobachtete Match-Profil
    lateinit var matchProfile: LiveData<MatchProfile?>

    /**
     * Setzt das zu beobachtende Profil anhand der userId
     */
    fun loadProfile(userId: String) {
        matchProfile = database.observeMatchProfile(userId)
    }
}
