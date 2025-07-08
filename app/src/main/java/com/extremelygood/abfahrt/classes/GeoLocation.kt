package com.extremelygood.abfahrt.classes

import android.location.Location
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
class GeoLocation(
    val locationName: String = "DefaultLocationName",

    @Serializable(with = AndroidLocationSerializer::class)
    val location: Location = Location(null)
) {
}


object AndroidLocationSerializer : KSerializer<Location> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("Location") {
            element<Double>("latitude")
            element<Double>("longitude")
        }



    override fun deserialize(decoder: Decoder): Location {
        val dec = decoder.beginStructure(descriptor)

        var lat = 0.0
        var lon = 0.0

        loop@ while (true) {
            when (val index = dec.decodeElementIndex(descriptor)) {
                CompositeDecoder.DECODE_DONE -> break@loop
                0 -> lat = dec.decodeDoubleElement(descriptor, 0)
                1 -> lon = dec.decodeDoubleElement(descriptor, 1)
                else -> throw SerializationException("Unexpected index $index")
            }
        }
        dec.endStructure(descriptor)

        return Location("").apply {
            latitude = lat
            longitude = lon
        }
    }

    override fun serialize(encoder: Encoder, value: Location) {
        val struct = encoder.beginStructure(descriptor)
        struct.encodeDoubleElement(descriptor, 0, value.latitude)
        struct.encodeDoubleElement(descriptor, 1, value.longitude)
        struct.endStructure(descriptor)
    }
}