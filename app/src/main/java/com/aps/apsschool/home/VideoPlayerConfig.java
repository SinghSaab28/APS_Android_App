package com.aps.apsschool.home;

public class VideoPlayerConfig {
    //Minimum Video you want to buffer while Playing
    public static final int MIN_BUFFER_DURATION = 5000;
    //Max Video you want to buffer during PlayBack
    public static final int MAX_BUFFER_DURATION = 5000;
    //Min Video you want to buffer before start Playing it
    public static final int MIN_PLAYBACK_START_BUFFER = 1500;
    //Min video You want to buffer when user resumes video
    public static final int MIN_PLAYBACK_RESUME_BUFFER = 5000;
    public static final String DEFAULT_VIDEO_URL =
            "https://firebasestorage.googleapis.com/v0/b/fiery-muse-287609.appspot.com/o/lectures%2FVIDEO_20201005_212308.mp4?alt=media&token=c669950b-5b5b-446d-86fa-5e269c24e621";
}
