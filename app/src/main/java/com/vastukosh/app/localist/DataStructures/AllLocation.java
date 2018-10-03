package com.vastukosh.app.localist.DataStructures;

public class AllLocation {

    public String locationName, latitude, longitude, locationId;

    public AllLocation(String name, String lat, String lon, String id) {
        locationName = name;
        latitude = lat;
        longitude = lon;
        locationId = id;
    }

    public String getLocationName() {
        return locationName;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getLocationId() {
        return locationId;
    }
}
