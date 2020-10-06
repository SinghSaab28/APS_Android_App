package com.aps.apsschool.welcome;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.aps.apsschool.user.R;

public class BlankWelcomeScreen extends AppCompatActivity {

    int ALL_PERMISSIONS = 101;

    final String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blank_welcome_screen);
        ActivityCompat.requestPermissions(BlankWelcomeScreen.this, permissions, ALL_PERMISSIONS);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    while(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){}
                }
                    Intent i = new Intent(getApplicationContext(), SchoolWelcomeScreen.class);
                startActivity(i);
                finish();
            }
        }, 2000);
    }
}