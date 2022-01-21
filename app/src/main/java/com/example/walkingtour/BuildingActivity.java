package com.example.walkingtour;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Notification;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Window;
import android.widget.ActionMenuView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toolbar;

import com.squareup.picasso.Picasso;

import java.util.Objects;

import okhttp3.internal.Util;

public class BuildingActivity extends AppCompatActivity {
    private Typeface myCustomFont;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_building);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.home_image);
        actionBar.setDisplayHomeAsUpEnabled(true);
        setTitle("");

        myCustomFont = Typeface.createFromAsset(getAssets(), "fonts/Acme-Regular.ttf");

        TextView buildingName = findViewById(R.id.buildingName);
        TextView buildingAddress = findViewById(R.id.buildingAddress);
        TextView buildingDescription = findViewById(R.id.buildingDescription);
        ImageView buildingImage = findViewById(R.id.buildingImage);

        buildingName.setTypeface(myCustomFont);
        buildingAddress.setTypeface(myCustomFont);
        buildingDescription.setTypeface(myCustomFont);

        buildingDescription.setMovementMethod(new ScrollingMovementMethod());

        FenceData fd = (FenceData) getIntent().getSerializableExtra("DATA");

        if(fd != null) {
            buildingName.setText(fd.getId());
            buildingAddress.setText(fd.getAddress());
            buildingDescription.setText(fd.getDescription());
            Picasso.get().load(fd.getImage()).into(buildingImage);
        }
    }
}