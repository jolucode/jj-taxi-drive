package org.jjdrive.entities.dto;

public class LocationRequest {
    public String userId;
    public double lat;
    public double lng;

    public LocationRequest() {}

    public LocationRequest(String userId, double lat, double lng) {
        this.userId = userId;
        this.lat = lat;
        this.lng = lng;
    }
}
