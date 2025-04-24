package org.jjdrive.entities.dto;

import lombok.Data;

@Data
public class Location {
    public double lat;
    public double lng;


    public Location(double latOrigen, double lngOrigen) {
        this.lat = latOrigen;
        this.lng = lngOrigen;
    }

    public Location() {

    }
}
