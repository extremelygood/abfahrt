package com.extremelygood.abfahrt.classes

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration

interface Expiring {
    fun startExpiring(duration: Duration, onExpired: () -> Unit = {})
    fun cancelExpiring()
}

class ExpiringDelegate(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) : Expiring {

    private var job: Job? = null

    override fun startExpiring(duration: Duration, onExpired: () -> Unit) {
        job?.cancel()
        job = scope.launch {
            delay(duration)
            onExpired()
        }
    }

    override fun cancelExpiring() { job?.cancel(); job = null }
}
