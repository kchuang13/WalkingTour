package com.example.walkingtour;

import com.google.android.gms.location.Geofence;

import java.io.Serializable;

public class FenceData implements Serializable {

    private final String id;
    private final String address;
    private final double lat;
    private final double lon;
    private final float radius;
    private final String description;
    private final String fenceColor;
    private final String image;

    FenceData(String id, String address, double lat, double lon, float radius, String description, String fenceColor, String image) {
        this.id = id;
        this.address = address;
        this.lat = lat;
        this.lon = lon;
        this.radius = radius;
        this.description = description;
        this.fenceColor = fenceColor;
        this.image = image;
    }

    String getId() { return id;}

    String getAddress() {return address;}

    double getLat() {return lat;}

    double getLon() {return lon;}

    float getRadius() {return radius;}

    String getDescription() {return description;}

    String getFenceColor() {return fenceColor;}

    String getImage() {return image;}

}



