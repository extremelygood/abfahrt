package com.extremelygood.abfahrt.classes

import android.location.Location

class GeoLocation(
    private var locationName: String = "DefaultLocationName",
    private var location: Location = Location(null)
) {
}