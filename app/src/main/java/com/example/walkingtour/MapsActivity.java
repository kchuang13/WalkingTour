package com.example.walkingtour;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";
    private static final int LOCATION_REQUEST = 111;
    private static final int ACCURACY_REQUEST = 222;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private GeofenceManager fenceMgr;
    private Geocoder geocoder;
    private Polyline llHistoryPolyLine;
    private Polyline tourPathPolyLine;
    private Marker walker;
    public static int screenWidth;

    private TextView addressText;

    private final ArrayList<LatLng> latLongHistory = new ArrayList<>(); //store location updates

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        fenceMgr = new GeofenceManager(this);

        addressText = findViewById(R.id.location);

        checkLocationAccuracy();

        geocoder = new Geocoder(this);
    }

    public void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if(mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng origin = new LatLng(41.88673, -87.62);
        mMap.addMarker(new MarkerOptions().alpha(0.5f).position(origin).title("Tour Starting Point"));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16.0f));


        //required in app
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.setBuildingsEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        if(checkPermission()) {
            setupLocationListener();
        }
    }

    public void addTourPath(ArrayList<LatLng> tourPathCords) {
        PolylineOptions polylineOptions = new PolylineOptions();
        for (LatLng tourPath : tourPathCords) {
            polylineOptions.add(tourPath);
        }
        tourPathPolyLine = mMap.addPolyline(polylineOptions);
        tourPathPolyLine.setEndCap(new RoundCap());
        tourPathPolyLine.setWidth(12);
        tourPathPolyLine.setColor(Color.YELLOW);
    }


    public void updateLocation(Location location) {

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        latLongHistory.add(latLng); //list of LatLng Object

        try{
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            Address address = addresses.get(0);
            addressText.setText(address.getAddressLine(0));
        } catch (IOException e) {
            e.printStackTrace();
            addressText.setText("");
        }

        if(llHistoryPolyLine != null) {
            llHistoryPolyLine.remove();
        }

        if (latLongHistory.size() == 1) {
            //mMap.addMarker(new MarkerOptions().alpha(0.5f).position(latLng).title("My Origin"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0f));
            return;
        }

        if(latLongHistory.size() > 1) {
            PolylineOptions polylineOptions = new PolylineOptions();

            for(LatLng ll : latLongHistory) {
                polylineOptions.add(ll);
            }
            llHistoryPolyLine = mMap.addPolyline(polylineOptions);
            llHistoryPolyLine.setEndCap(new RoundCap());
            llHistoryPolyLine.setWidth(12);
            llHistoryPolyLine.setColor(getResources().getColor(R.color.line_green));
        }


        //marker: walker icon
        float r = getRadius();
        if( r > 0) {
            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.walker_left);
            Bitmap resized = Bitmap.createScaledBitmap(icon, (int)r, (int)r, false);
            BitmapDescriptor iconBitmap = BitmapDescriptorFactory.fromBitmap(resized);

            MarkerOptions options = new MarkerOptions();
            options.position(latLng);
            options.icon(iconBitmap);
            options.rotation(location.getBearing());

            if ( walker != null) {
                walker.remove();
            }
            walker = mMap.addMarker(options);
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0f));
    }


    private float getRadius() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        float z= mMap.getCameraPosition().zoom;
        float factor = (float) ((35.0/2.0 * z) - (355.0/2.0));
        float multiplier = ((7.0f/7200.0f) * screenWidth) - (1.0f / 200.f);
        return factor * multiplier;
    }

    public void checkBoxGeofences(View v) {
        CheckBox showGeofences = (CheckBox) v;
        if(showGeofences.isChecked()) {
            fenceMgr.drawFences();
        } else {
            fenceMgr.eraseFence();
        }
    }

    public void checkBoxTourPath(View v) {
        CheckBox showTourPath = (CheckBox) v;
        if(showTourPath.isChecked()) {
            tourPathPolyLine.setVisible(true);
        } else {
            tourPathPolyLine.setVisible(false);
        }

    }

    public void checkBoxTravelPath(View v) {
        CheckBox showTravelPath = (CheckBox) v;
        if(showTravelPath.isChecked()) {
            llHistoryPolyLine.setVisible(true);
        } else {
            llHistoryPolyLine.setVisible(false);
        }
    }

    public void checkBoxAddresses(View v) {
        CheckBox showAddress = (CheckBox) v;
        if(showAddress.isChecked()) {
            addressText.setTextColor(getResources().getColor(android.R.color.white));
        } else {
            addressText.setTextColor(getResources().getColor(R.color.notification_bar));
        }
    }

    public void setupLocationListener() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocListener(this);

        if(checkPermission() && locationManager != null) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
        }
    }

    public GoogleMap getMap() {return mMap;}

    protected void onDestroy() {
        super.onDestroy();
        if(locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }
        NotificationManagerCompat.from(this).cancelAll();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkPermission() && locationManager != null && locationListener != null)
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, locationListener);
    }

    private boolean checkPermission() {
        ArrayList<String> perms = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            perms.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {
                perms.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
            }
        }

        if (!perms.isEmpty()) {
            String[] array = perms.toArray(new String[0]);
            ActivityCompat.requestPermissions(this,
                    array, LOCATION_REQUEST);
            return false;
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_REQUEST) {
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        setupLocationListener();
                    } else {
                        Toast.makeText(this, "Location Permission not Granted", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private void checkLocationAccuracy() {

        Log.d(TAG, "checkLocationAccuracy: ");
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, locationSettingsResponse -> {
            Log.d(TAG, "onSuccess: High Accuracy Already Present");
            initMap();
        });

        task.addOnFailureListener(this, e -> {
            if (e instanceof ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(MapsActivity.this, ACCURACY_REQUEST);
                } catch (IntentSender.SendIntentException sendEx) {
                    sendEx.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACCURACY_REQUEST && resultCode == RESULT_OK) {
            Log.d(TAG, "onActivityResult: ");
            initMap();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("High-Accuracy Location Services Required");
            builder.setMessage("High-Accuracy Location Services Required");
            builder.setPositiveButton("OK", (dialog, id) -> finish());
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }



}