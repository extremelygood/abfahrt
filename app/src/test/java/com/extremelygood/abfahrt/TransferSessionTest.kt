package com.extremelygood.abfahrt

import com.extremelygood.abfahrt.network.ImageTransferSession
import com.extremelygood.abfahrt.network.DataPacketTransferSession
import com.extremelygood.abfahrt.network.BaseDataPacket
import com.google.android.gms.nearby.connection.Payload
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.milliseconds


private class StubDataPacket(
    override var associatedFileIds: MutableList<Long>
) : BaseDataPacket()

val DEFAULT_EXPIRE_TIME = 50.milliseconds

class DataPacketTransferSessionTest {


    @Test
    fun `session finishes when all required file transfers succeed`() {
        val ids = mutableListOf(1L, 2L)
        val dataPacket = StubDataPacket(ids)
        val session = DataPacketTransferSession(dataPacket, DEFAULT_EXPIRE_TIME)

        val finished = CountDownLatch(1)
        session.setOnFinishedCallback { combined ->
            assert(ids.toSet() == combined.files.keys)
            finished.countDown()
        }

        ids.forEach { id ->
            val payload = mockPayload(id)
            val imageSession = ImageTransferSession(payload, DEFAULT_EXPIRE_TIME)
            session.offerImage(imageSession)
            imageSession.transferSuccess()
        }

        assert(finished.await(250, TimeUnit.MILLISECONDS))
    }

    @Test
    fun `session fails when any image session fails`() {
        val dataPacket = StubDataPacket(mutableListOf(42L))
        val session = DataPacketTransferSession(dataPacket, DEFAULT_EXPIRE_TIME)

        val failed = CountDownLatch(1)
        session.setOnFailCallback { failed.countDown() }

        val payload = mockPayload(42L)
        val imageSession = ImageTransferSession(payload, DEFAULT_EXPIRE_TIME)
        session.offerImage(imageSession)

        imageSession.fail()

        assert(failed.await(250, TimeUnit.MILLISECONDS))
    }

    private fun mockPayload(id: Long): Payload {
        val p = mockk<Payload>(relaxed = true)
        every { p.id } returns id
        every { p.asFile() } returns mockk()
        return p
    }
}

class ImageTransferSessionTest {

    @Test
    fun `transferSuccess marks success and calls callback`() {
        val payload = mockk<Payload>()
        every { payload.id } returns 99L

        val session = ImageTransferSession(payload, DEFAULT_EXPIRE_TIME)

        var successFlag = false
        session.setOnSuccessCallback { successFlag = true }

        session.transferSuccess()

        assert(session.isSuccess)
        assert(successFlag)
    }


    @Test
    fun `fail triggers callback`() {
        val payload = mockk<Payload>()
        every { payload.id } returns 7L

        val session = ImageTransferSession(payload, DEFAULT_EXPIRE_TIME)

        val failed = CountDownLatch(1)
        session.setOnFailCallback { failed.countDown() }

        session.fail()

        assert(failed.await(250, TimeUnit.MILLISECONDS))
    }
}
