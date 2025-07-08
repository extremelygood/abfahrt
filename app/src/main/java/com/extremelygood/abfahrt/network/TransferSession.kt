package com.extremelygood.abfahrt.network

import com.extremelygood.abfahrt.classes.Expiring
import com.extremelygood.abfahrt.classes.ExpiringDelegate
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.Payload.File
import kotlin.time.Duration

typealias onFailCallback = () -> Unit
typealias onFinishedCallback = (wrappedPacket: ParsedCombinedPacket) -> Unit

/**
 * Class to allow for aggregating files to a dataPacket
 */
class DataPacketTransferSession(
    private val dataPacket: BaseDataPacket,
    private val expireTime: Duration
): Expiring by ExpiringDelegate() {


    private var onFinished: onFinishedCallback? = null
    private var onFail: onFailCallback? = null

    private val files: MutableMap<Long, ImageTransferSession> = mutableMapOf()

    private fun finish() {
        cancelExpiring()

        val filesMap: MutableMap<Long, File> = mutableMapOf()
        files.forEach { (id, transferSession) ->
            filesMap[id] = transferSession.payload.asFile()!!
        }

        val finishedPacket = ParsedCombinedPacket(dataPacket, filesMap)

        onFinished?.invoke(finishedPacket)

    }

    private fun checkFinished() {
        // Check if all associated files are indexed
        var notFound = false
        dataPacket.associatedFileIds.forEach({ id ->
            if (!files.containsKey(id)) {
                notFound = true
            }
        })
        if (notFound) {
            return
        }


        // Check if they are finished
        notFound = false
        files.forEach({ (id, imageSession) ->
            if (!imageSession.isSuccess) {
                notFound = true
            }
        })
        if (notFound) {
            return
        }

        // All checks passed, finish
        finish()
    }

    // If any part of the session fails, everything fails
    private fun fail() {
        cancelExpiring()

        onFail?.invoke()
    }

    fun offerImage(imageSession: ImageTransferSession) {
        if (!dataPacket.associatedFileIds.contains(imageSession.payload.id)) {
            return
        }

        files[imageSession.payload.id] = imageSession

        imageSession.setOnFailCallback {
            fail()
        }

        imageSession.setOnSuccessCallback {
            checkFinished()
        }

        checkFinished()
    }

    fun setOnFinishedCallback(callback: onFinishedCallback) {
        onFinished = callback
        checkFinished()
    }

    fun setOnFailCallback(callback: onFailCallback) {
        onFail = callback
    }


    private fun onExpired() {
        fail()
    }

    init {
        startExpiring(expireTime, ::onExpired)
    }
}


typealias OnSuccessCallback = () -> Unit

class ImageTransferSession(
    val payload: Payload,
    private val expireTime: Duration
): Expiring by ExpiringDelegate() {
    init {
        startExpiring(expireTime, ::onExpired)
    }

    var isSuccess = false

    private var onFailCallback: onFailCallback? = null
    private var onSuccessCallback: OnSuccessCallback? = null



    fun setOnFailCallback(onFail: onFailCallback) {
        onFailCallback = onFail
    }

    fun setOnSuccessCallback(onSuccess: OnSuccessCallback) {
        onSuccessCallback = onSuccess
    }

    fun transferSuccess() {
        isSuccess = true
        cancelExpiring()
        onSuccessCallback?.invoke()
    }

    fun fail() {
        cancelExpiring()
        onFailCallback?.invoke()
    }

    private fun onExpired() {
        if (!isSuccess) {
            fail()
        }
    }


}