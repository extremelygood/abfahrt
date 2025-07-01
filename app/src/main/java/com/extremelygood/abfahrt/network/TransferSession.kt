package com.extremelygood.abfahrt.network

import android.util.Printer
import com.extremelygood.abfahrt.network.packets.BaseDataPacket
import com.extremelygood.abfahrt.network.packets.ParsedCombinedPacket
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.Payload.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.exp
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds


typealias onFailCallback = () -> Unit
typealias onExpireCallback = () -> Unit
typealias onFinishedCallback = (wrappedPacket: ParsedCombinedPacket) -> Unit

val EXPIRE_TIME: Duration = 60.seconds


/**
 * Class to allow for aggregating files to a dataPacket
 */
class DataPacketTransferSession(
    private val dataPacket: BaseDataPacket,

) {
    private var onFinished: onFinishedCallback? = null
    private var onExpired: onExpireCallback? = null
    private var onFail: onFailCallback? = null

    private val files: MutableMap<Long, ImageTransferSession> = mutableMapOf()

    private fun finish() {

        val filesMap: MutableMap<Long, File> = mutableMapOf()
        files.forEach { (id, transferSession) ->
            filesMap[id] = transferSession.payload.asFile()!!
        }

        val finishedPacket = ParsedCombinedPacket(dataPacket, filesMap)

        onFinished?.invoke(finishedPacket)

        TODO("Add expiration timer cancel here")
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
    }

    fun setOnExpiredCallback(callback: onExpireCallback) {
        onExpired = callback
    }

    fun setOnFailCallback(callback: onFailCallback) {
        onFail = callback
    }


}


typealias OnSuccessCallback = () -> Unit

class ImageTransferSession(
    val payload: Payload,
) {
    init {
        startExpirationTimer()
    }

    var isSuccess = false

    private var onFailCallback: onFailCallback? = null
    private var onExpireCallback: onExpireCallback? = null
    private var onSuccessCallback: OnSuccessCallback? = null

    private var expirationJob: Job? = null




    fun setOnFailCallback(onFail: onFailCallback) {
        onFailCallback = onFail
    }

    fun setOnSuccessCallback(onSuccess: OnSuccessCallback) {
        onSuccessCallback = onSuccess
    }

    private fun setOnExpiredCallback(onExpired: onExpireCallback) {
        onExpireCallback = onExpired
    }

    private fun startExpirationTimer() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        expirationJob = scope.launch {
            delay(EXPIRE_TIME)
            onExpired()
        }
    }

    fun transferSuccess() {
        isSuccess = true
        expirationJob?.cancel()
    }

    fun fail() {
        expirationJob?.cancel()
        onFailCallback?.invoke()
    }

    private fun onExpired() {
        if (!isSuccess) {
            fail()
        }
        onExpireCallback?.invoke()
    }


}