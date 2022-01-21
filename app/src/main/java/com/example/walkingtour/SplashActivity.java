package com.example.walkingtour;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Locale;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    private static final int SPLASH_TIME_OUT = 3000;
    private static final int LOC_COMBO_REQUEST = 111;
    private static final int LOC_ONLY_PERM_REQUEST = 222;
    private static final int BGLOC_ONLY_PERM_REQUEST = 333;
    private static final int ACCURACY_REQUEST = 444;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //check perm here
        checkLocationAccuracy();

    }
    private boolean checkPermission() {

        // If R or greater, need to ask for these separately
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOC_ONLY_PERM_REQUEST);
                return false;
            }
            return true;

        } else {

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
                        array, LOC_COMBO_REQUEST);
                return false;
            }
        }

        return true;
    }

    private void checkLocationAccuracy() {

        if(checkPermission()) {
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
            SettingsClient client = LocationServices.getSettingsClient(this);
            Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

            task.addOnSuccessListener(this, locationSettingsResponse -> {
                Log.d(TAG, "onSuccess: High Accuracy Already Present");
                Intent i = new Intent(SplashActivity.this, MapsActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                finish();

            });

            task.addOnFailureListener(this, e-> {
                if (e instanceof ResolvableApiException) {
                    try {
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(SplashActivity.this, ACCURACY_REQUEST);
                    } catch (IntentSender.SendIntentException sendEx) {
                        sendEx.printStackTrace();
                    }
                }
            });
        }
    }

    public void requestBgPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BGLOC_ONLY_PERM_REQUEST);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOC_ONLY_PERM_REQUEST) {
            if (permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestBgPermission();
            } else {
                AlertDialog.Builder builder= new AlertDialog.Builder(this);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                builder.setMessage("Walking Tour cannot be used without location and background permissions");
                builder.setTitle("Permissions required");
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        } else if (requestCode == LOC_COMBO_REQUEST) {
            int permCount = permissions.length;
            int permSum = 0;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    permSum++;
                } else {
                    sb.append(permissions[i]).append(", ");
                }
            }
            if (permSum == permCount) {
                checkLocationAccuracy();
            } else {
                AlertDialog.Builder builder= new AlertDialog.Builder(this);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                builder.setMessage("Walking Tour cannot be used without location and background permissions");
                builder.setTitle("Permissions required");
                AlertDialog dialog = builder.create();
                dialog.show();
//                Toast.makeText(this,
//                        "Required permissions not granted: " + sb.toString(),
//                        Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == BGLOC_ONLY_PERM_REQUEST) {
            if (permissions[0].equals(Manifest.permission.ACCESS_BACKGROUND_LOCATION) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocationAccuracy();
            } else {
                AlertDialog.Builder builder= new AlertDialog.Builder(this);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                builder.setMessage("Walking Tour cannot be used without location and background permissions");
                builder.setTitle("Permissions required");
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACCURACY_REQUEST && resultCode == RESULT_OK) {
                Intent i = new Intent(SplashActivity.this, MapsActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                finish();

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













//
//    private void determineLocation() {
//        if(checkPermission()) {
//            locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
//            locationListener = new MyLocListener(this);
//
//            if(locationManager != null) {
//                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
//            }
//        }
//    }
//
//    private boolean checkPermission() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOC_ONLY_PERM_REQUEST);
//                return false;
//            }
//            return true;
//        } else {
//            ArrayList<String> perms = new ArrayList<>();
//
//            if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) !=
//                    PackageManager.PERMISSION_GRANTED) {
//                perms.add(Manifest.permission.ACCESS_FINE_LOCATION);
//            }
//
//            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) !=
//                        PackageManager.PERMISSION_GRANTED) {
//                    perms.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
//                }
//            }
//
//            if (!perms.isEmpty()) {
//                String[] array = perms.toArray(new String[0]);
//                ActivityCompat.requestPermissions(this, array, LOC_COMBO_REQUEST);
//                return false;
//            }
//        }
//        return true;
//    }
//
//    public void requestBgPermission() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//
//            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BGLOC_ONLY_PERM_REQUEST);
//            }
//        }
//    }
//
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        if(requestCode == LOC_ONLY_PERM_REQUEST) {
//            if(permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                requestBgPermission();
//            }
//        } else if (requestCode == LOC_COMBO_REQUEST) {
//            int permCount = permissions.length;
//            int permSum = 0;
//            StringBuilder sb = new StringBuilder();
//            for (int i = 0; i < permissions.length; i++) {
//                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
//                    permSum++;
//                } else {
//                    sb.append(permissions[i]).append(", ");
//                }
//            }
//            if (permSum == permCount) {
//                determineLocation();
//            } else {
//                Toast.makeText(this, "Required permissions not grated: " + sb.toString(), Toast.LENGTH_LONG).show();
//            }
//        } else if  (requestCode == BGLOC_ONLY_PERM_REQUEST) {
//            if(permissions[0].equals(Manifest.permission.ACCESS_BACKGROUND_LOCATION) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                determineLocation();
//            }
//        }
//    }

