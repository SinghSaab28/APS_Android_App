package com.aps.apsschool.upload;/*
* By Jorge E. Hernandez (@lalongooo) 2015
* */

import android.app.Application;

import com.aps.apsschool.upload.file.FileUtils;

public class VideoCompressorApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FileUtils.createApplicationFolder();
    }

}