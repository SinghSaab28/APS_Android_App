package com.aps.apsschool.home;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.aps.apsschool.beans.Student;
import com.aps.apsschool.database.DBActivities;
import com.aps.apsschool.staticutilities.StaticUtilities;
import com.aps.apsschool.user.R;
import com.aps.apsschool.user.UserLogin;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;


public class MainActivity extends AppCompatActivity {

    private VideoView mainVideoView;
    private ImageView playBtn;
    private TextView currentTimer;
    private TextView durationTimer;
    private ProgressBar currentProgress;
    private ProgressBar bufferProgress;
    private Boolean isPlaying;
    private VideoProgress videoProgress;
    private Uri videoUri;
    private int current = 0;
    private int seekVideo = 10000;
    private int duration = 0;
    private LinearLayout ll;
    private ImageView forwardBtn;
    private ImageView backwardBtn;
    private ConstraintLayout videoLayout;
    private int toolsVisibilitytime = 5000;
    final FirebaseFirestore db = StaticUtilities.DB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        db.collection("students").whereEqualTo("ROLLNUMBER", (new DBActivities(getApplicationContext()).getStudentRollNo()))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if(task.isSuccessful() && !task.getResult().isEmpty()) {
                                List<Student> students = task.getResult().toObjects(Student.class);
                                if(students.get(0).PERMISSION==false) {
                                    updateLoginFlag();
                                    Intent i = new Intent(getApplicationContext(), UserLogin.class);
                                    startActivity(i);
                                    finish();
                                }
                            }
                        }
                    }
                });

        final Handler hMic = new Handler();
        hMic.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkMic();
            }
        }, 10000);

        isPlaying = false;
        ll = findViewById(R.id.linearLayout);
        mainVideoView = findViewById(R.id.videoView);
        playBtn =findViewById(R.id.play_btn);
        currentTimer = findViewById(R.id.current_timer);
        durationTimer = findViewById(R.id.duration_timer);
        currentProgress = findViewById(R.id.progressBar);
        currentProgress.setMax(100);
        bufferProgress = findViewById(R.id.bufferProgress);
        forwardBtn = findViewById(R.id.forwardBtn);
        backwardBtn = findViewById(R.id.backwardBtn);
        videoLayout = findViewById(R.id.videoLayout);


        final Handler handler = new Handler();

        videoUri = Uri.parse(SelectCourse.VIDEO_SELECTED);

        DBActivities dbActivities = new DBActivities(getApplicationContext());
        final String courseProgress = dbActivities.getCoursesProgress(getApplicationContext(), SelectCourse.VIDEO_SELECTED);
        System.out.println("Course progress : "+courseProgress);

        mainVideoView.setVideoURI(videoUri);
        mainVideoView.requestFocus();
        mainVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mainVideoView.start();
                duration = mediaPlayer.getDuration()/1000;
                String durationString = String.format("%2d:%02d:%02d",duration/(60*60), (duration/60)%60, duration%60);
                durationTimer.setText(durationString);
            }
        });

        mainVideoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {

                if(i == mediaPlayer.MEDIA_INFO_BUFFERING_START){
                    bufferProgress.setVisibility(View.VISIBLE);
                } else if(i == mediaPlayer.MEDIA_INFO_BUFFERING_END){
                    bufferProgress.setVisibility(View.GONE);
                }

                return false;
            }
        });

        isPlaying = true;
        playBtn.setImageResource(R.mipmap.pause_button);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                playBtn.setVisibility(View.GONE);
                ll.setVisibility(View.GONE);
                forwardBtn.setVisibility(View.GONE);
                backwardBtn.setVisibility(View.GONE);
            }
        }, toolsVisibilitytime);

        videoProgress = new VideoProgress();
        videoProgress.execute();

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isPlaying) {
                    mainVideoView.pause();
                    isPlaying = false;
                    playBtn.setImageResource(R.mipmap.play_button);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            playBtn.setVisibility(View.GONE);
                            ll.setVisibility(View.GONE);
                            forwardBtn.setVisibility(View.GONE);
                            backwardBtn.setVisibility(View.GONE);
                        }
                    }, toolsVisibilitytime);
                } else {
                    mainVideoView.start();
                    isPlaying = true;
                    bufferProgress.setVisibility(View.GONE);
                    playBtn.setImageResource(R.mipmap.pause_button);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            playBtn.setVisibility(View.GONE);
                            ll.setVisibility(View.GONE);
                            forwardBtn.setVisibility(View.GONE);
                            backwardBtn.setVisibility(View.GONE);
                        }
                    }, toolsVisibilitytime);
                }
            }
        });

        forwardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainVideoView.seekTo(mainVideoView.getCurrentPosition()+seekVideo);
            }
        });

        backwardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainVideoView.seekTo(mainVideoView.getCurrentPosition()-seekVideo);
            }
        });

        videoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playBtn.setVisibility(View.VISIBLE);
                ll.setVisibility(View.VISIBLE);
                forwardBtn.setVisibility(View.VISIBLE);
                backwardBtn.setVisibility(View.VISIBLE);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        playBtn.setVisibility(View.GONE);
                        ll.setVisibility(View.GONE);
                        forwardBtn.setVisibility(View.GONE);
                        backwardBtn.setVisibility(View.GONE);
                    }
                }, toolsVisibilitytime);
            }
        });

        if(courseProgress!=null) {
            mainVideoView.seekTo(Integer.parseInt(courseProgress));
        }

    }

    private void checkMic() {
        boolean isMicFree = true;
        MediaRecorder recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setOutputFile("/dev/null");
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        try {
            recorder.start();
        } catch (IllegalStateException e) {
            Log.e("MediaRecorder", "start() failed: MIC is busy");
            Intent i = new Intent(this, SelectCourse.class);
            startActivity(i);
            finish();
            isMicFree = false;
        }
        if (isMicFree) {
            Log.e("MediaRecorder", "start() successful: MIC is free");
        }
        recorder.stop();
        recorder.release();
        Handler hMic = new Handler();
        hMic.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkMic();
            }
        }, 120000);
    }

    public class VideoProgress extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
        try {
            do {
                if (isPlaying) {
//                    bufferProgress.setVisibility(View.GONE);
                    current = mainVideoView.getCurrentPosition() / 1000;
                    try {
                        int currentPercent = current * 100 / duration;
                        publishProgress(currentPercent);
                    } catch (Exception e) {

                    }
                }
            } while (currentProgress.getProgress() <= 100);
        }catch(IllegalStateException e){

        }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            try {
                currentProgress.setProgress(values[0]);
                String currentString = "0:00";
                if(current/(60*60)!=0){
                    currentString = String.format("%2d:%02d:%02d",current/(60*60), (current/60)%60, current%60);
                }else{
                    currentString = String.format("%02d:%02d",current/60, current%60);
                }
                currentTimer.setText(currentString);
            } catch (Exception e){

            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        isPlaying = false;
        videoProgress.cancel(true);
        mainVideoView.stopPlayback();
    }

    @Override
    protected void onStart() {
        super.onStart();
        isPlaying = true;
        videoProgress = new VideoProgress();
        videoProgress.execute();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        DBActivities dbActivities = new DBActivities(getApplicationContext());
        dbActivities.setCoursesProgress(getApplicationContext(), SelectCourse.VIDEO_SELECTED, mainVideoView.getCurrentPosition());
        videoProgress.cancel(true);
        mainVideoView.stopPlayback();
        Intent i = new Intent(this, SelectCourse.class);
        startActivity(i);
        finish();
    }

    private void updateLoginFlag() {
        db.collection("students").whereEqualTo("ROLLNUMBER", (new DBActivities(getApplicationContext())).getStudentRollNo())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if(task.getResult().size()==1) {
                                System.out.println("jagga");
                                for (DocumentSnapshot document : task.getResult()) {
                                    String id = document.getId();
                                    db.collection("students").document(id).update("LOGIN_FLAG", false) //Set student object
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(MainActivity.this, "Updated Successfully", Toast.LENGTH_SHORT).show();
                                                    Log.d("SelectCourse", "updateLoginFlag : LOGIN_FLAG updated to false.");
                                                }
                                            });
                                }
                            }
                        }
                    }
                });
    }
}