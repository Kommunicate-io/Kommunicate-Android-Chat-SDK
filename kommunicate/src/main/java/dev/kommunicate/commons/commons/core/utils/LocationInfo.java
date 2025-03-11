package dev.kommunicate.commons.commons.core.utils;

import dev.kommunicate.commons.json.JsonMarker;

public class LocationInfo extends JsonMarker {

    public double lat = 0.0;
    public double lon = 0.0;

    public LocationInfo(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

}
