package com.extremelygood.abfahrt.classes

import android.location.Location
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure


@Serializable
data class GeoLocation(
    val locationName: String = "DefaultLocationName",

    @Serializable(with = AndroidLocationSerializer::class)
    val location: Location = Location(PROVIDER_MANUAL)
) {

    companion object {
        const val PROVIDER_MANUAL = "manual"
    }
}


object AndroidLocationSerializer : KSerializer<Location> {
    private const val LAT_INDEX = 0
    private const val LON_INDEX = 1

    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("Location") {
            element<Double>("latitude")
            element<Double>("longitude")
        }

    override fun serialize(encoder: Encoder, value: Location) =
        encoder.encodeStructure(descriptor) {
            encodeDoubleElement(descriptor, LAT_INDEX, value.latitude)
            encodeDoubleElement(descriptor, LON_INDEX, value.longitude)
        }

    override fun deserialize(decoder: Decoder): Location =
        decoder.decodeStructure(descriptor) {
            var lat: Double? = null
            var lon: Double? = null

            while (true) when (decodeElementIndex(descriptor)) {
                LAT_INDEX  -> lat = decodeDoubleElement(descriptor, LAT_INDEX)
                LON_INDEX  -> lon = decodeDoubleElement(descriptor, LON_INDEX)
                CompositeDecoder.DECODE_DONE -> break
                else -> throw SerializationException("Unexpected element index")
            }

            if (lat == null || lon == null) {
                throw SerializationException("Missing latitude and/or longitude")
            }

            Location(GeoLocation.PROVIDER_MANUAL).apply {
                latitude  = lat
                longitude = lon
            }
        }


}
