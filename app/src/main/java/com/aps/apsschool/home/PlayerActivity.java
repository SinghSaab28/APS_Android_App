/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aps.apsschool.home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.aps.apsschool.database.DBActivities;
import com.aps.apsschool.user.R;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;

/**
 * A fullscreen activity to play audio or video streams.
 */
public class PlayerActivity extends AppCompatActivity {

  private PlaybackStateListener playbackStateListener;
  private static final String TAG = PlayerActivity.class.getName();

  private PlayerView playerView;
  private SimpleExoPlayer player;
  private boolean playWhenReady = true;
  private int currentWindow = 0;
  private long playbackPosition = 0;
  private String courseProgress = null;
  private DBActivities dbActivities = null;
  private int minBufferMs = 5000;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_player);

    playerView = findViewById(R.id.video_view);

//    Allocator allocator = new DefaultAllocator(minBufferMs);
//    DataSource dataSource = new DefaultUriDataSource(this, null, userAgent);
//    ExtractorSampleSource sampleSource = new ExtractorSampleSource( Uri.parse(url), dataSource, allocator,
//            BUFFER_SEGMENT_COUNT * BUFFER_SEGMENT_SIZE);

    playbackStateListener = new PlaybackStateListener();

    dbActivities = new DBActivities(getApplicationContext());
    courseProgress = dbActivities.getCoursesProgress(getApplicationContext(), SelectCourse.VIDEO_SELECTED);
    System.out.println("Course progress : "+courseProgress);
  }

  @Override
  public void onStart() {
    super.onStart();
    if (Util.SDK_INT > 23) {
      initializePlayer();
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    hideSystemUi();
    if ((Util.SDK_INT <= 23 || player == null)) {
      initializePlayer();
    }
  }

  @Override
  public void onPause() {
    super.onPause();
    if (Util.SDK_INT <= 23) {
      dbActivities.setCoursesProgress(getApplicationContext(), SelectCourse.VIDEO_SELECTED, (int) player.getCurrentPosition());
      releasePlayer();
    }
  }

  @Override
  public void onStop() {
    super.onStop();
    if (Util.SDK_INT > 23) {
      releasePlayer();
    }
  }

  private void initializePlayer() {
    if (player == null) {
//      DefaultTrackSelector trackSelector = new DefaultTrackSelector(this);
//      trackSelector.setParameters(
//              trackSelector.buildUponParameters().setMaxVideoSizeSd());
      // 1. Create a default TrackSelector
      LoadControl loadControl = new DefaultLoadControl(
              new DefaultAllocator(true, 16),
              VideoPlayerConfig.MIN_BUFFER_DURATION,
              VideoPlayerConfig.MAX_BUFFER_DURATION,
              VideoPlayerConfig.MIN_PLAYBACK_START_BUFFER,
              VideoPlayerConfig.MIN_PLAYBACK_RESUME_BUFFER, -1, true);
//      BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
      TrackSelection.Factory videoTrackSelectionFactory =
              new AdaptiveTrackSelection.Factory();
      TrackSelector trackSelector =
              new DefaultTrackSelector(videoTrackSelectionFactory);
//      player = new SimpleExoPlayer.Builder(this)
//              .setTrackSelector(trackSelector)
//              .build();
      player = ExoPlayerFactory.newSimpleInstance(this, new DefaultRenderersFactory(this), trackSelector, loadControl);
    }

    playerView.setPlayer(player);
    MediaItem mediaItem = new MediaItem.Builder()
            .setUri(SelectCourse.VIDEO_SELECTED)
            .setMimeType(MimeTypes.APPLICATION_MP4)
            .build();
    player.setMediaItem(mediaItem);

    player.setPlayWhenReady(playWhenReady);
    if(courseProgress!=null) {
      player.seekTo(currentWindow, Integer.parseInt(courseProgress));
    }else {
      player.seekTo(currentWindow, playbackPosition);
    }
    player.addListener(playbackStateListener);
    player.prepare();
  }

  private void releasePlayer() {
    if (player != null) {
      playbackPosition = player.getCurrentPosition();
      currentWindow = player.getCurrentWindowIndex();
      playWhenReady = player.getPlayWhenReady();
      player.removeListener(playbackStateListener);
      player.release();
      player = null;
    }
  }

  @SuppressLint("InlinedApi")
  private void hideSystemUi() {
    playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
  }

  private class PlaybackStateListener implements Player.EventListener{

    @Override
    public void onPlaybackStateChanged(int playbackState) {
      String stateString;
      switch (playbackState) {
        case ExoPlayer.STATE_IDLE:
          stateString = "ExoPlayer.STATE_IDLE      -";
          break;
        case ExoPlayer.STATE_BUFFERING:
          stateString = "ExoPlayer.STATE_BUFFERING -";
          break;
        case ExoPlayer.STATE_READY:
          stateString = "ExoPlayer.STATE_READY     -";
          break;
        case ExoPlayer.STATE_ENDED:
          stateString = "ExoPlayer.STATE_ENDED     -";
          break;
        default:
          stateString = "UNKNOWN_STATE             -";
          break;
      }
      Log.d(TAG, "changed state to " + stateString);
    }
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    DBActivities dbActivities = new DBActivities(getApplicationContext());
    if(player.getCurrentPosition() > (player.getDuration()-2*1000)){
      dbActivities.setCoursesProgress(getApplicationContext(), SelectCourse.VIDEO_SELECTED, 0);
    }else {
      dbActivities.setCoursesProgress(getApplicationContext(), SelectCourse.VIDEO_SELECTED, (int) player.getCurrentPosition());
    }
    Intent i = new Intent(this, SelectCourse.class);
    startActivity(i);
    finish();
  }

}
