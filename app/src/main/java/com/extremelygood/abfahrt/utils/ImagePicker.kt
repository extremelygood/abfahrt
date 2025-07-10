package com.extremelygood.abfahrt.utils

import android.content.Context
import android.net.Uri
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import java.net.URI

/**
 * Class to get an image from a user
 */
class ImagePicker(
    private val context: Context,
    private val activityResultCaller: ActivityResultCaller,
    private val onResult: (Uri?) -> Unit
) {

    private val singleImagePickerLauncher = activityResultCaller.registerForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
        onResult
    )

    fun startPickImage() {
        singleImagePickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }
}