package com.extremelygood.abfahrt

import android.content.Context
import com.extremelygood.abfahrt.classes.DatabaseManager


interface AppModule {
    val databaseManager: DatabaseManager;
}


class AppModuleImpl(
    private val appContext: Context
): AppModule {
    override val databaseManager: DatabaseManager by lazy {
        DatabaseManager.getInstance(appContext)
    }
}