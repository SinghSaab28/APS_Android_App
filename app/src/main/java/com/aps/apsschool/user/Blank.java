package com.aps.apsschool.user;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.aps.apsschool.home.SelectSubject;

public class Blank extends AppCompatActivity {

    private Boolean exit = false;
    int c=0;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blank);
        handler = new Handler();
        Toast.makeText(this, "Press Back again to EXIT or wait 3 for secs to get redirected", Toast.LENGTH_LONG).show();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                check();
            }
        }, 1000);
    }

    @Override
    public void onBackPressed() {
        exit = true;
//        if (exit) {
//            finish();
//        } else {
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    Intent i = new Intent(Blank.this, SelectSubject.class);
//                    startActivity(i);
//                    finish();
//                }
//            }, 3 * 1000);
//
//        }
    }

    private void check(){
        if(exit==true){
            finish();
        }else if(c==3){
            handler.removeCallbacksAndMessages(null);
            Intent i = new Intent(Blank.this, SelectSubject.class);
            startActivity(i);
            finish();
        }else{
            c++;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    check();
                }
            }, 1000);
        }
    }
}