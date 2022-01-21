package com.example.walkingtour;

import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FenceDataDownloader implements Runnable {

    private static final String TAG = "FenceDataDownloader";
    private final Geocoder geocoder;
    private final GeofenceManager fenceMgr;
    private final MapsActivity mapsActivity;

    private static final String FENCE_URL = "http://www.christopherhield.com/data/WalkingTourContent.json";

    FenceDataDownloader(MapsActivity mapsActivity, GeofenceManager fenceMgr) {
        this.mapsActivity = mapsActivity;
        this.fenceMgr = fenceMgr;
        geocoder = new Geocoder(mapsActivity);
    }

    private void processData(String result){
        if(result == null) {
            return;
        }

        ArrayList<FenceData> fences = new ArrayList<>();
        ArrayList<LatLng> path = new ArrayList<>();
        HashMap<String, FenceData> buildingMap = new HashMap<>();
        try {
            JSONObject jObj = new JSONObject(result);
            JSONArray jArr = jObj.getJSONArray("fences");
            JSONArray JPathArr = jObj.getJSONArray("path");

            for (int i = 0; i < JPathArr.length(); i++) {
                String[] tourPath = (JPathArr.getString(i).split(", "));
                double lat = Double.parseDouble(tourPath[1]);
                double lon = Double.parseDouble(tourPath[0]);
                LatLng location = new LatLng(lat, lon);
                path.add(location);
            }

            for (int i = 0; i < jArr.length(); i++) {
                JSONObject fObj = jArr.getJSONObject(i);
                String id = fObj.getString("id");
                String address = fObj.getString("address");
                double latitude = fObj.getDouble("latitude");
                double longitude = fObj.getDouble("longitude");
                float radius = (float) fObj.getDouble("radius");
                String description = fObj.getString("description");
                String fenceColor = fObj.getString("fenceColor");
                String image = fObj.getString("image");

                FenceData fd = new FenceData(id, address, latitude, longitude, radius, description, fenceColor, image);
                fences.add(fd);
                buildingMap.put(fd.getId(), fd);
            }

            fenceMgr.addFences(fences);
            mapsActivity.runOnUiThread(() -> mapsActivity.addTourPath(path));


        } catch (Exception e) {
            e.printStackTrace();
            }
    }

    public void run() {

        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(FENCE_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "run: Response code: " + connection.getResponseCode());
                return;
            }

            InputStream stream = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuilder buffer = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            processData(buffer.toString());

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
