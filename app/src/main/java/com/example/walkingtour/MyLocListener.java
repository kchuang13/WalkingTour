package com.example.walkingtour;

import android.location.Location;
import android.location.LocationListener;

import androidx.annotation.NonNull;

public class MyLocListener implements LocationListener {


    private final MapsActivity mapsActivity;

    MyLocListener(MapsActivity mapsActivity) {this.mapsActivity = mapsActivity;}

    @Override
    public void onLocationChanged(@NonNull Location location) {
        mapsActivity.updateLocation(location);

    }


}
