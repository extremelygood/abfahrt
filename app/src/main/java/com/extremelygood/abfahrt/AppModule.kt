package com.extremelygood.abfahrt

import android.content.Context
import com.extremelygood.abfahrt.classes.DatabaseManager


interface AppModule {
    val databaseManager: DatabaseManager
    val appContext: Context
}


class AppModuleImpl(
    context: Context
): AppModule {
    override val databaseManager: DatabaseManager by lazy {
        DatabaseManager.getInstance(appContext)
    }
    override val appContext: Context = context
}