package com.extremelygood.abfahrt.classes

import android.location.Location
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
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
        val struct = decoder.beginStructure(descriptor)

        val lat = struct.decodeDoubleElement(descriptor, 0)
        val lon = struct.decodeDoubleElement(descriptor, 1)

        struct.endStructure(descriptor)

        val newLocation = Location(null)
        newLocation.latitude = lat
        newLocation.longitude = lon

        return newLocation
    }

    override fun serialize(encoder: Encoder, value: Location) {
        val struct = encoder.beginStructure(descriptor)
        struct.encodeDoubleElement(descriptor, 0, value.latitude)
        struct.encodeDoubleElement(descriptor, 1, value.longitude)
        struct.endStructure(descriptor)
    }
}