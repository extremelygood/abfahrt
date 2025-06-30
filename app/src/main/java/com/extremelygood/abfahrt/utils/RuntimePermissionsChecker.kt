package com.extremelygood.abfahrt.utils

import android.content.Context
import android.content.DialogInterface
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.dialog.MaterialAlertDialogBuilder

const val MAX_TRIES = 2;

class RuntimePermissionsChecker(
    private val context: Context,
    private val caller: ActivityResultCaller,
    private val permissions: Array<String>,
    private val onResult: (resultMap: Map<String, Boolean>, fullSuccess: Boolean) -> Unit,

    private var triesCount: Int = 0,
) {


    private val launcher: ActivityResultLauncher<Array<String>> =
        caller.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
            ::permissionsResultReceived,
        )

    fun checkPermissions() {
        launcher.launch(permissions)
        triesCount++
    }

    private fun permissionsResultReceived(resultMap: Map<String, Boolean>) {
        val fullSuccessFound = !resultMap.values.contains(false)

        // Case where the user denied but we want to reason and try again
        if (!fullSuccessFound && triesCount < MAX_TRIES) {
            showDialog {
                launcher.launch(permissions)
                triesCount++
            }
            return
        }

        // Case where retries have been exhausted. Show the dialog and stop trying
        if (!fullSuccessFound && triesCount > MAX_TRIES) {
            showDialog {}
        }

        // State the result
        onResult(resultMap, fullSuccessFound)
    }

    private fun showDialog(onDialogFinished: () -> Unit) {
        val dialogBuilder = MaterialAlertDialogBuilder(context)
        dialogBuilder.setTitle("Critical requirements")
        dialogBuilder.setMessage("Permissions are required for the app to function. Allow access or manually set in your device settings.")

        dialogBuilder.setPositiveButton("ACCEPT", { dialogInterface: DialogInterface, which: Int ->
            onDialogFinished()
        })
        dialogBuilder.setOnCancelListener({dialogInterface: DialogInterface ->
            onDialogFinished()
        })


        dialogBuilder.show()
    }

}
