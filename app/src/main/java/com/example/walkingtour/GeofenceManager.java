package com.example.walkingtour;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.graphics.ColorUtils;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PatternItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class GeofenceManager {

    private static final String TAG = "GeofenceManager";
    private final MapsActivity mapsActivity;
    private final GeofencingClient geofencingClient;
    private PendingIntent geofencePendingIntent;

    private final HashMap<String, FenceData> buildingList = new HashMap<>();
    private final ArrayList<Circle> circles = new ArrayList<>();
    private final List<PatternItem> pattern = Collections.singletonList(new Dot());
    private static final ArrayList<FenceData> fenceList = new ArrayList<>();


    GeofenceManager(final MapsActivity mapsActivity) {
        this.mapsActivity = mapsActivity;
        geofencingClient = LocationServices.getGeofencingClient(mapsActivity);


        //safety mechanism
        geofencingClient.removeGeofences(getGeofencePendingIntent()).addOnSuccessListener(mapsActivity, eVoid -> Log.d(TAG, "onSuccess: removeGeofances"))
                .addOnFailureListener(mapsActivity, e -> {
                    e.printStackTrace();
                    Log.d(TAG, "onFailure: removeGeofences ");
                    Toast.makeText(mapsActivity, "Trouble removing existing fences " + e.getMessage(), Toast.LENGTH_LONG).show();
                });

        new Thread(new FenceDataDownloader(mapsActivity, this)).start(); //START THE JSON DOWNLOAD FOR PATHS AND FENCES
    }

    static FenceData getFenceData(String id) { //for BroadCast Receiver
        for (FenceData fd : fenceList) {
            if (fd.getId().equals(id)) {
                return fd;
            }
        }
        return null;
    }

    void drawFences() {
        for (FenceData fd: fenceList) {
            drawFence(fd);
        }
    }

    void eraseFence() {
        for (Circle c : circles) {
            c.remove();
        }
        circles.clear();
    }

    private void drawFence(FenceData fd) {
        int line = Color.parseColor(fd.getFenceColor());
        int fill = ColorUtils.setAlphaComponent(line, 85);

        LatLng latLng = new LatLng(fd.getLat(), fd.getLon());
        Circle c = mapsActivity.getMap().addCircle(new CircleOptions()
                .center(latLng)
                .radius(fd.getRadius())
                .strokePattern(pattern)
                .strokeColor(line)
                .fillColor(fill));

        circles.add(c);
    }

    void addFences(ArrayList<FenceData> fences) {

        fenceList.clear();
        fenceList.addAll(fences);

        for(FenceData fd : fenceList) {
            Geofence geofence = new Geofence.Builder()
                    .setRequestId(fd.getId())
                    .setCircularRegion(
                            fd.getLat(),
                            fd.getLon(),
                            fd.getRadius())
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setNotificationResponsiveness(1000)
                    .build();

            GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                    .addGeofence(geofence)
                    .build();

            buildingList.put(fd.getId(), fd);

            geofencePendingIntent = getGeofencePendingIntent();

            if(ActivityCompat.checkSelfPermission(mapsActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            geofencingClient
                    .addGeofences(geofencingRequest, geofencePendingIntent)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "onSuccess: addGeofences"))
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                        Log.d(TAG, "onFailure: addGeofences ");
                        Toast.makeText(mapsActivity, "Trouble adding new fence " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
        mapsActivity.runOnUiThread(this::drawFences);

    }

    private PendingIntent getGeofencePendingIntent() {
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }

        //create a new intent using mapsActivity and geofenceReceiver as receiver.
        //when marker enters or leaves the fence, it sends a broadcast to notify
        Intent intent = new Intent(mapsActivity, GeofenceBroadcastReceiver.class);
        geofencePendingIntent = PendingIntent.getBroadcast(mapsActivity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return geofencePendingIntent;
    }
}
