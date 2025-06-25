package com.extremelygood.abfahrt.classes.utils

import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts


class RuntimePermissionsChecker(
    caller: ActivityResultCaller,
    private val permissions: Array<String>,
    private val onResult: (Map<String, Boolean>) -> Unit
) {

    private val launcher: ActivityResultLauncher<Array<String>> =
        caller.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
            onResult
        )

    fun checkPermissions() {
        launcher.launch(permissions)
    }

}
